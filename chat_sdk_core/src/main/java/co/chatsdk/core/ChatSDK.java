package co.chatsdk.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.lang.ref.WeakReference;

import co.chatsdk.core.dao.DaoCore;
import timber.log.Timber;

/**
 * Created by ben on 9/5/17.
 */

public class ChatSDK {

    public static String Preferences = "chat_sdk_preferences";

    private static final ChatSDK instance = new ChatSDK();
    public WeakReference<Context> context;
    private Bundle appBundle;

    protected ChatSDK () {
    }

    private void setContext (Context context) {
        this.context = new WeakReference<>(context);
        try {
            ApplicationInfo ai = this.context.get().getPackageManager().getApplicationInfo(this.context.get().getPackageName(), PackageManager.GET_META_DATA);
            appBundle = ai.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        DaoCore.init(context);
    }

    public static ChatSDK initialize (Context context) {
        return initialize(context, false);
    }

    public static ChatSDK initialize (Context context, boolean debug) {
        shared().setContext(context);

//        if (debug) {
            Timber.plant(new Timber.DebugTree());
//        } else {
//            Timber.plant(new Timber.Tree());
//        }

        return shared();
    }

    public static ChatSDK shared () {
        return instance;
    }

    public String twitterKey () {
        return appBundle.getString("twitter_key");
    }

    public String twitterSecret () {
        return appBundle.getString("twitter_secret");
    }

    public String firebaseStorageURL () {
        return appBundle.getString("firebase_storage_url");
    }

    public String firebaseURL () {
        return appBundle.getString("firebase_url") + rootPath();
    }

    public String firebaseCloudMessagingServerKey () {
        return appBundle.getString("firebase_cloud_messaging_server_key");
    }

    public String rootPath () {
        return appBundle.getString("firebase_root_path");
    }

    public String googleMapsApiKey () {
        return appBundle.getString("google_maps_key");
    }
    public String googleWebClientID () {
        return appBundle.getString("google_web_client_id");
    }

    public String xmppServiceName () {
        return appBundle.getString("xmpp_service_name");
    }
    public String xmppServiceHost () {
        return appBundle.getString("xmpp_service_host");
    }
    public Integer xmppServicePort  () {
        return Integer.valueOf(appBundle.getString("xmpp_service_port"));
    }
    public String xmppSearchService () {
        return appBundle.getString("xmpp_search_service");
    }
    public String xmppResource () {
        return appBundle.getString("xmpp_resource");
    }
    public boolean xmppDebugModeEnabled () {
        return appBundle.getString("xmpp_debug_mode_enabled").equals("yes");
    }

    public String stringForKey (String key) {
        return appBundle.getString(key);
    }

    public SharedPreferences getPreferences () {
        return context.get().getSharedPreferences(Preferences, Context.MODE_PRIVATE);
    }

    public Context context () {
        return context.get();
    }

}
