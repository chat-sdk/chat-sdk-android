package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.interfaces.AppEvents;

public abstract class ThreadEventListener extends Event implements AppEvents{
    public ThreadEventListener(String tag, String threadEntityID){
        super(tag, threadEntityID);
    }

    @Override
    public abstract boolean onThreadDetailsChanged(String threadId);

    @Override
    public boolean onUserAddedToThread(String threadId, String userId) {
        return false;
    }
}
