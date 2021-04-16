package sdk.chat.core.handlers;

import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;

public interface IEncryptionHandler {

//    Single<String> encrypt(List<User> users, Map<String, Object> data);
//    Single<Map<String, Object>> decrypt(String encryptedString);

    Map<String, Object> encrypt(Message message);

//    Completable encrypt(Message message);
//    Completable decrypt (String message);
    Map<String, Object> decrypt(String message) throws Exception;

    String publicKey();
    String privateKeyId();
    Completable publishKey();
    void addPublicKey(String userEntityID, String identifier, String key);

}