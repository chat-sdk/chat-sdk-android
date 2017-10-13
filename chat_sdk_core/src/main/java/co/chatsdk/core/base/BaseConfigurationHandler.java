package co.chatsdk.core.base;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import co.chatsdk.core.handlers.ConfigurationHandler;

/**
 * Created by ben on 10/3/17.
 */

public class BaseConfigurationHandler implements ConfigurationHandler {

    /*******************
     * Boolean Keys
     */

    public static final String AnonymousLoginEnabled = "AnonymousLoginEnabled";
    public static final String FacebookLoginEnabled = "FacebookLoginEnabled";
    public static final String TwitterLoginEnabled = "TwitterLoginEnabled";
    public static final String GoogleLoginEnabled = "GoogleLoginEnabled";
    public static final String ImageMessagesEnabled = "ImageMessagesEnabled";
    public static final String LocationMessagesEnabled = "LocationMessagesEnabled";
    /**
     * if true option to open group chats and add users to chat will be available.
     **/
    public static final String GroupsEnabled = "GroupsEnabled";
    public static final String ThreadDetailsEnabled = "ThreadDetailsEnabled";

    /**
     * If true images opened in the chat activity will be saved to the user image gallery
     * under the name assigned in the ImageDirName, Also images you pick from gallery and send will be saved.
     * If this is disabled images will be saved to the app cache directory.
     *
     * The message list adapter will first try to load images from internal storage, Enabling this will get
     * Better performance loading images and reduce network use.
     *
     **/
    public static final String SaveImageToDirectory = "SaveImageToDirectory";

    /*******************
     * Integer Keys
     */

    // Number of messages that will be pulled for a thread from local db or from server.
    public static final String MaxMessagesToLoad = "MaxMessagesToLoad";
    public static final String ImageMaxWidth = "ImageMaxWidth";
    public static final String ImageMaxHeight = "ImageMaxHeight";
    public static final String ImageMaxThumbnailDimension = "ImageMaxThumbnailDimension";

    /**
     * The maximum amounts of lines that will be shown in the notification for incoming messages.
     * Seems to have a problem when showing more then seven lines in lollipop.
     **/
    public static final String MaxInboxNotificationLines = "MaxInboxNotificationLines";

    /*******************
     * String Keys
     */

    public static final String UserNamePrefix = "UserNamePrefix";
    public static final String DefaultUserName = "DefaultUserName";
    public static final String ContactDeveloperEmailAddress = "ContactDeveloperEmailAddress";
    public static final String ContactDeveloperEmailSubject = "ContactDeveloperEmailSubject";
    public static final String ContactDeveloperDialogTitle = "ContactDeveloperDialogTitle";
    public static final String DefaultUserAvatarURL = "DefaultUserAvatarURL";

    // The name of the app image directory that will be seen in the phone image galley
    public static final String ImageDirectoryName = "ImageDirectoryName";

    /*******************
     * Long Keys
     */
    public static final String ReadReceiptMaxAge = "ReadReceiptMaxAge";

    @Override
    public boolean booleanForKey(String key) {
        switch (key) {
            case AnonymousLoginEnabled:
                return true;
            case FacebookLoginEnabled:
                return true;
            case TwitterLoginEnabled:
                return true;
            case GoogleLoginEnabled:
                return true;
            case ImageMessagesEnabled:
                return true;
            case LocationMessagesEnabled:
                return true;
            case GroupsEnabled:
                return true;
            case ThreadDetailsEnabled:
                return true;
            case SaveImageToDirectory:
                return false;
            default:
                return false;
        }
    }

    @Override
    public int integerForKey(String key) {
        switch (key) {
            case MaxMessagesToLoad:
                return 30;
            case ImageMaxWidth:
                return 1920;
            case ImageMaxHeight:
                return 2560;
            case ImageMaxThumbnailDimension:
                return 600;
            case MaxInboxNotificationLines:
                return 7;
            default:
                return -1;
        }
    }

    @Override
    public long longForKey (String key) {
        switch (key) {
            case ReadReceiptMaxAge:
                return TimeUnit.DAYS.toMillis(7);
            default:
                return -1;
        }
    }

    @Override
    public float floatForKey(String key) {
        switch (key) {
            default:
                return -1.0f;
        }
    }

    @Override
    public String stringForKey(String key) {
        switch (key) {
            case UserNamePrefix:
                return "ChatSDK";

            case DefaultUserName:
                return stringForKey(UserNamePrefix) + String.valueOf(new Random().nextInt(1000));

            case ImageDirectoryName:
                return "ChatSDK";

            case ContactDeveloperEmailAddress:
                return "support@chatsdk.co";

            case ContactDeveloperEmailSubject:
                return "";

            case ContactDeveloperDialogTitle:
                return "";

            case DefaultUserAvatarURL:
                return "http://flathash.com/" + String.valueOf(new Random().nextInt(1000)) + ".png";

            default:
                return "";
        }
    }
}
