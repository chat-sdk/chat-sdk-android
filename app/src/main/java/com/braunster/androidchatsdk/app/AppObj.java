package com.braunster.androidchatsdk.app;

import android.app.Application;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseNetworkAdapter;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.network.BNetworkManager;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ChatSDKUiHelper.initDefault();

        // Android chat SDK init!
        BNetworkManager.init(getApplicationContext());

        // Adapter init.
        BFirebaseNetworkAdapter adapter = new BFirebaseNetworkAdapter(getApplicationContext());


        BNetworkManager.sharedManager().setNetworkAdapter(adapter);
    }
}
