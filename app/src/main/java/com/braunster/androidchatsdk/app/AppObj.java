package com.braunster.androidchatsdk.app;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseCoreAdapter;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseThreadsAdapter;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseAuthAdapter;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;

import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.interfaces.AuthInterface;
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




        // Logging tool.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(Timber.asTree());
        }


        // Android chat SDK init!
        BNetworkManager.init(getApplicationContext());
        // Adapter init.
        AuthInterface auth = new FirebaseAuthAdapter(getApplicationContext());
        BNetworkManager.setAuthInterface(auth);
        ThreadsInterface threads = new FirebaseThreadsAdapter();
        BNetworkManager.setThreadsInterface(threads);
        CoreInterface core = new FirebaseCoreAdapter(getApplicationContext());
        BNetworkManager.setCoreInterface(core);
        UiLauncherInterface uiLauncher = ChatSDKUiHelper.initDefault();
        BNetworkManager.setUiLauncherInterface(uiLauncher);
    }
}
