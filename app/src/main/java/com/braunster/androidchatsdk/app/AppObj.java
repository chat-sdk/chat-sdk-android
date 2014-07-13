package com.braunster.androidchatsdk.app;

import android.app.Application;

import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.BFirebaseNetworkAdapter;
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
}
