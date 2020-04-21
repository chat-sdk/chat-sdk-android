package sdk.chat.core.handlers;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;

public interface EncryptionHandler {

    Completable encrypt (Message message);
    Completable decrypt (Message message);
}