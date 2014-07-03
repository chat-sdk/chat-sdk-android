package com.braunster.chatsdk.events;

import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.interfaces.AppEvents;

public abstract class ThreadEventListener extends Event implements AppEvents{
    public ThreadEventListener(String tag, String threadEntityID){
        super(tag, threadEntityID);
    }

    @Override
    public abstract boolean onThreadAdded(String threadId);
}
