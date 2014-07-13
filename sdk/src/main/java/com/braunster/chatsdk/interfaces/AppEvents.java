package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;

/**
 * Created by braunster on 30/06/14.
 */

public interface AppEvents {
    public static final int USER_DETAILS_CHANGED = 0;
    public static final int MESSAGE_RECEIVED = 1;
    public static final int THREAD_DETAILS_CHANGED = 2;
    public static final int USER_ADDED_TO_THREAD = 3;

    public boolean onUserDetailsChange(BUser user);
    public boolean onMessageReceived(BMessage message);

    public boolean onThreadIsAdded(String threadId);
    public boolean onThreadDetailsChanged(String threadId);
    public boolean onUserAddedToThread(String threadId, String userId);
}
