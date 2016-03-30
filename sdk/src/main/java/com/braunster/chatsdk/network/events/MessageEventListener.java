/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.dao.BMessage;

public abstract class MessageEventListener extends Event {

    public MessageEventListener(String tag, String threadEntityId){
        super(tag, threadEntityId);
    }

    public abstract boolean onMessageReceived(BMessage message);
}
