package sdk.chat.encryption;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.utils.Base64;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.IEncryptionHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

public class EncryptionHandler implements IEncryptionHandler {

    public final VirgilCrypto virgilCrypto = new VirgilCrypto();

    protected PrivateKey privateKey;
    protected PublicKey publicKey;

    // Public chat encryption enabled

    // Here are the hooks, one for a successful authentication and one for a recieved message

    public EncryptionHandler() {

        Hook decryptionHook = Hook.async(data -> {
            Object messageObject = data.get(HookEvent.Message);
            if (messageObject instanceof Message) {
                Message message = (Message) messageObject;
                Logger.debug("Decrypt Message: " + message.getText());
                return decrypt(message);
            }
            return Completable.complete();
        });
        ChatSDK.hook().addHook(decryptionHook, HookEvent.MessageReceived, HookEvent.MessageSent);

        Hook encryptHook = Hook.async(data -> {
            Object messageObject = data.get(HookEvent.Message);
            if (messageObject instanceof Message) {
                Message message = (Message) messageObject;
                Logger.debug("Encrypt Message: " + message.getText());
                return encrypt(message);
            }
            return Completable.complete();
        });
        ChatSDK.hook().addHook(encryptHook, HookEvent.MessageWillSend);

        Hook auth = Hook.sync(data -> {
            privateKey = null;
            publicKey = null;

            publishKey().subscribe();

//            return getKeys(ChatSDK.currentUserID()).flatMapCompletable(keyPair -> {
//                byte [] identifierBytes = keyPair.getPublicKey().getIdentifier();
//                String identifierString = Base64.encode(identifierBytes);
//                String identifierEncoded = URLEncoder.encode(identifierString, "UTF-8");
//                VirgilPublicKey publicKey = keyPair.getPublicKey();
//                byte [] publicKeyBytes = virgilCrypto.exportPublicKey(publicKey);
//                String encodedPublicKey = Base64.encode(publicKeyBytes);
//                currentUser().setMetaString(PublicKeys + "/" + identifierEncoded, encodedPublicKey);
//                ChatSDK.core().pushUser().subscribe(ChatSDK.events());
//                return Completable.complete();
//            }).subscribeOn(RX.computation());
        });

        ChatSDK.hook().addHook(auth, HookEvent.DidAuthenticate);

    }

    protected PrivateKey getPrivateKey() {
        if (privateKey == null) {
            privateKey = EncryptionManager.shared().getKeyPair(ChatSDK.currentUserID()).privateKey;
        }
        return privateKey;
    }

    @Override
    public Completable encrypt(Message message) {
        return extractPublicKeys(message).flatMapCompletable(publicKeyList -> {
            if (publicKeyList.size() > 0) {
                // Get the message meta data as bytes
                byte[] metaData = mapToBytes(message.getMetaValuesAsMap());

                byte[] encryptedMessage = virgilCrypto.encrypt(metaData, publicKeyList);
                String encryptedMessageString = Base64.encode(encryptedMessage);

                HashMap<String, Object> newMessageMeta = new HashMap<>();
                newMessageMeta.put(Keys.MessageEncryptedPayloadKey, encryptedMessageString);

                message.setMetaValues(newMessageMeta);
            }
            return Completable.complete();
        }).subscribeOn(RX.computation());
    }

    protected byte [] mapToBytes(Map<String, Object> map) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return json.getBytes("UTF-8");
    }

    protected Map<String, Object> bytesToMap (byte [] bytes) {
        String json = new String(bytes);
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<HashMap<String, Object>>(){}.getType());
    }

    protected Single<List<VirgilPublicKey>> extractPublicKeys (Message message) {
        return Single.create((SingleOnSubscribe<List<VirgilPublicKey>>) emitter -> {

            List<VirgilPublicKey> publicKeyList = new ArrayList<>();

            if (message.getThread().typeIs(ThreadType.Public)) {
                Object commonPublicKeyObject = ChatSDK.config().getRemoteConfigValue("public-key");
                if (commonPublicKeyObject instanceof String) {
                    String commonPublicKey = (String) commonPublicKeyObject;
                    if (!commonPublicKey.isEmpty()) {
                        byte[] keyBytes = Base64.decode(commonPublicKey);
                        VirgilPublicKey virgilPublicKey = virgilCrypto.importPublicKey(keyBytes);
                        publicKeyList.add(virgilPublicKey);
                    }
                }
            } else {

                List<User> users = message.getThread().getUsers();
                List<PublicKey> keys = new ArrayList<>();
                List<User> failedUsers = new ArrayList<>();

                for (User u : users) {
                    PublicKey key = PublicKeyStorage.getKey(u.getEntityID());

                    if (key != null) {
                        keys.add(key);
                    } else {
                        failedUsers.add(u);
                    }
                }

                if (failedUsers.isEmpty()) {
                    for (PublicKey key: keys) {
                        publicKeyList.add(key.key);
                    }
                } else {
                    for (User u: failedUsers) {
                        requestPublicKey(u.getEntityID());
                    }
                }

            }
            emitter.onSuccess(publicKeyList);
        }).subscribeOn(RX.computation());
    }

    @Override
    public Completable decrypt(Message message) {
        return Completable.defer(() -> {

            if (message.getThread() == null) {
                return Completable.complete();
            }

            // Get the private keys
            return privateKeyFor(message).flatMapCompletable(virgilPrivateKey -> {

                String encryptedMessageString = message.stringForKey(Keys.MessageEncryptedPayloadKey);
                if (!encryptedMessageString.isEmpty()) {
                    byte [] encryptedMessageData = Base64.decode(encryptedMessageString);
                    byte [] decryptedMessageData = virgilCrypto.decrypt(encryptedMessageData, virgilPrivateKey);
                    Map<String, Object> metaData = bytesToMap(decryptedMessageData);
                    message.setMetaValues(metaData);
                }
                return Completable.complete();
            }).subscribeOn(RX.computation());

        }).doOnError(throwable -> {
            message.getThread().removeMessage(message);
        }).subscribeOn(RX.computation());

    }

        protected Single<VirgilPrivateKey> privateKeyFor(final Message message) {
        return Single.defer(() -> {
            if (message.getThread().typeIs(ThreadType.Public)) {
                Object commonPrivateKeyObject = ChatSDK.config().getRemoteConfigValue("private-key");
                if (commonPrivateKeyObject instanceof String) {
                    String commonPrivateKey = (String) commonPrivateKeyObject;
                    if (!commonPrivateKey.isEmpty()) {
                        byte[] keyBytes = Base64.decode(commonPrivateKey);
                        return Single.just(virgilCrypto.importPrivateKey(keyBytes));
                    }
                }
            } else {
                return Single.just(getPrivateKey().key);
            }
            return Single.error(new Throwable("No private key available"));
        }).subscribeOn(RX.computation());
    }


    @Override
    public Map<String, Object> decrypt(String message) throws Exception {
        if (!message.isEmpty() && ChatSDK.auth().isAuthenticatedThisSession()) {
            if (!message.isEmpty()) {
                VirgilPrivateKey key = privateKey.key;
                byte [] encryptedMessageData = Base64.decode(message);
                byte [] decryptedMessageData = virgilCrypto.decrypt(encryptedMessageData, key);
                return bytesToMap(decryptedMessageData);
            }
        }
        return null;
    }

    @Override
    public String publicKey() {
        if (publicKey == null) {
            publicKey = EncryptionManager.shared().getKeyPair(ChatSDK.currentUserID()).publicKey;
        }
        try {
            return privateKey.getBase64EncodedString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String privateKeyId() {
        return privateKey.getId();
    }

    @Override
    public Completable publishKey() {
        return Completable.complete();
    }

    @Override
    public void addPublicKey(String userEntityID, String identifier, String key) {
        try {
            PublicKeyStorage.addKey(userEntityID, identifier, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String requestPublicKey(String userId) {
        return null;
    }

    public void stop() {

    }
}
