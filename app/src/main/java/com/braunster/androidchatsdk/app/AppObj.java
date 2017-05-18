package com.braunster.androidchatsdk.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseCoreAdapter;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseThreadsAdapter;
//import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.BackendlessHandler;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;

import co.chatsdk.core.utils.AppContext;
import co.chatsdk.firebase.FirebaseNetworkAdapter;

import co.chatsdk.core.NetworkManager;
import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.interfaces.CoreInterface;
import tk.wanderingdevelopment.chatsdk.core.interfaces.ThreadsInterface;
import tk.wanderingdevelopment.chatsdk.core.interfaces.UiLauncherInterface;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.ChatSDKUiHelper;

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
        ThreadsInterface threads = new FirebaseThreadsAdapter();
        BNetworkManager.setThreadsInterface(threads);


        CoreInterface core = new FirebaseCoreAdapter(getApplicationContext());
        BNetworkManager.setCoreInterface(core);

        UiLauncherInterface uiLauncher = ChatSDKUiHelper.initDefault();
        BNetworkManager.setUiLauncherInterface(uiLauncher);
    }
}
