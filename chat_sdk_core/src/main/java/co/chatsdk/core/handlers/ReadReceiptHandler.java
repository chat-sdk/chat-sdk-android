package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Thread;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ReadReceiptHandler {

    public void updateReadReceipts(Thread thread);
    public void markRead (Thread thread);

}
