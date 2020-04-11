package sdk.chat.core.handlers;

import sdk.chat.core.dao.Message;

public interface EncryptionHandler {

    void encrypt (Message message);
    void decrypt (Message message);
}