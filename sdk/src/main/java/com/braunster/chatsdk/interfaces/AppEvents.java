package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;

/**
 * Created by braunster on 30/06/14.
 */

public interface AppEvents {
    public boolean onUserDetailsChange(BUser user);
    public boolean onMessageReceived(BMessage message);
    public boolean onThreadAdded(String threadId);
}
