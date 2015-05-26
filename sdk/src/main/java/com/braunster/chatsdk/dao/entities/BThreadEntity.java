/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.dao.entities;

import android.support.annotation.StringDef;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

/**
 * Created by braunster on 25/06/14.
 */
public abstract class BThreadEntity extends Entity{

    @StringDef({Type.OneToOne, Type.Public, Type.Group, Type.NoType})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ThreadType {}
    
    
    public static class Type{
        /**
         * This was added so we wont get warning from the system when placing an empty string for specifying no particular thread type.
         *
         * @see BUser#getThreads()*/
        public static final String NoType = "no_type";
        public static final String OneToOne = "1to1";
        public static final String Public = "public";
        public static final String Group = "group";
    }

    public abstract Date lastMessageAdded();

    /** Fetch messages list from the db for current thread, Messages will be order Desc/Asc on demand.*/
    public abstract List<BMessage> getMessagesWithOrder(int order);

    public abstract boolean hasUser(BUser user);


    @ThreadType
    public abstract void setType(@ThreadType String type);
    
    public abstract List<BUser> getUsers();

    public abstract String displayName();

    public abstract String getType();


}
