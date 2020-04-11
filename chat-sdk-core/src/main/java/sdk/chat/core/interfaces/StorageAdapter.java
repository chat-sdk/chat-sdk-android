package sdk.chat.core.interfaces;


import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public interface StorageAdapter {

    User fetchUserWithEntityID(String entityID);
    Thread fetchThreadWithEntityID(String entityID);
    Message fetchMessageWithEntityID(String entityID);

}
