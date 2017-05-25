package co.chatsdk.core.interfaces;


import co.chatsdk.core.dao.core.BMessage;
import co.chatsdk.core.dao.core.BThread;
import co.chatsdk.core.dao.core.BUser;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public interface StorageAdapter {

    public BUser fetchUserWithEntityID (String entityID);
    public BThread fetchThreadWithEntityID (String entityID);
    public BMessage fetchMessageWithEntityID (String entityID);

}
