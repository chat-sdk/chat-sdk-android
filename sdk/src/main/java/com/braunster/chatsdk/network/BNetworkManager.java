package com.braunster.chatsdk.network;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.bugsense.trace.BugSenseHandler;
import com.firebase.client.Firebase;
import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by itzik on 6/8/2014.
 */
public class BNetworkManager {

    private static final String TAG = BNetworkManager.class.getSimpleName();
    private static final boolean DEBUG = Debug.BNetworkManager;

    private static final String CHAT_SDK_SHRED_PREFS = "ChatSDK_Prefs";
    public static final boolean BUGSENSE_ENABLED = false, PushEnabledDefaultValue = true;

    public static SharedPreferences preferences, userPreferences;

    private static BNetworkManager instance;

    private AbstractNetworkAdapter networkAdapter;

    private static Context context;

    public static void init(Context ctx){
        context = ctx;

        Firebase.setAndroidContext(context);

        preferences = ctx.getSharedPreferences(CHAT_SDK_SHRED_PREFS, Context.MODE_PRIVATE);
        VolleyUtils.init(ctx);
        DaoCore.init(ctx);

        // Parse init
        Parse.initialize(ctx, BDefines.APIs.ParseAppId, BDefines.APIs.ParseClientKey);
        ParseInstallation.getCurrentInstallation().saveInBackground();
//        PushService.setDefaultPushCallback(ctx, MainActivity.class);

        BFacebookManager.init(BDefines.APIs.FacebookAppId, ctx);

        //Bug Sense
        int adb;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
            adb = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        else adb = Settings.Global.getInt(ctx.getContentResolver(), Settings.Global.ADB_ENABLED, 0);

        if (adb == 0 || BNetworkManager.BUGSENSE_ENABLED) {
            BugSenseHandler.initAndStartSession(ctx, BDefines.APIs.BugSenseKey);
            BugSenseHandler.addCrashExtraData("Version", ctx.getResources().getString(R.string.chat_sdk_version_name));
        }
    }
    public static BNetworkManager sharedManager(){
//        if (DEBUG) Log.v(TAG, "sharedManager");
        if (instance == null) {
            instance = new BNetworkManager();
        }
        return instance;
    }

    public void setNetworkAdapter(AbstractNetworkAdapter networkAdapter) {
        this.networkAdapter = networkAdapter;
    }

    public AbstractNetworkAdapter getNetworkAdapter() {
        return networkAdapter;
    }

    /** Always safe to call*/
    public static SharedPreferences getUserPrefs(String entityId){
        return context.getSharedPreferences(entityId, Context.MODE_PRIVATE);
    }
    /** Safe to call after login.*/
    public static SharedPreferences getCurrentUserPrefs(){
        return context.getSharedPreferences(sharedManager().getNetworkAdapter().currentUser().getEntityID(), Context.MODE_PRIVATE);
    }
}
