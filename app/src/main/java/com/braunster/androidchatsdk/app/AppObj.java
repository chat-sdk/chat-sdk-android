package com.braunster.androidchatsdk.app;

import android.app.Application;

import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.BFirebaseNetworkAdapter;

//import com.braunster.network.BFacebookManager;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Android chat SDK init!
        BNetworkManager.init(getApplicationContext());

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
