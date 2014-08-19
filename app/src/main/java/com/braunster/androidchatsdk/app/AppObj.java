package com.braunster.androidchatsdk.app;

import android.app.Application;
import android.os.Build;
import android.provider.Settings;

import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.BFirebaseNetworkAdapter;
import com.bugsense.trace.BugSenseHandler;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

//import com.braunster.network.BFacebookManager;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Bug Sense
        int adb;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
            adb = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        else adb = Settings.Global.getInt(getApplicationContext().getContentResolver(), Settings.Global.ADB_ENABLED, 0);

        if (adb == 0 || BNetworkManager.BUGSENSE_ENABLED) {
            BugSenseHandler.initAndStartSession(getApplicationContext(), BDefines.APIs.BugSenseKey);
            BugSenseHandler.addCrashExtraData("Version", getResources().getString(R.string.chat_sdk_version));
        }

        // Android chat SDK init!
        BNetworkManager.init(getApplicationContext());
        BFacebookManager.init("247787328762280", getApplicationContext());

        // Parse init
        Parse.initialize(getApplicationContext(), BDefines.APIs.ParseAppId, BDefines.APIs.ParseClientKey);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        PushService.setDefaultPushCallback(this, MainActivity.class);

        // Adapter init.
        BFirebaseNetworkAdapter adapter = new BFirebaseNetworkAdapter(getApplicationContext());
        BNetworkManager.sharedManager().setNetworkAdapter(adapter);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        String levelName = "";

        switch (level)
        {
            case TRIM_MEMORY_BACKGROUND:
                levelName = "memory background";
                break;

            case TRIM_MEMORY_COMPLETE:
                levelName = "memory complete";
                break;

            case TRIM_MEMORY_MODERATE:
                levelName = "memory moderate";
                break;

            case TRIM_MEMORY_RUNNING_CRITICAL:
                levelName = "memory critical";
                break;

            case TRIM_MEMORY_RUNNING_LOW:
                levelName = "memory low";
                break;

            case TRIM_MEMORY_UI_HIDDEN:
                levelName = "memory ui hidden";
                break;
        }
//        Log.d("App", "onTrimMemory, Level: " + levelName);
    }
}
