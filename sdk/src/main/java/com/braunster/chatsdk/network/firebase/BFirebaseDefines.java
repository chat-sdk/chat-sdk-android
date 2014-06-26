package com.braunster.chatsdk.network.firebase;

/**
 * Created by itzik on 6/8/2014.
 */
public class BFirebaseDefines {

    /* BDefines.h implementation, Suggest a better name fot this class*/
    public static final class Path{
        public static final String BUsersPath = "users";
        public static final String BMessagesPath = "messages";
        public static final String BThreadPath = "threads";
        public static final String BPublicThreadPath = "public-threads";
        public static final String BDetailsPath = "details";
        public static final String BIndexPath = "index";
        public static final String BOnlinePath = "online";
        public static final String BMetaPath = "meta";
    }

    public static final class Defaults{
        public static final String MessageColor = "Red";
        public static final String MessageTextColor = "Black";
        public static final String MessageFontName= "Roboto";
        public static final int MessageFontSize = 25;
    }

    // How many historic messages should we load this will
    // load the messages that were sent in the last x seconds
    public static final int NumberOfMessagesPerBatch = 30;

    /*
    * ASK is this callbacks?
      typedef void(^Completion)(id object);
      typedef void(^CompletionErr)(NSError * error);
     */


}
