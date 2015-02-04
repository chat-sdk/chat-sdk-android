package com.braunster.androidchatsdk.app;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseNetworkAdapter;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.network.BNetworkManager;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);
        
        ChatSDKUiHelper.initDefault();

        // Android chat SDK init!
        BNetworkManager.init(getApplicationContext());

        // Adapter init.
        BFirebaseNetworkAdapter adapter = new BFirebaseNetworkAdapter(getApplicationContext());


        BNetworkManager.sharedManager().setNetworkAdapter(adapter);
    }
}
