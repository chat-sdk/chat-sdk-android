package co.chatsdk.android.app;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firefly.FireflyNetworkAdapter;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import firefly.sdk.chat.Config;
import firefly.sdk.chat.namespace.Fl;

/**
 * Created by itzik childOn 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        try {

            Configuration.Builder config = new Configuration.Builder();

            config.firebaseRootPath("micro_test");
//            config.firebaseRootPath("live_01_20");
            config.googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE");
            config.publicRoomCreationEnabled(true);
            config.pushNotificationSound("default");

            config.twitterLogin("Kqprq5b6bVeEfcMAGoHzUmB3I", "hPd9HCt3PLnifQFrGHJWi6pSZ5jF7kcHKXuoqB8GJpSDAlVcLq");
            config.googleLogin("1088435112418-e3t77t8jl2ucs8efeqs72o696in8soui.apps.googleusercontent.com");

            // For the demo version of the client expire rooms after 24 hours
            config.publicChatRoomLifetimeMinutes(60 * 24);
//            config.remoteConfigEnabled(true);

//            ChatSDK.initialize(context, config.build(), FirebaseNetworkAdapter.class, BaseInterfaceAdapter.class);

            // Firefly configuration
            Config fireflyConfig = new Config();
            fireflyConfig.root = config.build().firebaseRootPath;
            fireflyConfig.sandbox = "firefly";
            fireflyConfig.database = Config.DatabaseType.Firestore;

            Fl.y.initialize(context, fireflyConfig);

            ChatSDK.initialize(context, config.build(), FireflyNetworkAdapter.class, BaseInterfaceAdapter.class);


//            AConfigurator.configure();

            FirebaseFileStorageModule.activate();
            FirebasePushModule.activate();
            ProfilePicturesModule.activate();


            // Uncomment this to enable Firebase UI
                    // FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));


        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
