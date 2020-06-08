package co.chatsdk.encryption;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilKeyPair;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.utils.Base64;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.EncryptionHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

import static sdk.chat.core.session.ChatSDK.currentUser;

public class BaseEncryptionHandler implements EncryptionHandler {

    public static String MyPrivateKey = "private-key";
    public static String MyPublicKey = "public-key";

    public static String PublicKeys = "public-keys";
    public final VirgilCrypto virgilCrypto = new VirgilCrypto();
    public static String EncryptedMessageKey = "encrypted-message";

    protected DisposableMap dm = new DisposableMap();

    // Public chat encryption enabled

    // Here are the hooks, one for a successful authentication and one for a recieved message

    public BaseEncryptionHandler() {

//        try {
//            VirgilKeyPair keyPair = virgilCrypto.generateKeys();
//            VirgilPublicKey publicKey = keyPair.getPublicKey();
//            VirgilPrivateKey privateKey = keyPair.getPrivateKey();
//
//            byte [] privateKeyBytes = virgilCrypto.exportPrivateKey(privateKey);
//            String privateKeyString = Base64.encode(privateKeyBytes);
//            byte [] publicKeyBytes = virgilCrypto.exportPublicKey(publicKey);
//            String publicKeyString = Base64.encode(publicKeyBytes);
//
//            System.out.println("Ok");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Hook decryptionHook = Hook.async(data -> {
            Object messageObject = data.get(HookEvent.Message);
            if (messageObject instanceof Message) {
                Message message = (Message) messageObject;
                Logger.debug("Decrypt Message: " + message.getText());
                return decrypt(message);
            }
            return Completable.complete();
        });
        ChatSDK.hook().addHook(decryptionHook, HookEvent.MessageReceived);

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

        Hook auth = Hook.async(data -> {
            return getKeys(ChatSDK.currentUserID()).flatMapCompletable(keyPair -> {
                byte [] identifierBytes = keyPair.getPublicKey().getIdentifier();
                String identifierString = Base64.encode(identifierBytes);
                String identifierEncoded = URLEncoder.encode(identifierString, "UTF-8");
                VirgilPublicKey publicKey = keyPair.getPublicKey();
                byte [] publicKeyBytes = virgilCrypto.exportPublicKey(publicKey);
                String encodedPublicKey = Base64.encode(publicKeyBytes);
                currentUser().setMetaString(PublicKeys + "/" + identifierEncoded, encodedPublicKey);
                ChatSDK.core().pushUser().subscribe(ChatSDK.events());
                return Completable.complete();
            }).subscribeOn(RX.computation());
        });

        ChatSDK.hook().addHook(auth, HookEvent.DidAuthenticate);

    }

    //Checking if the key was saved before. If so, the key is retrieved. If not, the key is generated and stored.


    public Single<VirgilKeyPair> getKeys(String entityID) {
        return Single.create((SingleOnSubscribe<VirgilKeyPair>) emitter -> {
            // We can have multiple private keys because we can have multiple users on one device
            // Append the users entity iD to the private key key

            String privateKeyString = ChatSDK.shared().getPreferences().getString(MyPrivateKey + entityID, null);
            String publicKeyString = ChatSDK.shared().getPreferences().getString(MyPublicKey + entityID, null);

            VirgilKeyPair keyPair;

            if (privateKeyString == null || privateKeyString.isEmpty() || publicKeyString == null || publicKeyString.isEmpty()) {

                keyPair = virgilCrypto.generateKeys();
                VirgilPublicKey publicKey = keyPair.getPublicKey();
                VirgilPrivateKey privateKey = keyPair.getPrivateKey();

                byte [] privateKeyBytes = virgilCrypto.exportPrivateKey(privateKey);
                privateKeyString = Base64.encode(privateKeyBytes);
                byte [] publicKeyBytes = virgilCrypto.exportPublicKey(publicKey);
                publicKeyString = Base64.encode(publicKeyBytes);

                ChatSDK.shared().getPreferences().edit().putString(MyPrivateKey + entityID, privateKeyString).apply();
                ChatSDK.shared().getPreferences().edit().putString(MyPublicKey + entityID, publicKeyString).apply();

            }
            else {

                byte [] privateKeyBytes = Base64.decode(privateKeyString);
                byte [] publicKeyBytes = Base64.decode(publicKeyString);

                VirgilPrivateKey privateKey = virgilCrypto.importPrivateKey(privateKeyBytes);
                VirgilPublicKey publicKey = virgilCrypto.importPublicKey(publicKeyBytes);
                keyPair = new VirgilKeyPair(publicKey, privateKey);
            }

            emitter.onSuccess(keyPair);
        }).subscribeOn(RX.computation());
    }

    // Here is where we actually encrypt and decrypt the messages;

    @Override
    public Completable encrypt(Message message) {
        return extractPublicKeys(message).flatMapCompletable(new Function<List<VirgilPublicKey>, CompletableSource>() {
            @Override
            public CompletableSource apply(List<VirgilPublicKey> publicKeyList) throws Exception {
                if (publicKeyList.size() > 0) {
                    // Get the message meta data as bytes
                    byte[] metaData = mapToBytes(message.getMetaValuesAsMap());

                    byte[] encryptedMessage = virgilCrypto.encrypt(metaData, publicKeyList);
                    String encryptedMessageString = Base64.encode(encryptedMessage);

                    HashMap<String, Object> newMessageMeta = new HashMap<>();
                    newMessageMeta.put(Keys.MessageText, ChatSDK.shared().context().getString(R.string.encrypted_message));
                    newMessageMeta.put(EncryptedMessageKey, encryptedMessageString);

                    message.setMetaValues(newMessageMeta);
                }
                return Completable.complete();
            }
        }).subscribeOn(RX.computation());
    }

    protected byte [] mapToBytes(Map<String, Object> map) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return json.getBytes("UTF-8");
    }

    protected HashMap<String, Object> bytesToMap (byte [] bytes) {
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
                for (User u : users) {
                    // Loop over meta values and see if any key matches your pattern like "public-key"
                    // Key could look like public-key-[identifier]
                    Map<String, String> meta = u.metaMap();

                    for (String key : meta.keySet()) {
                        if (key.contains(PublicKeys + "/")) {
                            String publicKey = meta.get(key);
                            if (publicKey != null && !publicKey.isEmpty()) {
                                byte[] keyBytes = Base64.decode(publicKey);
                                VirgilPublicKey virgilPublicKey = virgilCrypto.importPublicKey(keyBytes);
                                publicKeyList.add(virgilPublicKey);
                            }
                        }
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

            return privateKeyFor(message).flatMapCompletable(virgilPrivateKey -> {

                String encryptedMessageString = message.stringForKey(EncryptedMessageKey);
                if (!encryptedMessageString.isEmpty()) {
                    byte [] encryptedMessageData = Base64.decode(encryptedMessageString);
                    byte [] decryptedMessageData = virgilCrypto.decrypt(encryptedMessageData, virgilPrivateKey);
                    HashMap<String, Object> metaData = bytesToMap(decryptedMessageData);
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
                return getKeys(ChatSDK.currentUserID()).map(VirgilKeyPair::getPrivateKey);
            }
            return Single.error(new Throwable("No private key available"));
        }).subscribeOn(RX.computation());
    }
}
