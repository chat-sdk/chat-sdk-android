package sdk.chat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import org.pmw.tinylog.Logger;

import co.chatsdk.core.avatar.gravatar.GravatarAvatarGenerator;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Config;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.ui.module.DefaultUIModule;
import co.chatsdk.ui.module.UIConfig;
import sdk.chat.ui.extras.ExtrasModule;

/**
 * Created by Ben Smiley on 6/8/2014.
 */
//public class MainApplication extends MultiDexApplication {
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String rootPath = "micro_test_98";

        try {

            ChatSDK.configure(config -> config

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(true)
                    .setRemoteConfigEnabled(true)
                    .setIdenticonType(Config.IdenticonType.RoboHash))

                    // Add the network adapter module
                    .addModule(FirebaseModule.shared(config -> config
                            .setFirebaseRootPath(rootPath)
                    ))

                    // Add the UI module
                    .addModule(DefaultUIModule.shared(config -> config
                            .setPublicRoomCreationEnabled(true)
                            .setPublicChatRoomLifetimeMinutes(60 * 24)
                    ))

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseFileStorageModule.shared())
                    .addModule(FirebasePushModule.shared())
                    .addModule(ProfilePicturesModule.shared())
//                    .addModule(ExtrasModule.shared(config -> config.setDrawerEnabled(true)))

//                    .addModule(FirestreamModule.shared(config -> config
//                            .setRoot(rootPath)
//                            .setStartListeningFromLastSentMessageDateEnabled(false)
//                            .setListenToMessagesWithTimeAgo(FirestreamConfig.TimePeriod.days(7))
//                            .setDatabaseType(FirestreamConfig.DatabaseType.Realtime)
//                            .setDeleteMessagesOnReceiptEnabled(false)
//                            .setDeliveryReceiptsEnabled(false)
//                    ))

//                     Enable Firebase UI
//                    .addModule(FirebaseUIModule.shared(config -> config
//                            .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
//                    ))

                    // Activate
                    .activate(this);

//            TestScript.run(context, config.firebaseRootPath);
//            new DummyData(200, 50);

            ChatSDK.ui().setAvatarGenerator(new GravatarAvatarGenerator());

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Error");
            assert(false);
        }

    }

//    @Override
//    protected void attachBaseContext (Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }
}
