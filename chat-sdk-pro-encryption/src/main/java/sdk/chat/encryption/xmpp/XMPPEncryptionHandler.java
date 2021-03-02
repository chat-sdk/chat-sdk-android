package sdk.chat.encryption.xmpp;

import io.reactivex.Completable;
import sdk.chat.encryption.EncryptionHandler;

public class XMPPEncryptionHandler extends EncryptionHandler {
    @Override
    public Completable publishKey() {
        return Completable.complete();
    }
}
