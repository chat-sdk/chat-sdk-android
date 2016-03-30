package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.dao.BMessage;

public abstract class MessageEventListener extends Event {

    public MessageEventListener(String tag, String threadEntityId){
        super(tag, threadEntityId);
    }

    public abstract boolean onMessageReceived(BMessage message);
}
