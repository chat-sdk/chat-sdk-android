/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.dao.entities;

/**
 * Created by braunster on 25/06/14.
 */
public class BLinkedAccountEntity extends Entity{

    public static class Type{
        public static final int FACEBOOK = 0;
        public static final int TWITTER = 1;
        public static final int PASSWORD = 2;
        public static final int GOOGLE = 3;
    }
}
