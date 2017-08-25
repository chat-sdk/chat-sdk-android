package com.braunster.androidchatsdk.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.firebase.FirebaseUploadHandler;
import co.chatsdk.firebase.backendless.BackendlessHandler;

import co.chatsdk.core.utils.AppContext;

import co.chatsdk.xmpp.XMPPModule;
import timber.log.Timber;
import co.chatsdk.ui.helpers.UIHelper;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        Context context = getApplicationContext();

        // Logging tool.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(Timber.asTree());
        }

        AppContext.shared().setContext(getApplicationContext());
        AppContext.shared().setGoogleMapsAPIKey(context.getResources().getString(R.string.google_api_key));

        new XMPPModule().activate();
//        new FirebaseModule().activate();

        String backendlessAppKey = context.getString(com.braunster.chatsdk.R.string.backendless_app_id);
        String backendlessSecret = context.getString(com.braunster.chatsdk.R.string.backendless_secret_key);
        String backendlessVersion = context.getString(com.braunster.chatsdk.R.string.backendless_app_version);

        NetworkManager.shared().a.push = new BackendlessHandler(context, backendlessAppKey, backendlessSecret, backendlessVersion);
        NetworkManager.shared().a.upload = new FirebaseUploadHandler();

        // Needed?
        DaoCore.init(context);

        // Adapter init.

        UIHelper.shared().setContext(context);
    }
}
