package sdk.chat.encryption;

import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilKeyPair;

import sdk.chat.core.session.ChatSDK;

public class EncryptionManager {

    static final EncryptionManager instance = new EncryptionManager();
    public static EncryptionManager shared() {
        return instance;
    }

    public static String MyPrivateKey = "private-key";
    public static String MyPublicKey = "public-key";

    protected final VirgilCrypto crypto = new VirgilCrypto();

    public KeyPair getKeyPair(String userId) {

        KeyPair pair = null;

        try {
            pair = load(userId);
        } catch (Exception e) {

        }

        if (pair == null) {
            try {
                VirgilKeyPair vp = crypto.generateKeys();
                pair = new KeyPair(vp);
                save(userId, pair);
            } catch (Exception e) {

            }
        }

        return pair;
    }

    public KeyPair load(String userId) throws Exception {

        KeyPair pair = null;

        String privateKeyString = ChatSDK.shared().getPreferences().getString(MyPrivateKey + userId, null);
        String publicKeyString = ChatSDK.shared().getPreferences().getString(MyPublicKey + userId, null);

        if (privateKeyString != null && publicKeyString != null) {
            pair = new KeyPair(privateKeyString, publicKeyString);
            return pair;
        }
        return pair;
    }

    public void save(String userId, KeyPair keys) throws Exception {
        ChatSDK.shared().getPreferences().edit().putString(MyPrivateKey + userId, keys.privateKey.getBase64EncodedString()).apply();
        ChatSDK.shared().getPreferences().edit().putString(MyPublicKey + userId, keys.publicKey.getBase64EncodedString()).apply();
        // Also save to the key store
        PublicKeyStorage.addKey(userId, keys.privateKey.getId(), keys.publicKey.getBase64EncodedString());
    }

}
