package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Message;

public interface EncryptionHandler {

    void encrypt (Message message);
    void decrypt (Message message);
}