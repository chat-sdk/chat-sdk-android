/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

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
