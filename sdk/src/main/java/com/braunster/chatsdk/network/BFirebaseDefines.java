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

        public static final String BUsersPath = "users";
        public static final String BMessagesPath = "messages";
        public static final String BThreadPath = "threads";
        public static final String BPublicThreadPath = "public-threads";
        public static final String BDetailsPath = "details";
        public static final String BIndexPath = "searchIndex";
        public static final String BOnlinePath = "online";
        public static final String BMetaPath = "meta";
        public static final String FollowerLinks = "followers";
        public static final String BFollows = "follows";
        public static final String BImage = "imaeg";
        public static final String BThumbnail = "thumbnail";
    }



    // How many historic messages should we load this will
    // load the messages that were sent in the last x seconds
    public static final int NumberOfMessagesPerBatch = 30;

    public static final int NumberOfUserToLoadForIndex = 20;

}
