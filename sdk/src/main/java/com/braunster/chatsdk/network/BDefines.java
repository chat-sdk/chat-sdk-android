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
    public static final String InitialsForAnonymous = "AN";

    public static final String DIVIDER = ",";

    // Vibration Duration in Millis
    public static final int VIBRATION_DURATION = 300;

    public static final int MAX_MESSAGES_TO_PULL = 30;

    public static final String ContactDeveloper_Email = "itzik.register@gmail.com";
    public static final String ContactDeveloper_Subject = "Report: ";
    public static final String ContactDeveloper_DialogTitle = "Contact Developer";


    public static final class Defaults{
        public static final String MessageColor = "0.635294 0.552941 0.686275 1";
        public static final String MessageTextColor = "#000000";
        public static final String MessageFontName= "Roboto";
        public static final String MessageSendingColor = "#C3C2C4";
        public static final String BubbleDefaultColor = "#27ae60";
        public static final String BubbleDefaultPressedColor = "#3498db";
        public static final int MessageFontSize = 25;
        public static final int SDKExitMode = Exit.EXIT_MODE_DOUBLE_BACK;
    }

    public static final class Exit {
        public static final int EXIT_MODE_NONE = 1990;
        public static final int EXIT_MODE_DOUBLE_BACK = 1991;
        public static final int EXIT_MODE_DIALOG = 1992;

        public static final int DOUBLE_CLICK_INTERVAL = 2000;
    }

    public static final class APIs{
        /* Change this id's to your app id's!*/
        // Facebook
        public static final String FacebookAppId = "247787328762280";

        // Google
        public static final String GoogleAppId = "";

        // Twitter
        public static final String TwitterConsumerKey = "PppCGHuU1jQWMrUSuCCfumUCB";
        public static final String TwitterConsumerSecret = "bY5Pc5EXfY1tzUFTwqh0HprRVSJ50sa83nJQIMIxvvlEsFODVY";
        public static final String TwitterAccessToken = "447438464-UiyStJVNB1uhtvzyzJmmeDs3hZ3yprowdzheWUeg";
        public static final String TwitterAccessTokenSecret = "fdZbHxMyXl97MKm84VwB9ceuuNbNuuDcx1hyc6qnoB5cN";

        // Parse
        public static final String ParseAppId = "oZsitstcYtm1yvwTkrMIpLtpE3SulFvdVb12rH8d";
        public static final String ParseClientKey = "A1lZKXHQWKUQogcfCCIFBg1cPbJ0dtekh2ghV6He";

        public static final String BugSenseKey = "a83a219d";
    }

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

    public static final class Keys{
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

        public static final String UserId = "user-id";

        public static final class ThirdPartyData{
            public static final String Name = "name";
            public static final String ImageURL = "profile_image_url";
            public static final String AccessToken = "accessToken";
            public static final String DisplayName = "displayName";
            public static final String EMail = "email";
        }
    }

    public static final class Time{
        public static final float BMinutes = 60.0f;
        public static final float BHours = 60.0f * BMinutes;
        public static final float BDays = 24.0f * BHours;
        public static final float BMonths = 30.0f * BDays;
        public static final float BYears = 12.0f * BMonths;
    }

    public static final class ImageProperties{

        public static final int MAX_WIDTH_IN_PX = 1920;
        public static final int MAX_HEIGHT_IN_PX = 2560;

        public static final int MAX_IMAGE_THUMBNAIL_SIZE = 600;
        public static final int PROFILE_PIC_THUMBNAIL_SIZE = 100;

        public static final int INITIALS_IMAGE_SIZE = 500;
        public static final float INITIALS_TEXT_SIZE = 150f;
    }
}
