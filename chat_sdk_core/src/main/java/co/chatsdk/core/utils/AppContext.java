package co.chatsdk.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.ref.WeakReference;

import wanderingdevelopment.tk.chatsdkcore.R;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

public class AppContext {

    private static final AppContext instance = new AppContext();
    private static final String CHAT_SDK_SHRED_PREFS = "chat_sdk_prefs";

    private WeakReference<Context> context;
    private String googleMapsAPIKey;

    protected AppContext () {

    }
    public static AppContext shared () {
        return instance;
    }

    public void setContext (Context context) {
        this.context = new WeakReference<>(context.getApplicationContext());
    }

    public Context context () {
        return context.get();
    }

    @Deprecated
    public void setGoogleMapsAPIKey (String key) {
        googleMapsAPIKey = key;
    }

    public String googleMapsAPIKey () {
        return googleMapsAPIKey;
    }

    public SharedPreferences getPreferences () {
        return context.get().getSharedPreferences(CHAT_SDK_SHRED_PREFS, Context.MODE_PRIVATE);
    }

}
