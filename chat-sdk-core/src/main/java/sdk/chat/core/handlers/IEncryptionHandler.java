package sdk.chat.core.handlers;

import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;

public interface IEncryptionHandler {

    Map<String, String> encrypt(Message message);
    Map<String, String> encryptMeta(ThreadX thread, Map<String, String> meta);

    Map<String, Object> decrypt(String message) throws Exception;

    String publicKey();
    String privateKeyId();
    Completable publishKey();
    void addPublicKey(String userEntityID, String identifier, String key);

}