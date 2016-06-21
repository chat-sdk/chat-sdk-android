/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;

import com.braunster.chatsdk.BuildConfig;

import java.util.Random;

/**
 * Created by braunster on 23/06/14.
 */
public class BDefines {

    /** This is the root path of all the app data,
     *  This is helpful if you want to test new behavior and don't want to infect all the old thread. */
    public static final String BRootPath = "chat-sdk-v3/";

    /**
     * The version name of the app, Here we are using the name from the BuildConfig.
     * The best way to use it would be to change the value of this veriable in the application object
     * to your own version name.
     **/
    public static String BAppVersion = BuildConfig.VERSION_NAME;

    /** The server url that is used to do all the API calls.*/
    public static String ServerUrl = "https://chat-sdk-v3.firebaseio.com/" + BRootPath;

    /**
     * The name of the app image directory that will be seen in the phone image galley
     * */
    public static final String ImageDirName = "AndroidChatSDK";

    public static final String BCloudImageToken = "skbb48";

    public static String getDefaultImageUrl(String url, int width, int height){
        return String.format("http://%s.cloudimage.io/s/crop/%sx%s/%s", BCloudImageToken, width, height, url);
    }

    public static String getDefaultUserName(){
        return "Chatcat" + String.valueOf(new Random().nextInt(1000) + 1);
    }

    /** Each type get his own prefix by using the private constructor.
     * This is the place to change the prefix if wanted.*/
    public static final class BAccountType{
        public static final int Password = 1;
        public static final int Facebook = 2;
        public static final int Twitter = 4;
        public static final int Google = 3;
        public static final int Anonymous = 5;
        public static final int Register = 6;
        public static final int Custom = 7;
    }

    /**
     * If true anonymous login is possible.
     * */
    public static final boolean AnonymousLoginEnabled = true;

    /**
     * The initials that will be used to create initials image for anonymous user and user without names.
     * @see com.braunster.chatsdk.Utils.ImageUtils#getInitialsBitmap(int, int, String)
     * */
    public static final String InitialsForAnonymous = "AN";

    /**
     * divide tat is used to divide b */
    public static final String DIVIDER = ",";

    /**
     * Vibration Duration in Millis of the notifications.
     * */
    public static final int VIBRATION_DURATION = 300;

    /**
     * Amount of messages that will be pulled for a thread from local db or from server.
     * */
    public static final int MAX_MESSAGES_TO_PULL = 30;

    /**
     * If true user phone number will be index if not empty.
     *
     * This is not final so you could set this up each time the user enters the app,
     * This way you could make an option to enable this option in your settings screen if the user does not want to index his phone number.
     *
     * */
    public static boolean IndexUserPhoneNumber = true;

    public static final String ContactDeveloper_Email = "";
    public static final String ContactDeveloper_Subject = "Report: ";
    public static final String ContactDeveloper_DialogTitle = "Contact Developer";

    /**
     * Currently there is no reason to disable following but maybe in the future it would be needed to keep track of it.
     * */
    public static boolean EnableFollowers = true;

    public static final class Defaults{
        public static final String MessageColor = "0.635294 0.552941 0.686275 1";
        public static final String BubbleDefaultColor = "#27ae60";
        public static final String BubbleDefaultPressedColor = "#3498db";
        public static final int SDKExitMode = Exit.EXIT_MODE_DOUBLE_BACK;
    }

    public static final class Exit {
        public static final int EXIT_MODE_NONE = 1990;
        public static final int EXIT_MODE_DOUBLE_BACK = 1991;
        public static final int EXIT_MODE_DIALOG = 1992;

        public static final int DOUBLE_CLICK_INTERVAL = 2000;
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
        public static final String PushEnabled = "push-enabled";
    }

    public static final class Keys{
        /*Metadata*/
        public static final String BEmail = "email";
        public static final String BKey = "key";
        public static final String BValue = "value";
        public static final String BPhone = "phone";
        public static final String BPictureURL = "pictureURL";
        public static final String BPictureURLThumbnail = "pictureURLThumbnail";

        public static final String BLastMessageAdded = "last-message-added";
        public static final String BUserFirebaseId = "user-firebase-id";
        public static final String BColor = "color";
        public static final String BName = "name";
        public static final String BNull = "null";
        public static final String BCreationDate = "creation-date";
        public static final String BPayload = "payload";
        public static final String BType = "type";
        public static final String BOnline = "online";
        public static final String BDate = "date";
        public static final String BLastOnline = "last-online";
        public static final String BVersion = "version";
        public static final String BMeta = "meta";

        public static final String BImageUrl = "image-url";
        public static final String BCreatorEntityId = "creator-firebase-id";
        public static final String BDeleted = "deleted";
        public static final String BLeaved = "leaved";
        public static final String UserId = "user-id";

        // User details
        public static final String BGender = "gender";
        public static final String BCountry = "country";
        public static final String BLocation = "location";
        public static final String BDateOfBirth= "date-of-birth";
        public static final String BStatus = "status";

        public static final class ThirdPartyData{
            public static final String ID = "id";
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

    public static final class ProviderString{
        public static final String Anonymous = "anonymous";
        public static final String Password = "password";
        public static final String Facebook = "facebook";
        public static final String Twitter = "twitter";
        public static final String Google = "google";
        public static final String Custom = "custom";
    }

    public static final class ProviderInt{
        public static final int Password = 1;
        public static final int Facebook = 2;
        public static final int Google = 3;
        public static final int Twitter = 4;
        public static final int Anonymous = 5;
        public static final int Custom = 6;
    }

    public static final class Options{
        /**
         * if true option to send location will be available, If the google maps key is empty it wont be available.
         **/
        public static boolean LocationEnabled = true;

        /**
         * if true option to open group chats and add users to chat will be available.
         **/
        public static boolean GroupEnabled = true;

        /**
         * if true the option to open the thread details would be visible in the chat activity.
         **/
        public static boolean ThreadDetailsEnabled = true;

        /**
         * If true images opened in the chat activity will be saved to the user image gallery
         * under the name assigned in the ImageDirName, Also images you pick from gallery and send will be saved.
         * If this is disabled images will be saved to the app cache directory. 
         *
         * The message list adapter will first try to load images from internal storage, Enabling this will get
         * Better performance loading images and reduce network use.
         *
         * @see #ImageDirName
         * */
        public static boolean SaveImagesToDir = true;

        /**
         * The maximum amounts of lines that will be shown in the notification for incoming messages.
         *
         * Seems to have a problem when showing more then seven lines in lollipop.
         **/
        public static final int MaxInboxNotificationLines = 7;

        /**
         * The string that will be used to format the selected date of birth.
         **/
        public static final String DateOfBirthFormat = "dd/MM/yyyy";
    }

    public static final class MessageDateFormat {
        public static final String YearOldMessageFormat = "MM/yy";
        public static final String DayOldFormat = "MMM dd";
        public static final String LessThenDayFormat = "HH:mm";
    }

    public static final int MESSAGE_NOTIFICATION_ID = 1001;
    public static final int FOLLOWER_NOTIFICATION_ID = 1002;

}
