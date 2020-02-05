package sdk.chat;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import co.chatsdk.core.avatar.gravatar.GravatarAvatarGenerator;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.firestream.FireStreamNetworkAdapter;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.ui.chatkit.CKChatActivity;
import co.chatsdk.ui.chatkit.CKPrivateThreadsFragment;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import firestream.chat.Config;
import firestream.chat.namespace.Fire;
import sdk.chat.ui.extras.MainDrawActivity;

/**
 * Created by Ben Smiley on 6/8/2014.
 */
public class MainApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        String rootPath = "micro_test_99";

        try {
            Configuration config = new Configuration.Builder()
                    .firebaseRootPath(rootPath)
                    .publicRoomCreationEnabled(true)
                    .publicChatRoomLifetimeMinutes(60 * 24)
                    .twitterLogin("Kqprq5b6bVeEfcMAGoHzUmB3I", "hPd9HCt3PLnifQFrGHJWi6pSZ5jF7kcHKXuoqB8GJpSDAlVcLq")
                    .googleLogin("1088435112418-e3t77t8jl2ucs8efeqs72o696in8soui.apps.googleusercontent.com")
                    .googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .build();

            // FireStream configuration

            Config firestreamConfig = new Config();
            try {
                firestreamConfig.setRoot(rootPath);
                firestreamConfig.startListeningFromLastSentMessageDate = false;
                firestreamConfig.listenToMessagesWithTimeAgo = Config.TimePeriod.days(7);
                firestreamConfig.database = Config.DatabaseType.Realtime;
                firestreamConfig.deleteMessagesOnReceipt = false;
                firestreamConfig.deliveryReceiptsEnabled = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

            Fire.stream().initialize(context, firestreamConfig);
            ChatSDK.initialize(context, config, FireStreamNetworkAdapter.class, BaseInterfaceAdapter.class);

            // Old Firebase Adapter
//            ChatSDK.initialize(context, config, FirebaseNetworkAdapter.class, BaseInterfaceAdapter.class);

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error");
        }
        finally {
            ChatSDK.ui().setPrivateThreadsFragment(new CKPrivateThreadsFragment());
            ChatSDK.ui().setMainActivity(MainDrawActivity.class);

            ChatSDK.ui().setAvatarGenerator(new GravatarAvatarGenerator());

            FirebaseFileStorageModule.activate();
            FirebasePushModule.activate();
            ProfilePicturesModule.activate();
            FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

//            new DummyData(1, 1000);

//            TestScript.run(context, config.build().firebaseRootPath);

            // Uncomment this to enable Firebase UI
            // FirebaseUIModule.activate(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));

        }
    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
