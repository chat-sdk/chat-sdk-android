package co.chatsdk.android.app;

import android.content.Context;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.error.ChatSDKException;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.session.NetworkManager;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
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

//        builder.firebaseRootPath("firebase_v4_web_new_4");
        config.firebaseRootPath("19_01");
        config.googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE");
        config.publicRoomCreationEnabled(false);
        config.pushNotificationSound("default");
//        config.pushNotificationsForPublicChatRoomsEnabled(true);

        try {
            ChatSDK.initialize(config.build(), new FirebaseNetworkAdapter(), new BaseInterfaceAdapter(context));
        }
        catch (ChatSDKException e) {

        }

        FirebaseFileStorageModule.activate();
        FirebasePushModule.activate();


//        FirebaseUIModule.activate(context, EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

        ProfilePicturesModule.activate();

    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
