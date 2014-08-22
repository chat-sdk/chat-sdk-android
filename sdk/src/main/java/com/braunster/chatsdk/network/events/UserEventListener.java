package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.AppEvents;

public abstract class UserEventListener extends Event implements AppEvents{
    public UserEventListener(String tag, String userEntityId){
        super(tag, userEntityId);
    }

    public UserEventListener(String tag){
        super(tag, "");
    }


    @Override
    public abstract boolean onUserDetailsChange(BUser user);
}
