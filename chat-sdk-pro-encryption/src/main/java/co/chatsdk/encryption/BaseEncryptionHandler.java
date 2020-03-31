package co.chatsdk.encryption;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilKeyPair;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.crypto.exceptions.CryptoException;
import com.virgilsecurity.sdk.utils.Base64;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EncryptionHandler;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

import static co.chatsdk.core.session.ChatSDK.currentUser;

public class BaseEncryptionHandler implements EncryptionHandler {

    public static String MyPrivateKey = "private-key";
    public static String MyPublicKey = "public-key";

    public static String PublicKeys = "public-keys";
    public final VirgilCrypto virgilCrypto = new VirgilCrypto();
    public static String EncryptedMessageKey = "encrypted-message";

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

        Hook decryptionHook = Hook.sync(data -> {
            Object message = data.get(HookEvent.Message);
            if (message instanceof Message) {
                System.out.println("Message: " + ((Message) message).getText());
                decrypt((Message) message);
            }
        });
        ChatSDK.hook().addHook(decryptionHook, HookEvent.MessageReceived);

        Disposable d = ChatSDK.events().source().filter(NetworkEvent.
                filterType(EventType.MessageSendStatusUpdated)).subscribe(networkEvent -> {
                    MessageSendProgress progress = networkEvent.getMessageSendProgress();
                    if (progress.getStatus() == MessageSendStatus.WillSend) {
                        encrypt(networkEvent.getMessage());
                    }
                });

        Hook auth = Hook.async(data -> {
            return Completable.create(emitter -> {
                // This code will run here, when the user is logged in
                // Must upload the user's public key to the user'r profile
                User currentUser = currentUser();

                try {
                    VirgilKeyPair keyPair = getKeys(currentUser.getEntityID());
                    byte [] identifierBytes = keyPair.getPublicKey().getIdentifier();
                    String identifierString = Base64.encode(identifierBytes);
                    String identifierEncoded = URLEncoder.encode(identifierString, "UTF-8");
                    VirgilPublicKey publicKey = keyPair.getPublicKey();
                    byte [] publicKeyBytes = virgilCrypto.exportPublicKey(publicKey);
                    String encodedPublicKey = Base64.encode(publicKeyBytes);
                    currentUser().setMetaString(PublicKeys + "/" + identifierEncoded, encodedPublicKey);
                    ChatSDK.core().pushUser().subscribe(ChatSDK.events());
                    emitter.onComplete();
                }
                catch (Exception e) {
                    emitter.onError(e);
                }
            });
        });
        ChatSDK.hook().addHook(auth, HookEvent.DidAuthenticate);

    }

    //Checking if the key was saved before. If so, the key is retrieved. If not, the key is generated and stored.


    public VirgilKeyPair getKeys(String entityID) throws Exception {

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

        return keyPair;
    }

    // Here is where we actually encrypt and decrypt the messages;

    @Override
    public void encrypt(Message message) {
        try {
            List<VirgilPublicKey> publicKeyList = extractPublicKeys(message);
            if (publicKeyList.size() > 0) {
                // Get the message meta data as bytes
                byte [] metaData = mapToBytes(message.getMetaValuesAsMap());

                byte [] encryptedMessage = virgilCrypto.encrypt(metaData, publicKeyList);
                String encryptedMessageString = Base64.encode(encryptedMessage);

                HashMap<String, Object> newMessageMeta = new HashMap<>();
                newMessageMeta.put(Keys.MessageText, ChatSDK.shared().context().getString(R.string.encrypted_message));
                newMessageMeta.put(EncryptedMessageKey, encryptedMessageString);

                message.setMetaValues(newMessageMeta);
            }
        }
        catch (Exception e) {
            // What do we do if there are no public keys? Create an error message on the screen
            e.printStackTrace();
        }
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

    protected List<VirgilPublicKey> extractPublicKeys (Message message) throws CryptoException {
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

        return publicKeyList;
    }

    @Override
    public void decrypt(Message message) {
        try {

            if (message.getThread() == null) {
                return;
            }

            VirgilPrivateKey privateKey = null;

            if (message.getThread().typeIs(ThreadType.Public)) {
                Object commonPrivateKeyObject = ChatSDK.config().getRemoteConfigValue("private-key");
                if (commonPrivateKeyObject instanceof String) {
                    String commonPrivateKey = (String) commonPrivateKeyObject;
                    if (!commonPrivateKey.isEmpty()) {
                        byte[] keyBytes = Base64.decode(commonPrivateKey);
                        privateKey = virgilCrypto.importPrivateKey(keyBytes);
                    }
                }
            } else {
                VirgilKeyPair keyPair = getKeys(ChatSDK.currentUser().getEntityID());
                privateKey = keyPair.getPrivateKey();
            }

            String encryptedMessageString = message.stringForKey(EncryptedMessageKey);
            if (!encryptedMessageString.isEmpty()) {
                byte [] encryptedMessageData = Base64.decode(encryptedMessageString);
                byte [] decryptedMessageData = virgilCrypto.decrypt(encryptedMessageData, privateKey);
                HashMap<String, Object> metaData = bytesToMap(decryptedMessageData);

                message.setMetaValues(metaData);
                message.update();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            message.getThread().removeMessage(message);
        }
    }
}
