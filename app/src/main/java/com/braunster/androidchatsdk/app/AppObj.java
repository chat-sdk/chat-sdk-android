package com.braunster.androidchatsdk.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.firebase.backendless.BackendlessHandler;

import com.braunster.chatsdk.network.BNetworkManager;

import co.chatsdk.core.utils.AppContext;
import co.chatsdk.firebase.FirebaseNetworkAdapter;


import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.interfaces.UiLauncherInterface;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.ChatSDKUiHelper;

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

        AppContext.init(getApplicationContext());
        NetworkManager.shared().a = new FirebaseNetworkAdapter();
        //StorageManager.shared().a = DaoCore.getDaoCore(getApplicationContext());

        String backendlessAppKey = context.getString(com.braunster.chatsdk.R.string.backendless_app_id);
        String backendlessSecret = context.getString(com.braunster.chatsdk.R.string.backendless_secret_key);
        String backendlessVersion = context.getString(com.braunster.chatsdk.R.string.backendless_app_version);

        NetworkManager.shared().a.push = new BackendlessHandler(context, backendlessAppKey, backendlessSecret, backendlessVersion);

        // Android chat SDK init!
        BNetworkManager.init(getApplicationContext());
        // Adapter init.

        UiLauncherInterface uiLauncher = ChatSDKUiHelper.initDefault();
        BNetworkManager.setUiLauncherInterface(uiLauncher);
    }
}
