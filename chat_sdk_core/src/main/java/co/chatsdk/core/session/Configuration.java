package co.chatsdk.core.session;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import co.chatsdk.core.utils.StringChecker;

/**
 * Created by ben on 10/17/17.
 */

public class Configuration {

    public WeakReference<Context> context;

    public boolean debug = false;

    // Twitter Login
    public String twitterKey;
    public String twitterSecret;

    // Google
    public String googleMapsApiKey;
    public String googleWebClientKey;

    // Firebase
    public String firebaseUrl;
    public String firebaseRootPath;
    public String firebaseStorageUrl;
    public String firebaseCloudMessagingServerKey;

    // XMPP
    public String xmppServiceName;
    public String xmppServiceHost;
    public int xmppServicePort;
    public String xmppSearchService;
    public String xmppResource;

    // Contact Book
    public String contactBookInviteContactEmailSubject;
    public String contactBookInviteContactEmailBody;
    public String contactBookInviteContactSmsBody;

    // Login
    public boolean anonymousLoginEnabled = true;
    public boolean facebookLoginEnabled = true;
    public boolean twitterLoginEnabled = true;
    public boolean googleLoginEnabled = true;

    // Message types
    public boolean imageMessagesEnabled = true;
    public boolean locationMessagesEnabled = true;

    // Chat options
    public boolean groupsEnabled = true;
    public boolean threadDetailsEnabled = true;
    public boolean publicRoomCreationEnabled = false;
    public boolean saveImagesToDirectory = false;

    public int maxMessagesToLoad = 30;
    public int imageMaxWidth = 1920;
    public int imageMaxHeight = 2560;
    public int imageMaxThumbnailDimension = 600;
    public int maxInboxNotificationLines = 7;

    public String defaultUserNamePrefix = "ChatSDK";
    public String defaultUserName = defaultUserNamePrefix + String.valueOf(new Random().nextInt(1000));
    public String imageDirectoryName = "ChatSDK";
    public String contactDeveloperEmailAddress = "support@chatsdk.co";
    public String contactDeveloperEmailSubject = "";
    public String contactDeveloperDialogTitle = "";
    public String defaultUserAvatarURL = "http://flathash.com/" + String.valueOf(new Random().nextInt(1000)) + ".png";

    public long readReceiptMaxAge = TimeUnit.DAYS.toMillis(7);

    public HashMap<String, Object> customProperties = new HashMap<>();

    public Object getCustomProperty (String key) {
        return customProperties.get(key);
    }

    public String fullFirebasePath () {
        return firebaseUrl + firebaseRootPath;
    }

    public boolean twitterLoginEnabled () {
        return !StringChecker.isNullOrEmpty(twitterKey) && !StringChecker.isNullOrEmpty(twitterSecret) && twitterLoginEnabled;
    }

    public boolean googleLoginEnabled () {
        return !StringChecker.isNullOrEmpty(googleWebClientKey) && googleLoginEnabled;
    }

    public boolean facebookLoginEnabled () {
        return facebookLoginEnabled;
    }

    public static class Builder {

        private Configuration  config;

        public Builder (Context context) {
            config = new Configuration();
            configureFromManifest(context);
            config.context = new WeakReference<>(context);
        }

        public Builder debugModeEnabled (boolean debug) {
            config.debug = debug;
            return this;
        }

        public Builder twitterLogin (String key, String secret) {
            config.twitterKey = key;
            config.twitterSecret = secret;
            return this;
        }

        public Builder googleLogin (String webClientKey) {
            config.googleWebClientKey = webClientKey;
            return this;
        }

        public Builder googleMaps (String mapsApiKey) {
            config.googleMapsApiKey = mapsApiKey;
            return this;
        }

        public Builder firebase (String url, String rootPath, String storageUrl, String cloudMessagingServerKey) {

            if(!url.substring(url.length() - 1).equals('/')) {
                url += "/";
            }
            if(!rootPath.substring(rootPath.length() - 1).equals('/')) {
                rootPath += "/";
            }

            config.firebaseUrl = url;
            config.firebaseRootPath = rootPath;
            config.firebaseStorageUrl = storageUrl;
            config.firebaseCloudMessagingServerKey = cloudMessagingServerKey;
            return this;
        }

        public Builder xmpp (String serviceName, String serviceHost, int port, String searchService, String resource) {
            config.xmppServiceName = serviceName;
            config.xmppServiceHost = serviceHost;
            config.xmppServicePort = port;
            config.xmppSearchService = searchService;
            config.xmppResource = resource;
            return this;
        }

        public Builder contactBook (String inviteEmailSubject, String inviteEmailBody, String inviteSmsBody) {
            config.contactBookInviteContactEmailSubject = inviteEmailSubject;
            config.contactBookInviteContactEmailBody = inviteEmailBody;
            config.contactBookInviteContactSmsBody = inviteSmsBody;
            return this;
        }

        public Builder publicRoomCreationEnabled (boolean value) {
            config.publicRoomCreationEnabled = value;
            return this;
        }

        public Builder anonymousLoginEnabled (boolean value) {
            config.anonymousLoginEnabled = value;
            return this;
        }

        public Builder facebookLoginEnabled (boolean value) {
            config.facebookLoginEnabled = value;
            return this;
        }

        public Builder twitterLoginEnabled (boolean value) {
            config.twitterLoginEnabled = value;
            return this;
        }

        public Builder googleLoginEnabled (boolean value) {
            config.googleLoginEnabled = value;
            return this;
        }

        public Builder imageMessagesEnabled (boolean value) {
            config.imageMessagesEnabled = value;
            return this;
        }

        public Builder locationMessagesEnabled (boolean value) {
            config.locationMessagesEnabled = value;
            return this;
        }

        public Builder groupsEnabled (boolean value) {
            config.groupsEnabled = value;
            return this;
        }

        public Builder threadDetailsEnabled (boolean value) {
            config.threadDetailsEnabled = value;
            return this;
        }

        public Builder saveImagesToDirectoryEnabled (boolean value) {
            config.saveImagesToDirectory = value;
            return this;
        }

        public Builder maxMessagesToLoad (int value) {
            config.maxMessagesToLoad = value;
            return this;
        }

        public Builder maxImageWidth (int value) {
            config.imageMaxWidth = value;
            return this;
        }

        public Builder maxImageHeight (int value) {
            config.imageMaxHeight = value;
            return this;
        }

        public Builder maxThumbnailDimensions (int value) {
            config.imageMaxThumbnailDimension = value;
            return this;
        }

        public Builder maxInboxNotificationLines (int value) {
            config.maxInboxNotificationLines = value;
            return this;
        }

        public Builder defaultUserNamePrefix (String value) {
            config.defaultUserNamePrefix = value;
            return this;
        }

        public Builder defaultUserName (String value) {
            config.defaultUserName = value;
            return this;
        }

        public Builder contactDeveloperEmailAddress (String value) {
            config.contactDeveloperEmailAddress = value;
            return this;
        }

        public Builder contactDeveloperEmailSubject (String value) {
            config.contactDeveloperEmailSubject = value;
            return this;
        }

        public Builder contactDeveloperDialogTitle (String value) {
            config.contactDeveloperDialogTitle = value;
            return this;
        }

        public Builder defaultUserAvatarUrl (String value) {
            config.defaultUserAvatarURL = value;
            return this;
        }

        public Builder imageDirectoryName (String value) {
            config.imageDirectoryName = value;
            return this;
        }

        public Builder readReceiptMaxAge (long millis) {
            config.readReceiptMaxAge = millis;
            return this;
        }

        public Builder addCustomSetting (String key, Object value) {
            config.customProperties.put(key, value);
            return this;
        }

        public Builder configureFromManifest (Context context) {
            try {
                ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                Bundle appBundle = ai.metaData;

                twitterLogin(
                        appBundle.getString("twitter_key"),
                        appBundle.getString("twitter_secret")
                );

                firebase(
                        appBundle.getString("firebase_url"),
                        appBundle.getString("firebase_root_path"),
                        appBundle.getString("firebase_storage_url"),
                        appBundle.getString("firebase_cloud_messaging_server_key")
                );

                String port = appBundle.getString("xmpp_service_port");

                xmpp(
                        appBundle.getString("xmpp_service_name"),
                        appBundle.getString("xmpp_service_host"),
                        port != null ? Integer.valueOf(port) : 0,
                        appBundle.getString("xmpp_search_service"),
                        appBundle.getString("xmpp_resource")
                );

                googleMaps(appBundle.getString("com.google.android.geo.API_KEY"));
                googleLogin(appBundle.getString("google_web_client_id"));

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return this;
        }

        public Configuration build () {
            return config;
        }

    }
}
