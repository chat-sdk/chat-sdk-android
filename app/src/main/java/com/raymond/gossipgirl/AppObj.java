package com.raymond.gossipgirl;

import android.content.Context;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import co.chatsdk.core.error.ChatSDKException;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        Configuration.Builder config = new Configuration.Builder(context);
        config.firebaseRootPath("v1");
        config.googleMaps("___");
        config.twitterLogin("___", "___");
        config.googleLogin("___");

        try {
            ChatSDK.initialize(config.build(), new BaseInterfaceAdapter(context), new FirebaseNetworkAdapter());
        }
        catch (ChatSDKException e) {

        }

        FirebaseFileStorageModule.activate();
        FirebasePushModule.activate();

        FirebaseUIModule.activate(context,
                EmailAuthProvider.PROVIDER_ID,
                FacebookAuthProvider.PROVIDER_ID,
                TwitterAuthProvider.PROVIDER_ID,
                GoogleAuthProvider.PROVIDER_ID);
    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
