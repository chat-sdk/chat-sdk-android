package com.braunster.chatsdk.dao.entities;

import com.braunster.chatsdk.dao.BLinkedAccount;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.core.Entity;

/**
 * Created by braunster on 25/06/14.
 */
public class BLinkedAccountEntity extends Entity<BLinkedAccount>{

    public static class Type{
        public static final int FACEBOOK = 0;
        public static final int TWITTER = 1;
        public static final int PASSWORD = 2;
        public static final int GOOGLE = 3;
    }
}
