package sdk.chat.core.handlers;

import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;

public interface IEncryptionHandler {

    Map<String, Object> encrypt(Message message);
    Map<String, Object> encryptMeta(Thread thread, Map<String, Object> meta);

    Map<String, Object> decrypt(String message) throws Exception;

    String publicKey();
    String privateKeyId();
    Completable publishKey();
    void addPublicKey(String userEntityID, String identifier, String key);

}