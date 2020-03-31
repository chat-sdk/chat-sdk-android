package co.chatsdk.core.session;

import android.graphics.Color;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Random;

import co.chatsdk.core.R;
import co.chatsdk.core.interfaces.CrashHandler;

/**
 * Created by ben on 10/17/17.
 */

public class Config {

    public enum IdenticonType {
        FlatHash,
        RoboHash,
//        Avataaars,
        Gravatar,
//        Identicon,
//        Male,
//        Female,
//        Human,
//        Initials,
//        Robot,
//        Avataaar,
//        Jidenticon,
//        Gridy,
//        Code
    }

    HashMap<String, Object> remote = new HashMap<>();

    // Basic parameters
    public int messagesToLoadPerBatch = 30;
    public int contactsToLoadPerBatch = 20;

    // Testing
    public boolean debug = true;

    public String debugUsername = null;
    public String debugPassword = null;
    public CrashHandler crashHandler;

    // Google
    public String googleMapsApiKey;

    // Should we call disconnect when the app is in the background for more than 5 seconds?
    public boolean disconnectFromFirebaseWhenInBackground = true;

    // Push notification
    public int pushNotificationImageDefaultResourceId;
    public String pushNotificationAction;

    public boolean inboundPushHandlingEnabled = true;
    public boolean replyFromNotificationEnabled = true;

    // Should the client send the push or is a server script handling it?
    public boolean clientPushEnabled = false;

    // If this is true, then we will only send a push notification if the recipient is offline
    public boolean onlySendPushToOfflineUsers = false;
    public boolean showEmptyChats = true;

    // Login
    public boolean anonymousLoginEnabled = true;

    // Should we open a new thread with a user after the thread has been deleted?
    public boolean reuseDeleted1to1Threads = true;

    public int messageHistoryDownloadLimit = 30;
    public int messageDeletionListenerLimit = 30;

    public int imageMaxWidth = 1920;
    public int imageMaxHeight = 2560;
    public int imageMaxThumbnailDimension = 400;

    public boolean remoteConfigEnabled = false;

    public boolean publicChatAutoSubscriptionEnabled = false;

    public String identiconBaseURL;
    public IdenticonType identiconType;

    public String defaultNamePrefix = "ChatSDK";
    public String defaultName = null;

    public String storageDirectory = "ChatSDK";

    public String defaultUserAvatarURL = null;

    // If we are representing a location as a URL, which service should we use? Google Maps is the default
    public String locationURLRepresentation = "https://www.google.com/maps/search/?api=1&query=%f,%f";

    public String pushNotificationSound = "";
    public boolean showLocalNotifications = false;
    public int pushNotificationColor = Color.parseColor("#ff33b5e5");
    public boolean localPushNotificationsForPublicChatRoomsEnabled = false;

    // If this is set to true, we will simulate what happens when a push is recieved and the app
    // is in the killed state. This is useful to help us debug that process.
    public boolean backgroundPushTestModeEnabled = false;

    public int logoDrawableResourceID = R.drawable.ic_launcher_big;

    public HashMap<String, Object> customProperties = new HashMap<>();

    public Config() {
        updateDefaultName();
        setIdenticonType(IdenticonType.FlatHash);
    }

    public Object getCustomProperty(String key) {
        return customProperties.get(key);
    }

    public void setCustomProperty(String key, Object value) {
        customProperties.put(key, value);
    }

    public void updateDefaultName() {
        defaultName = defaultNamePrefix + String.valueOf(new Random().nextInt(1000));
    }

    public void updateRemoteConfig(HashMap<String, Object> config) {
        for (String key : config.keySet()) {
            setRemoteConfigValue(key, config.get(key));
        }
    }

    public Object getRemoteConfigValue(String key) {
        return remote.get(key);
    }

    public void setRemoteConfigValue(String key, Object value) {
        remote.put(key, value);
    }

    public Config setDebugModeEnabled(boolean debug) {
        this.debug = debug;
        if (debug) {
            Logger.getConfiguration().level(Level.DEBUG).activate();
        }
        return this;
    }

    public Config setDebugUsername(String username) {
        this.debugUsername = username;
        return this;
    }

    public Config setDebugPassword(String password) {
        this.debugPassword = password;
        return this;
    }

    public Config setGoogleMaps(String mapsApiKey) {
        this.googleMapsApiKey = mapsApiKey;
        return this;
    }

    public Config setReplyFromNotificationEnabled(boolean enabled) {
        this.replyFromNotificationEnabled = enabled;
        return this;
    }

    public Config setDisconnectFromFirebaseWhenInBackground(boolean disconnect) {
        this.disconnectFromFirebaseWhenInBackground = disconnect;
        return this;
    }

    public Config setAnonymousLoginEnabled(boolean value) {
        this.anonymousLoginEnabled = value;
        return this;
    }

    public Config setPushNotificationAction(String action) {
        this.pushNotificationAction = action;
        return this;
    }

    public Config setShowEmptyChats(boolean showEmpty) {
        this.showEmptyChats = showEmpty;
        return this;
    }

    public Config setInboundPushHandlingEnabled(boolean enabled) {
        this.inboundPushHandlingEnabled = enabled;
        return this;
    }

    public Config setReuseDeleted1to1Threads(boolean reuse) {
        this.reuseDeleted1to1Threads = reuse;
        return this;
    }

    /**
     * @deprecated use {@link #localPushNotificationsForPublicChatRoomsEnabled}
     */
    @Deprecated
    public Config setPushNotificationsForPublicChatRoomsEnabled(boolean value) {
        this.localPushNotificationsForPublicChatRoomsEnabled = value;
        return this;
    }

    public Config setLocalPushNotificationsForPublicChatRoomsEnabled(boolean value) {
        this.localPushNotificationsForPublicChatRoomsEnabled = value;
        return this;
    }

    public Config setRemoteConfigEnabled(boolean value) {
        this.remoteConfigEnabled = value;
        return this;
    }

    public Config setMessageHistoryDownloadLimit (int downloadLimit) {
        this.messageHistoryDownloadLimit = downloadLimit;
        return this;
    }

    public Config setMessageDeletionListenerLimit (int limit) {
        this.messageDeletionListenerLimit = limit;
        return this;
    }

    public Config setContactsToLoadPerBatch (int number) {
        this.contactsToLoadPerBatch = number;
        return this;
    }

    public Config setMessagesToLoadPerBatch(int number) {
        this.messagesToLoadPerBatch = number;
        return this;
    }

    public Config setBackgroundPushTestModeEnabled(boolean enabled) {
        this.backgroundPushTestModeEnabled = enabled;
        return this;
    }

    public Config setCrashHandler(CrashHandler handler) {
        this.crashHandler = handler;
        return this;
    }

    public Config setClientPushEnabled(boolean clientPushEnabled) {
        this.clientPushEnabled = clientPushEnabled;
        return this;
    }

    public Config setShowLocalNotifications(boolean show) {
        this.showLocalNotifications = show;
        return this;
    }

    public Config setMaxImageWidth(int value) {
        this.imageMaxWidth = value;
        return this;
    }

    public Config setMaxImageHeight(int value) {
        this.imageMaxHeight = value;
        return this;
    }

    public Config setMaxThumbnailDimensions(int value) {
        this.imageMaxThumbnailDimension = value;
        return this;
    }

    public Config setDefaultNamePrefix(String value) {
        this.defaultNamePrefix = value;
        this.updateDefaultName();
        return this;
    }

    public Config setDefaultName(String value) {
        this.defaultName = value;
        return this;
    }

    public Config setLogoDrawableResourceID(int resource) {
        this.logoDrawableResourceID = resource;
        return this;
    }

    public boolean setLogoIsSet () {
        return this.logoDrawableResourceID != R.drawable.ic_launcher_big;
    }

    public Config setDefaultUserAvatarUrl(String value) {
        this.defaultUserAvatarURL = value;
        return this;
    }

    public Config addCustomSetting(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    public Config setPushNotificationImageDefaultResourceId(int resourceId) {
        this.pushNotificationImageDefaultResourceId = resourceId;
        return this;
    }

    public Config setOnlySendPushToOfflineUsers(boolean value) {
        this.onlySendPushToOfflineUsers = value;
        return this;
    }

    public Config setPushNotificationSound(String sound) {
        this.pushNotificationSound = sound;
        return this;
    }

    public Config setPushNotificationColor(String hexColor) {
        this.pushNotificationColor = Color.parseColor(hexColor);
        return this;
    }

    public Config setPushNotificationColor(int color) {
        this.pushNotificationColor = color;
        return this;
    }

    public Config setLocationURLRepresentation (String representation) {
        this.locationURLRepresentation = representation;
        return this;
    }

    /**
     * @deprecated use {@link #setPublicChatAutoSubscriptionEnabled(boolean)}
     */
    @Deprecated
    public Config setRemoveUserFromPublicThreadOnExit (boolean remove) {
        this.publicChatAutoSubscriptionEnabled = !remove;
        return this;
    }

    public Config setPublicChatAutoSubscriptionEnabled(boolean enabled) {
        this.publicChatAutoSubscriptionEnabled = enabled;
        return this;
    }

    public Config setStorageDirectory(String value) {
        this.storageDirectory = value;
        return this;
    }

    public void setIdenticonBaseURL(String identiconBaseURL) {
        this.identiconBaseURL = identiconBaseURL;
    }

    public void setIdenticonType(IdenticonType type) {
        identiconType = type;
        switch (type) {
            case RoboHash:
                identiconBaseURL = "https://robohash.org/%s.png";
                break;
            case FlatHash:
            default:
                identiconBaseURL = "http://flathash.com/%s.png";
                break;
//            case Identicon:
//                identiconBaseURL = "https://avatars.dicebear.com/v2/identicon/%s.svg";
//            case Male:
//                identiconBaseURL = "https://avatars.dicebear.com/v2/male/%s.svg";
//            case Female:
//                identiconBaseURL = "https://avatars.dicebear.com/v2/female/%s.svg";
        }
    }

}
