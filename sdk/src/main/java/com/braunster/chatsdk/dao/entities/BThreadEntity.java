package com.braunster.chatsdk.dao.entities;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;

import java.util.Date;
import java.util.List;

/**
 * Created by braunster on 25/06/14.
 */
public abstract class BThreadEntity extends Entity{

    public static class Type{
        public static final int Private = 0;
        public static final int Public = 1;
    }

    public abstract Date lastMessageAdded();

    /** Fetch messages list from the db for current thread, Messages will be order Desc/Asc on demand.*/
    public abstract List<BMessage> getMessagesWithOrder(int order);

    public abstract boolean hasUser(BUser user);


    public abstract List<BUser> getUsers();

    public abstract String displayName();

    public abstract Integer getType();


}
