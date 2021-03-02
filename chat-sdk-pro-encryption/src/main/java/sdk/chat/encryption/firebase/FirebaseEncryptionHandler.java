package sdk.chat.encryption.firebase;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.encryption.EncryptionHandler;

public class FirebaseEncryptionHandler extends EncryptionHandler {

    @Override
    public Completable publishKey() {
        // Add the key to our meta data
        User user = ChatSDK.currentUser();
        String publicKey = publicKey();
        if (user != null && publicKey != null) {
            user.setMetaValue(Keys.PublicKey, publicKey);
            return ChatSDK.core().pushUser();
        }
        return Completable.complete();
    }

}
