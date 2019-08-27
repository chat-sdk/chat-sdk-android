package co.chatsdk.android.app;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.database.DataSnapshot;

import co.chatsdk.android.app.custom.AChatActivity;
import co.chatsdk.android.app.custom.AConfigurator;
import co.chatsdk.android.app.custom.AContactsFragment;
import co.chatsdk.android.app.custom.ALoginActivity;
import co.chatsdk.android.app.custom.APrivateThreadsFragment;
import co.chatsdk.android.app.custom.AProfileActivity;
import co.chatsdk.android.app.custom.AProfileFragment;
import co.chatsdk.android.app.custom.APublicThreadsFragment;
import co.chatsdk.android.app.test.MessageTestChatOption;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.error.ChatSDKException;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.ui.ProfileFragmentProvider;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.FirebasePaths;
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

        try {

            Configuration.Builder config = new Configuration.Builder(context);

            config.firebaseRootPath("modules_test_june_20");
            config.googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE");
            config.publicRoomCreationEnabled(true);
            config.pushNotificationSound("default");
            config.pushNotificationsForPublicChatRoomsEnabled(false);

            config.twitterLogin("Kqprq5b6bVeEfcMAGoHzUmB3I", "hPd9HCt3PLnifQFrGHJWi6pSZ5jF7kcHKXuoqB8GJpSDAlVcLq");
            config.googleLogin("1088435112418-e3t77t8jl2ucs8efeqs72o696in8soui.apps.googleusercontent.com");


            // For the demo version of the client expire rooms after 24 hours
            config.publicChatRoomLifetimeMinutes(60 * 24);


            ChatSDK.initialize(config.build(), new FirebaseNetworkAdapter(), new BaseInterfaceAdapter(context));

//            AConfigurator.configure();

            FirebaseFileStorageModule.activate();
            FirebasePushModule.activate();
            ProfilePicturesModule.activate();


            // Uncomment this to enable Firebase UI
            // FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("Message Burst"));


        }
        catch (ChatSDKException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
