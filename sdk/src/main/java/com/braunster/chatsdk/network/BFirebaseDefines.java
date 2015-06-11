/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 *
 * This is the only trace in the core SDK for firebase, Needed for some stuff that cannot be removed from the core.
 *
 */
public class BFirebaseDefines {

    public static Map<String, String> getServerTimestamp(){
        Map<String, String> timestamp = new HashMap<String, String>();
        // Firebase server timestamp
        timestamp.put(".sv", "timestamp");

        return timestamp;
    }

    /* BDefines.h implementation, Suggest a better name fot this class*/
    public static final class Path{
        public static final char SEPARATOR = '/';

        public static final String BUsers = "users";
        public static final String BMessages = "messages";
        public static final String BLastMessage = "lastMessage";
        public static final String BThread = "rooms";
        public static final String BPublicThread = "public-rooms";
        public static final String BIndex = "searchIndex";
        public static final String BOnline = "online";
        public static final String BMeta = "meta";
        public static final String BImage = "image";
        public static final String BThumbnail = "thumbnail";
        public static final String BState = "state";
        public static final String BUsersMeta = "usersMeta";
        public static final String BTyping = "typing";
        public static final String BFollows = "follows";
        public static final String BFollowers = "followers";
        public static final String BBlocked = "blocked";
        public static final String BFriends = "friends";
    }



    // How many historic messages should we load this will
    // load the messages that were sent in the last x seconds
    public static final int NumberOfMessagesPerBatch = 30;

    public static final int NumberOfUserToLoadForIndex = 20;

}
