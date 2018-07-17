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
//        builder.firebaseRootPath("firebase_v4_web_new_4");
        builder.firebaseRootPath("18_07");
        builder.googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE");
        builder.firebaseCloudMessagingServerKey("AAAA_WvJyeI:APA91bFIDYoxnbFTub61SKCh8-RZrElzdkZpzyV3paGFlRWonMzq33zQmQW3ub5hDXLuRaipwtoHSoDKXkZlN5DRb_EYdrxtaDptmvZKCYBPKI-4RqTK9wVLOJvgc5X3bVWLfpNSJO_tLK2pnmhfpHDw2Zs-5L2yug");
        builder.publicRoomCreationEnabled(true);


        ChatSDK.initialize(builder.build());

        UserInterfaceModule.activate(context);

        FirebaseModule.activate();

        FirebaseFileStorageModule.activate();
        FirebasePushModule.activateForFirebase();
//        FirebaseUIModule.activate(context, EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
