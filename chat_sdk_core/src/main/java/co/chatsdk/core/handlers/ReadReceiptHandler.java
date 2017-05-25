package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.core.BThread;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ReadReceiptHandler {

    public void updateReadReceipts(BThread thread);
    public void markRead (BThread thread);

}
