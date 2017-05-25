package co.chatsdk.core.interfaces;


import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public interface StorageAdapter {

    public BUser fetchUserWithEntityID (String entityID);
    public BThread fetchThreadWithEntityID (String entityID);
    public BMessage fetchMessageWithEntityID (String entityID);

}
