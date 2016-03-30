package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;

public class AppEventListener extends Event{

    public AppEventListener(String tag){
        super(tag, "");
    }

    @Override
    public boolean onMessageReceived(BMessage message) {
        return false;
    }

    @Override
    public boolean onThreadDetailsChanged(String threadId) {
        return false;
    }

    @Override
    public boolean onUserDetailsChange(BUser user) {
        return false;
    }
}
