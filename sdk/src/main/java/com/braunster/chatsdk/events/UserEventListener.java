package com.braunster.chatsdk.events;

import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.AppEvents;

public abstract class UserEventListener extends Event implements AppEvents{
    public UserEventListener(String tag, String userEntityId){
        super(tag, userEntityId);
    }

    @Override
    public abstract boolean onUserDetailsChange(BUser user);
}
