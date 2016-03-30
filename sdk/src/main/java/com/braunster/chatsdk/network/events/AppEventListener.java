/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

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
