package com.braunster.androidchatsdk.app;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BChatcatNetworkAdapter;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;

import timber.log.Timber;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // Setting the version name of the sdk, This data will be added 
        // to the user metadata and will help in future when doing code updating.
        BDefines.BAppVersion = BuildConfig.VERSION_NAME;
        
        MultiDex.install(this);




        // Logging tool.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.HollowTree());
        }


        // Android chat SDK init!
        ChatSDKUiHelper.initDefault();
        BNetworkManager.init(getApplicationContext());
        // Adapter init.
        BChatcatNetworkAdapter adapter = new BChatcatNetworkAdapter(getApplicationContext());
        BNetworkManager.sharedManager().setNetworkAdapter(adapter);
    }
}
