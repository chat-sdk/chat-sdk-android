package co.chatsdk.core.handlers;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ReadReceiptHandler {

    public void updateReadReceipts(Object thread);
    public void markRead (Object thread);

}
