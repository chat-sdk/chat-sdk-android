package sdk.chat.core.handlers;

import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;

public interface IEncryptionHandler {

    Completable encrypt (Message message);
    Completable decrypt (Message message);
    Map<String, Object> decrypt (String message) throws Exception;

    String publicKey();
    String privateKeyId();
    Completable publishKey();
    void addPublicKey(String userEntityID, String identifier, String key);

}