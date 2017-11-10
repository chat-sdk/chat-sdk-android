package co.chatsdk.core.interfaces;


import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public interface StorageAdapter {

    public User fetchUserWithEntityID (String entityID);
    public Thread fetchThreadWithEntityID (String entityID);
    public Message fetchMessageWithEntityID (String entityID);

}
