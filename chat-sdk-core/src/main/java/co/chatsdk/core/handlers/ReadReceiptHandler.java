package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Message;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ReadReceiptHandler {
    void markRead (Message message);
    void markDelivered (Message message);
}
