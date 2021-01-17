package sdk.chat.encryption;

import sdk.chat.core.session.ChatSDK;

public class PublicKeyStorage {

    public static PublicKey getKey(String userId) {
        sdk.chat.core.dao.PublicKey pk = ChatSDK.db().getPublicKey(userId);
        if (pk != null) {
            String key = pk.getKey();
            if (key != null && ! key.isEmpty()) {
                try {
                    return new PublicKey(key);
                } catch (Exception e) {

                }
            }
        }
        return null;
    }

    public static void deleteKey(String userId) {
        ChatSDK.db().deletePublicKey(userId);
    }

    public static void deleteAllKeys() {
        ChatSDK.db().deleteAllPublicKeys();
    }

    public static void addKey(String userId, String identifier, String key) throws Exception {
        ChatSDK.db().addPublicKey(userId, identifier, key);
    }

}
