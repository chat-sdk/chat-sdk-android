package co.chatsdk.android.app;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import co.chatsdk.android.app.test.MicroChatSDKTest;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firestore.FirestoreNetworkAdapter;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import co.chatsdk.core.dao.Message;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Action;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        try {

            Configuration.Builder config = new Configuration.Builder();

//            config.firebaseRootPath("local_test_19");
            config.firebaseRootPath("live_12_19");
            config.googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE");
            config.publicRoomCreationEnabled(true);
            config.pushNotificationSound("default");
            config.pushNotificationsForPublicChatRoomsEnabled(false);
//            config.messagesToLoadPerBatch(10);

            config.twitterLogin("Kqprq5b6bVeEfcMAGoHzUmB3I", "hPd9HCt3PLnifQFrGHJWi6pSZ5jF7kcHKXuoqB8GJpSDAlVcLq");
            config.googleLogin("1088435112418-e3t77t8jl2ucs8efeqs72o696in8soui.apps.googleusercontent.com");

            // For the demo version of the client expire rooms after 24 hours
            config.publicChatRoomLifetimeMinutes(60 * 24);
//            config.remoteConfigEnabled(true);

            ChatSDK.initialize(context, config.build(), FirebaseNetworkAdapter.class, BaseInterfaceAdapter.class);
//            ChatSDK.initialize(context, config.build(), FirestoreNetworkAdapter.class, BaseInterfaceAdapter.class);


//            AConfigurator.configure();

            FirebaseFileStorageModule.activate();
            FirebasePushModule.activate();
            ProfilePicturesModule.activate();


            // Uncomment this to enable Firebase UI
            // FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("Message Burst"));



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
