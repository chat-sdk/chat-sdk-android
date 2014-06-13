package com.braunster.androidchatsdk.app;

import android.app.Application;

import com.braunster.chatsdk.network.BFacebookManager;

//import com.braunster.network.BFacebookManager;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BFacebookManager.init("247787328762280");
    }
}
