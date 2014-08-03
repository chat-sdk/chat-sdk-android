package com.braunster.chatsdk.network;

/**
 * Created by braunster on 23/06/14.
 */
public class BDefines {

    /** Each type get his own prefix by using the private constructor.
     * This is the place to change the prefix if wanted.*/
    public static final class BAccountType{
        public static final int Password = 1;
        public static final int Facebook = 2;
        public static final int Twitter = 4;
        public static final int Google = 3;
        public static final int Anonymous = 5;
        public static final int Register = 6;
    }

    public static final boolean AnonymuosLoginEnabled = true;
    public static final String InitialsForAnonymuos = "AN";

    public static final String DIVIDER = ",";

    // Vibration Duration in Millis
    public static final int VIBRATION_DURATION = 300;

    public static final int MAX_MESSAGES_TO_PULL = 30;

    public static final String ContactDeveloper_Email = "itzik.register@gmail.com";
    public static final String ContactDeveloper_Subject = "Report: ";
    public static final String ContactDeveloper_DialogTitle = "Contact Developer";

    public static final class APIs{
        /* Change this id's to your app id's!*/
        // Facebook
        public static final String FacebookAppId = "247787328762280";

        // Google
        public static final String GoogleAppId = "";

        // Twitter
        public static final String TwitterConsumerKey = "nBgJK3BFEdtekszIvpdg1FnsF";
        public static final String TwitterConsumerSecret = "9kTtJBTQrjKqGnwqX7jzfT7Z8khmwQKQ5T0IcjpM77cJlKKAOr";

        // Parse
        public static final String ParseAppId = "oZsitstcYtm1yvwTkrMIpLtpE3SulFvdVb12rH8d";
        public static final String ParseClientKey = "A1lZKXHQWKUQogcfCCIFBg1cPbJ0dtekh2ghV6He";

        public static final String BugSenseKey = "a83a219d";
    }

    // Static values for prefs.
    // Prefs
    public static final class Prefs{
        public static final String CurrentUserLoginInfo = "Current-User-Login-Info";
        public static final String AuthenticationID = "authentication-id";
        public static final String TokenKey = "token";
        public static final String AccountTypeKey = "accounty-type";
        public static final String ObjectKey = "object";
        public static final String LoginEmailKey = "login-email";
        public static final String LoginTypeKey = "login-type";
        public static final String LoginPasswordKey = "login-password";
    }

    public static class Keys{
        /*Metadata*/
        public static final String BEmail = "email";
        public static final String Bkey = "key";
        public static final String BValue = "value";
        public static final String BPhone = "phone";
        public static final String BPicture = "picture";
        public static final String BPictureURL = "pictureURL";
        public static final String BPictureURLThumbnail = "pictureURLThumbnail";

        public static final String BAuthenticationID = "authentication-id";
        public static final String BLastMessageAdded = "last-message-added";
        public static final String BFirebaseId = "firebase-id";
        public static final String BUserFirebaseId = "user-firebase-id";
        public static final String BThreadFirebaseId = "thread-firebase-id";
        public static final String BRoundEnds = "round-ends";
        public static final String BColor = "color";
        public static final String BTextColor = "text-color";
        public static final String BFontName = "font-name";
        public static final String BFontSize = "font-size";
        public static final String BName = "name";
        public static final String BNull = "null";
        public static final String BCreationDate = "creation-date";
        public static final String BPayload = "payload";
        public static final String BType = "type";
        public static final String BOnline = "online";
        public static final String BDate = "date";
        public static final String BLastUpdated = "last-updated";
        public static final String BLastOnline = "last-online";

    }

    public static final class Time{
        public static final float BMinutes = 60.0f;
        public static final float BHours = 60.0f * BMinutes;
        public static final float BDays = 24.0f * BHours;
        public static final float BMonths = 30.0f * BDays;
        public static final float BYears = 12.0f * BMonths;
    }

    public static final class ImageProperties{
        public static final int MAX_BYTE_SIZE = 1500;

        public static final int MAX_WIDTH_IN_PX = 1920;
        public static final int MAX_HEIGHT_IN_PX = 2560;

        public static final int MAX_IMAGE_THUMBNAIL_SIZE = 600;
    }
}
