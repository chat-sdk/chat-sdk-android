package sdk.chat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageHolders;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import co.chatsdk.android.app.R;
import co.chatsdk.core.avatar.gravatar.GravatarAvatarGenerator;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.ui.BaseInterfaceAdapter;

/**
 * Created by Ben Smiley on 6/8/2014.
 */
public class MainApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        String rootPath = "micro_test_99";

        try {
            Configuration config = new Configuration.Builder()
                    .firebaseRootPath(rootPath)
                    .publicRoomCreationEnabled(true)
                    .publicChatRoomLifetimeMinutes(60 * 24)
                    .googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .build();

            config.defaultNamePrefix = "Your name";
            config.defaultName = "Name";

            // FireStream configuration

//            Config firestreamConfig = new Config();
//            try {
//                firestreamConfig.setRoot(rootPath);
//                firestreamConfig.startListeningFromLastSentMessageDate = false;
//                firestreamConfig.listenToMessagesWithTimeAgo = Config.TimePeriod.days(7);
//                firestreamConfig.database = Config.DatabaseType.Realtime;
//                firestreamConfig.deleteMessagesOnReceipt = false;
//                firestreamConfig.deliveryReceiptsEnabled = false;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            Fire.stream().initialize(context, firestreamConfig);
//            ChatSDK.initialize(context, config, FireStreamNetworkAdapter.class, BaseInterfaceAdapter.class);

            // Old Firebase Adapter
            ChatSDK.initialize(this, config, FirebaseNetworkAdapter.class, BaseInterfaceAdapter.class);

//            TestScript.run(context, config.firebaseRootPath);


        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error");
        }
        finally {
//            ChatSDK.ui().setMainActivity(MainDrawActivity.class);

            ChatSDK.ui().setAvatarGenerator(new GravatarAvatarGenerator());

            FirebaseFileStorageModule.activate();
            FirebasePushModule.activate();
            ProfilePicturesModule.activate();
            FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

//            new DummyData(1, 1000);



            // Uncomment this to enable Firebase UI
            // FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));

        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
