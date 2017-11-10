package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Thread;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ReadReceiptHandler {

    void updateReadReceipts(Thread thread);
    void markRead (Thread thread);

}
