package sdk.chat.core.handlers;

import sdk.chat.core.dao.Message;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ReadReceiptHandler {
    void markRead(Message message);
    void markDelivered(Message message);
}
