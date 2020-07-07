package sdk.chat.core.session;

import android.graphics.Color;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sdk.chat.core.R;
import sdk.guru.common.BaseConfig;

/**
 * Created by ben on 10/17/17.
 */

public class Config<T> extends BaseConfig<T> {

    HashMap<String, Object> remote = new HashMap<>();

    // Basic parameters
    public int messagesToLoadPerBatch = 30;
    public int userSearchLimit = 20;

    // Testing
    public boolean debug = true;

    public String debugUsername = null;
    public String debugPassword = null;

    // Google
    public String googleMapsApiKey;

    // Rooms that are older than this will be hidden
    // Zero is infinite lifetime
    // Default - 7 days
    public long publicChatRoomLifetimeMinutes = TimeUnit.DAYS.toMinutes(7);
    public long privateChatRoomLifetimeMinutes = 0;

    // Push notification
    public int pushNotificationImageDefaultResourceId;
    public String pushNotificationAction;

    public boolean inboundPushHandlingEnabled = true;
    public boolean replyFromNotificationEnabled = true;
    public boolean markAsReadFromNotificationEnabled = true;

    // Should the client send the push or is a server script handling it?
    public boolean clientPushEnabled = false;

    // If this is true, then we will only send a push notification if the recipient is offline
    public boolean onlySendPushToOfflineUsers = false;
    public boolean showEmptyChats = true;

    // Login
    public boolean anonymousLoginEnabled = true;

    public boolean rolesEnabled = true;

    // Should we open a new thread with a user after the thread has been deleted?
    public boolean reuseDeleted1to1Threads = true;

    public int messageDeletionListenerLimit = 30;

    public int imageMaxWidth = 1920;
    public int imageMaxHeight = 2560;
    public int imageMaxThumbnailDimension = 400;

    public boolean remoteConfigEnabled = false;

    public boolean publicChatAutoSubscriptionEnabled = false;

    public String identiconBaseURL = "http://identicon.sdk.chat?value=%s&size=400.png";

    public String storageDirectory = "ChatSDK";

    public String defaultUserAvatarURL = null;

    public boolean sendSystemMessageWhenRoleChanges = true;

    // If we are representing a location as a URL, which service should we use? Google Maps is the default
    public String locationURLRepresentation = "https://www.google.com/maps/search/?api=1&query=%f,%f";

    public String pushNotificationSound = "";
    public int pushNotificationColor = Color.parseColor("#ff33b5e5");
    public boolean localPushNotificationsForPublicChatRoomsEnabled = false;

    public boolean showLocalNotifications = true;

    // If this is set to true, we will simulate what happens when a push is recieved and the app
    // is in the killed state. This is useful to help us debug that process.
    public boolean backgroundPushTestModeEnabled = false;

    public int logoDrawableResourceID = R.drawable.ic_launcher_big;

    public HashMap<String, Object> customProperties = new HashMap<>();

    public boolean disablePresence = false;

//    public boolean disconnectFromServerWhenInBackground = true;

    public Config(T onBuild) {
        super(onBuild);
    }

    public Object getCustomProperty(String key) {
        return customProperties.get(key);
    }

    public void setCustomProperty(String key, Object value) {
        customProperties.put(key, value);
    }

    public void setRemoteConfig(Map<String, Object> config) {
        remote.clear();
        for (String key : config.keySet()) {
            setRemoteConfigValue(key, config.get(key));
        }
    }

    public void clearRemoteConfig() {
        remote.clear();
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

    /**
     * Choose a default username which will populate the login activity. This can
     * save you from having to type it during testing
     * @param username
     * @return
     */
    public Config<T> setDebugUsername(String username) {
        this.debugUsername = username;
        return this;
    }

    /**
     * Password for default username
     * @param password
     * @return
     */
    public Config<T> setDebugPassword(String password) {
        this.debugPassword = password;
        return this;
    }

    /**
     * Set the Google maps API key. This is necessary for location messages
     * @param mapsApiKey
     * @return
     */
    public Config<T> setGoogleMaps(String mapsApiKey) {
        this.googleMapsApiKey = mapsApiKey;
        return this;
    }

    /**
     * Allow the user to reply directly from a push notification
     * @param enabled
     * @return
     */
    public Config<T> setReplyFromNotificationEnabled(boolean enabled) {
        this.replyFromNotificationEnabled = enabled;
        return this;
    }

    /**
     * Allow the user to mark as read from a push notification
     * @param enabled
     * @return
     */
    public Config<T> setMarkAsReadFromNotificationEnabled(boolean enabled) {
        this.markAsReadFromNotificationEnabled = enabled;
        return this;
    }
    /**
     * Allow users to login anonymously
     * @param value
     * @return
     */
    public Config<T> setAnonymousLoginEnabled(boolean value) {
        this.anonymousLoginEnabled = value;
        return this;
    }

    /**
     * Set a default action for a push payload i.e.
     * {
     *     action: "your.custom.action"
     * }
     * @param action
     * @return
     */
    public Config<T> setPushNotificationAction(String action) {
        this.pushNotificationAction = action;
        return this;
    }

    /**
     * Show empty conversations
     * @param showEmpty
     * @return
     */
    public Config<T> setShowEmptyChats(boolean showEmpty) {
        this.showEmptyChats = showEmpty;
        return this;
    }

    /**
     * If set to false, the Chat SDK will not handle incoming push notifications
     * @param enabled
     * @return
     */
    public Config<T> setInboundPushHandlingEnabled(boolean enabled) {
        this.inboundPushHandlingEnabled = enabled;
        return this;
    }

    /**
     * If a user deletes a 1-to-1 conversation, should this object be reused?
     * If not, a new thread will be created each time
     * @param reuse
     * @return
     */
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

    /**
     * Receive local push notifications for public chat rooms
     * @param value
     * @return
     */
    public Config<T> setLocalPushNotificationsForPublicChatRoomsEnabled(boolean value) {
        this.localPushNotificationsForPublicChatRoomsEnabled = value;
        return this;
    }

    /**
     * Enable remote config. When enabled the app will listen to the /root-path/config path
     * in Firebase and the values will be made available using
     * ChatSDK.config().getRemoteConfigValue("key")
     * @param value
     * @return
     */
    public Config<T> setRemoteConfigEnabled(boolean value) {
        this.remoteConfigEnabled = value;
        return this;
    }

    /**
     * How many messages back can we delete. In Firebase we can't only add a deletion
     * listener, so this is an additional event listener. That will cause this
     * number of messages to be re-downloaded when the app starts. 
     * @param limit
     * @return
     */
    public Config<T> setMessageDeletionListenerLimit (int limit) {
        this.messageDeletionListenerLimit = limit;
        return this;
    }

    /**
     * Max users to load when we run a search
     * @param number
     * @return
     */
    public Config<T> setUserSearchLimit(int number) {
        this.userSearchLimit = number;
        return this;
    }

    /**
     * When we are scrolling up, how many messages to lazy load per batch
     * @param number
     * @return
     */
    public Config<T> setMessagesToLoadPerBatch(int number) {
        this.messagesToLoadPerBatch = number;
        return this;
    }

    /**
     * Used for testing
     * @param enabled
     * @return
     */
    public Config<T> setBackgroundPushTestModeEnabled(boolean enabled) {
        this.backgroundPushTestModeEnabled = enabled;
        return this;
    }

    /**
     * Should the client try to send the push notifications? This should be set to true
     * when using XMPP with Firebase Cloud Messaging
     * @param clientPushEnabled
     * @return
     */
    public Config<T> setClientPushEnabled(boolean clientPushEnabled) {
        this.clientPushEnabled = clientPushEnabled;
        return this;
    }

    /**
     * Max message image width
     * @param value
     * @return
     */
    public Config<T> setMaxImageWidth(int value) {
        this.imageMaxWidth = value;
        return this;
    }

    /**
     * Max message image height
     * @param value
     * @return
     */
    public Config<T> setMaxImageHeight(int value) {
        this.imageMaxHeight = value;
        return this;
    }

    /**
     * Max thumbnail dimension - used for avatar images
     * @param value
     * @return
     */
    public Config<T> setMaxThumbnailDimensions(int value) {
        this.imageMaxThumbnailDimension = value;
        return this;
    }

    /**
     * Set the logo to show on the login and splash screens
     * @param resource
     * @return
     */
    public Config<T> setLogoDrawableResourceID(int resource) {
        this.logoDrawableResourceID = resource;
        return this;
    }

    /**
     * URL of default user profile image
     * @param value
     * @return
     */
    public Config<T> setDefaultUserAvatarUrl(String value) {
        this.defaultUserAvatarURL = value;
        return this;
    }

    public Config<T> addCustomSetting(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    /**
     * Image used for push notification
     * @param resourceId
     * @return
     */
    public Config<T> setPushNotificationImageDefaultResourceId(int resourceId) {
        this.pushNotificationImageDefaultResourceId = resourceId;
        return this;
    }

    /**
     * Only send push notifications if recipient is offline
     * only used when client push is enabled
     * @param value
     * @return
     */
    public Config<T> setOnlySendPushToOfflineUsers(boolean value) {
        this.onlySendPushToOfflineUsers = value;
        return this;
    }

    /**
     * Provide a sound to play when a push notification is received
     * @param sound
     * @return
     */
    public Config<T> setPushNotificationSound(String sound) {
        this.pushNotificationSound = sound;
        return this;
    }

    /**
     * Push notification color
     * @param hexColor
     * @return
     */
    public Config<T> setPushNotificationColor(String hexColor) {
        this.pushNotificationColor = Color.parseColor(hexColor);
        return this;
    }

    public Config<T> setPushNotificationColor(int color) {
        this.pushNotificationColor = color;
        return this;
    }

    /**
     * Formatted URL which can be used to display a location
     * i.e. "https://www.google.com/maps/search/?api=1&query=%f,%f";
     * @param representation
     * @return
     */
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

    /**
     * When enabled, if a user joins a public chat, when they leave, they will
     * not be removed from the user list. So they can continue to receive notifications
     * @param enabled
     * @return
     */
    public Config<T> setPublicChatAutoSubscriptionEnabled(boolean enabled) {
        this.publicChatAutoSubscriptionEnabled = enabled;
        return this;
    }

    /**
     * Name of file storage directory
     * @param value
     * @return
     */
    public Config<T> setStorageDirectory(String value) {
        this.storageDirectory = value;
        return this;
    }

    /**
     * Provide a URL to generate an avatar
     * This URL should provide a link to a PNG to be used
     * it should be the form http://someurl.com/%s.png
     * %s will be replaced by the user's entity ID
     * @param identiconBaseURL
     * @return
     */
    public Config<T> setIdenticonBaseURL(String identiconBaseURL) {
        this.identiconBaseURL = identiconBaseURL;
        return this;
    }

    /**
     * How long will a public chat exist for. 0 is forever
     * this can stop getting hundreds of new public chat rooms
     * created by users
     * @param minutes
     * @return
     */
    public Config<T> setPublicChatRoomLifetimeMinutes (long minutes) {
        this.publicChatRoomLifetimeMinutes = minutes;
        return this;
    }

    /**
     * How long will a private chat exist for. 0 is forever
     * this can stop getting hundreds of new public chat rooms
     * created by users
     * @param minutes
     * @return
     */
    public Config<T> setPrivateChatRoomLifetimeMinutes (long minutes) {
        this.privateChatRoomLifetimeMinutes = minutes;
        return this;
    }

    /**
     * Disable presence
     * @param disablePresence
     * @return
     */
    public Config<T> setDisablePresence(boolean disablePresence) {
        this.disablePresence = disablePresence;
        return this;
    }

    /**
     * Notify the user that their roles has changed with a system message
     * @param value
     * @return
     */
    public Config<T> setSendSystemMessageWhenRoleChanges(boolean value) {
        this.sendSystemMessageWhenRoleChanges = value;
        return this;
    }

    /**
     * Enable or disable public chat roles
     * @param value
     * @return
     */
    public Config<T> setRolesEnabled(boolean value) {
        this.rolesEnabled = value;
        return this;
    }

    /**
     * Show local notifications or not
     * @param value
     * @return
     */
    public Config<T> setShowLocalNotifications(boolean value) {
        this.showLocalNotifications = value;
        return this;
    }

}
