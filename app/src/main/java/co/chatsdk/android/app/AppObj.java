package co.chatsdk.android.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firebase.FirebaseModule;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.ui.manager.UserInterfaceModule;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        Configuration.Builder builder = new Configuration.Builder(context);
        builder.firebaseRootPath("firebase_v4_web_new_4");

        ChatSDK.initialize(builder.build());

        UserInterfaceModule.activate(context);

        FirebaseModule.activate();

        FirebaseFileStorageModule.activate();
        FirebasePushModule.activateForFirebase();

    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
