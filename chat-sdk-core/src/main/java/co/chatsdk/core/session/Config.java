package co.chatsdk.core.session;

import android.graphics.Color;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import co.chatsdk.core.R;
import co.chatsdk.core.interfaces.CrashHandler;
import sdk.guru.common.BaseConfig;

/**
 * Created by ben on 10/17/17.
 */

public class Config<T> extends BaseConfig<T> {

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

    // Rooms that are older than this will be hidden
    // Zero is infinite lifetime
    // Default - 7 days
    public int publicChatRoomLifetimeMinutes = 60 * 24 * 7;

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

    public boolean disablePresence = false;

    public Config(T onBuild) {
        super(onBuild);

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
        defaultName = defaultNamePrefix + new Random().nextInt(1000);
    }

    public void updateRemoteConfig(Map<String, Object> config) {
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

    public Config<T> setDebugModeEnabled(boolean debug) {
        this.debug = debug;
        if (debug) {
            Logger.getConfiguration().level(Level.DEBUG).activate();
        }
        return this;
    }

    public Config<T> setDebugUsername(String username) {
        this.debugUsername = username;
        return this;
    }

    public Config<T> setDebugPassword(String password) {
        this.debugPassword = password;
        return this;
    }

    public Config<T> setGoogleMaps(String mapsApiKey) {
        this.googleMapsApiKey = mapsApiKey;
        return this;
    }

    public Config<T> setReplyFromNotificationEnabled(boolean enabled) {
        this.replyFromNotificationEnabled = enabled;
        return this;
    }

    public Config<T> setDisconnectFromFirebaseWhenInBackground(boolean disconnect) {
        this.disconnectFromFirebaseWhenInBackground = disconnect;
        return this;
    }

    public Config<T> setAnonymousLoginEnabled(boolean value) {
        this.anonymousLoginEnabled = value;
        return this;
    }

    public Config<T> setPushNotificationAction(String action) {
        this.pushNotificationAction = action;
        return this;
    }

    public Config<T> setShowEmptyChats(boolean showEmpty) {
        this.showEmptyChats = showEmpty;
        return this;
    }

    public Config<T> setInboundPushHandlingEnabled(boolean enabled) {
        this.inboundPushHandlingEnabled = enabled;
        return this;
    }

    public Config<T> setReuseDeleted1to1Threads(boolean reuse) {
        this.reuseDeleted1to1Threads = reuse;
        return this;
    }

    /**
     * @deprecated use {@link #localPushNotificationsForPublicChatRoomsEnabled}
     */
    @Deprecated
    public Config<T> setPushNotificationsForPublicChatRoomsEnabled(boolean value) {
        this.localPushNotificationsForPublicChatRoomsEnabled = value;
        return this;
    }

    public Config<T> setLocalPushNotificationsForPublicChatRoomsEnabled(boolean value) {
        this.localPushNotificationsForPublicChatRoomsEnabled = value;
        return this;
    }

    public Config<T> setRemoteConfigEnabled(boolean value) {
        this.remoteConfigEnabled = value;
        return this;
    }

    public Config<T> setMessageHistoryDownloadLimit (int downloadLimit) {
        this.messageHistoryDownloadLimit = downloadLimit;
        return this;
    }

    public Config<T> setMessageDeletionListenerLimit (int limit) {
        this.messageDeletionListenerLimit = limit;
        return this;
    }

    public Config<T> setContactsToLoadPerBatch (int number) {
        this.contactsToLoadPerBatch = number;
        return this;
    }

    public Config<T> setMessagesToLoadPerBatch(int number) {
        this.messagesToLoadPerBatch = number;
        return this;
    }

    public Config<T> setBackgroundPushTestModeEnabled(boolean enabled) {
        this.backgroundPushTestModeEnabled = enabled;
        return this;
    }

    public Config<T> setCrashHandler(CrashHandler handler) {
        this.crashHandler = handler;
        return this;
    }

    public Config<T> setClientPushEnabled(boolean clientPushEnabled) {
        this.clientPushEnabled = clientPushEnabled;
        return this;
    }

    public Config<T> setShowLocalNotifications(boolean show) {
        this.showLocalNotifications = show;
        return this;
    }

    public Config<T> setMaxImageWidth(int value) {
        this.imageMaxWidth = value;
        return this;
    }

    public Config<T> setMaxImageHeight(int value) {
        this.imageMaxHeight = value;
        return this;
    }

    public Config<T> setMaxThumbnailDimensions(int value) {
        this.imageMaxThumbnailDimension = value;
        return this;
    }

    public Config<T> setDefaultNamePrefix(String value) {
        this.defaultNamePrefix = value;
        this.updateDefaultName();
        return this;
    }

    public Config<T> setDefaultName(String value) {
        this.defaultName = value;
        return this;
    }

    public Config<T> setLogoDrawableResourceID(int resource) {
        this.logoDrawableResourceID = resource;
        return this;
    }

    public boolean setLogoIsSet () {
        return this.logoDrawableResourceID != R.drawable.ic_launcher_big;
    }

    public Config<T> setDefaultUserAvatarUrl(String value) {
        this.defaultUserAvatarURL = value;
        return this;
    }

    public Config<T> addCustomSetting(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    public Config<T> setPushNotificationImageDefaultResourceId(int resourceId) {
        this.pushNotificationImageDefaultResourceId = resourceId;
        return this;
    }

    public Config<T> setOnlySendPushToOfflineUsers(boolean value) {
        this.onlySendPushToOfflineUsers = value;
        return this;
    }

    public Config<T> setPushNotificationSound(String sound) {
        this.pushNotificationSound = sound;
        return this;
    }

    public Config<T> setPushNotificationColor(String hexColor) {
        this.pushNotificationColor = Color.parseColor(hexColor);
        return this;
    }

    public Config<T> setPushNotificationColor(int color) {
        this.pushNotificationColor = color;
        return this;
    }

    public Config<T> setLocationURLRepresentation (String representation) {
        this.locationURLRepresentation = representation;
        return this;
    }

    /**
     * @deprecated use {@link #setPublicChatAutoSubscriptionEnabled(boolean)}
     */
    @Deprecated
    public Config<T> setRemoveUserFromPublicThreadOnExit (boolean remove) {
        this.publicChatAutoSubscriptionEnabled = !remove;
        return this;
    }

    public Config<T> setPublicChatAutoSubscriptionEnabled(boolean enabled) {
        this.publicChatAutoSubscriptionEnabled = enabled;
        return this;
    }

    public Config<T> setStorageDirectory(String value) {
        this.storageDirectory = value;
        return this;
    }

    public Config<T> setIdenticonBaseURL(String identiconBaseURL) {
        this.identiconBaseURL = identiconBaseURL;
        return this;
    }

    public Config<T> setIdenticonType(IdenticonType type) {
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
        return this;
    }

    public Config<T> setPublicChatRoomLifetimeMinutes (int minutes) {
        this.publicChatRoomLifetimeMinutes = minutes;
        return this;
    }

    public Config<T> setDisablePresence(boolean disablePresence) {
        this.disablePresence = disablePresence;
        return this;
    }

}
