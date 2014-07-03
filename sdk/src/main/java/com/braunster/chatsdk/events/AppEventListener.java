package com.braunster.chatsdk.events;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.AppEvents;

public class AppEventListener extends Event{

    public AppEventListener(String tag){
        super(tag, "");
    }

    @Override
    public boolean onMessageReceived(BMessage message) {
        return false;
    }

    @Override
    public boolean onThreadAdded(String threadId) {
        return false;
    }

    @Override
    public boolean onUserDetailsChange(BUser user) {
        return false;
    }
}
