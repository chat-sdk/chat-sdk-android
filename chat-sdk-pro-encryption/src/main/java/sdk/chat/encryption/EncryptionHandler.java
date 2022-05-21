package sdk.chat.encryption;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.utils.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.IEncryptionHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.recycler.ButtonViewModel;
import sdk.chat.ui.recycler.SectionViewModel;
import sdk.guru.common.RX;

public abstract class EncryptionHandler implements IEncryptionHandler {

    public final VirgilCrypto virgilCrypto = new VirgilCrypto();

    protected PrivateKey privateKey;
    protected PublicKey publicKey;

    public EncryptionHandler() {
        Hook auth = Hook.sync(data -> {
            privateKey = null;
            publicKey = null;

            publishKey().subscribe();
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
    public Map<String, String> encrypt(final Message message) {
        return encryptMeta(message.getThread(), message.getMetaValuesAsMap());
    }

    @Override
    public Map<String, String> encryptMeta(Thread thread, Map<String, String> meta) {
        // Get the list of keys
        List<VirgilPublicKey> keys = extractKeysSync(thread);

        if (keys.size() > 0) {
            // Get the message meta data as bytes
            try {
                byte[] metaData = mapToBytes(meta);

                byte[] encryptedMessage = virgilCrypto.encrypt(metaData, keys);
                String encryptedMessageString = Base64.encode(encryptedMessage);

                Map<String, String> newMessageMeta = new HashMap<>();
                newMessageMeta.put(Keys.MessageEncryptedPayloadKey, encryptedMessageString);
                newMessageMeta.put(Keys.MessageText, ChatSDK.shared().context().getString(R.string.encrypted_message));

                return newMessageMeta;

            } catch (Exception e) {

            }
        }
        return null;
    }

    protected <T extends Object> byte [] mapToBytes(Map<String, T> map) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return json.getBytes("UTF-8");
    }

    protected Map<String, Object> bytesToMap (byte [] bytes) {
        String json = new String(bytes);
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<HashMap<String, Object>>(){}.getType());
    }

    protected Single<List<VirgilPublicKey>> extractPublicKeys (final Message message) {
        return Single.create((SingleOnSubscribe<List<VirgilPublicKey>>) emitter -> {
            emitter.onSuccess(extractKeysSync(message.getThread()));
        }).subscribeOn(RX.computation());
    }

    public List<VirgilPublicKey> extractKeysSync(final Thread thread) {
        List<VirgilPublicKey> publicKeyList = new ArrayList<>();

        if (!thread.typeIs(ThreadType.Public)) {

            List<User> users = thread.getUsers();
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
        return publicKeyList;
    }


    @Override
    public Map<String, Object> decrypt(String message) throws Exception {
        if (!message.isEmpty() && ChatSDK.auth().isAuthenticatedThisSession()) {
            VirgilPrivateKey key = getPrivateKey().key;
            byte [] encryptedMessageData = Base64.decode(message);
            byte [] decryptedMessageData = virgilCrypto.decrypt(encryptedMessageData, key);
            return bytesToMap(decryptedMessageData);
        }
        return null;
    }

    @Override
    public String publicKey() {
        if (publicKey == null) {
            publicKey = EncryptionManager.shared().getKeyPair(ChatSDK.currentUserID()).publicKey;
        }
        try {
            return publicKey.getBase64EncodedString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String privateKeyId() {
        return getPrivateKey().getId();
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

    public static String exportKeyPair() throws Exception {
        KeyPair pair = EncryptionManager.shared().getKeyPair(ChatSDK.currentUserID());
        return pair.privateKey.getBase64EncodedString() + " " + pair.publicKey.getBase64EncodedString();
    }

    public static void importKeyPair(String keys) throws Exception {
        String[] keysArray = keys.split(" ");
        if (keysArray.length == 2) {
            String privateKeyString = keysArray[0];
            String publicKeyString = keysArray[1];

            KeyPair pair = new KeyPair(privateKeyString, publicKeyString);
            EncryptionManager.shared().save(ChatSDK.currentUserID(), pair);

            // Try to decript any failed messages
            for (Message m: ChatSDK.db().fetchMessagesWithFailedDecryption()) {
                try {
                    Map<String, Object> meta = ChatSDK.encryption().decrypt(m.getEncryptedText());
                    m.setMetaValues(meta);
                    m.setEncryptedText(null);
                    m.update();
                } catch (Exception e) {

                }
            }
            ChatSDK.encryption().publishKey().subscribe();
        }
    }

    public static void activate(Context context) {

        Integer color = context.getResources().getColor(R.color.black);
        ChatSDKUI.shared().addSettingsItem(new SectionViewModel("Encryption", 10));
        ChatSDKUI.shared().addSettingsItem(new ButtonViewModel("Export Keys", color, (activity) -> {
            Intent intent = new Intent(activity, ExportKeyQRActivity.class);
            activity.startActivity(intent);
        }));
        ChatSDKUI.shared().addSettingsItem(new ButtonViewModel("Import  Keys", color, (activity) -> {
            Intent intent = new Intent(activity, ImportKeyQRActivity.class);
            activity.startActivity(intent);
        }));

    }
}
