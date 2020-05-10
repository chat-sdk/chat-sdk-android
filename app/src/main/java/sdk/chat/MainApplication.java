package sdk.chat;

import android.app.Application;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import co.chatsdk.contact.ContactBookModule;
import sdk.chat.location.FirebaseNearbyUsersModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Config;
import co.chatsdk.firebase.blocking.FirebaseBlockingModule;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.last_online.FirebaseLastOnlineModule;
import co.chatsdk.message.file.FileMessageModule;
import co.chatsdk.message.sticker.module.StickerMessageModule;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.read_receipts.FirebaseReadReceiptsModule;
import co.chatsdk.typing_indicator.FirebaseTypingIndicatorModule;
import co.chatsdk.ui.module.DefaultUIModule;
import io.reactivex.disposables.Disposable;
import sdk.chat.audio.AudioMessageModule;
import sdk.chat.core.utils.Device;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.ui.extras.ExtrasModule;

/**
 * Created by Ben Smiley on 6/8/2014.
 */
//public class MainApplication extends MultiDexApplication {
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            firebase();
//        xmpp();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void firebase() throws Exception {
        String rootPath = "pre_2";

        ChatSDK.builder().configure()
                .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                .setAnonymousLoginEnabled(false)
//                .setDebugModeEnabled(true)
                .setRemoteConfigEnabled(false)
                .setIdenticonType(Config.IdenticonType.Gravatar)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                .setSendSystemMessageWhenRoleChanges(true)
                .build()

                // Add the network adapter module
                .addModule(
                        FirebaseModule.configure()
                                .setFirebaseRootPath(rootPath)
                                .setDisableClientProfileUpdate(false)
                                .setEnableCompatibilityWithV4(true)
                                .setDevelopmentModeEnabled(true)
                                .build()
                )

                // Add the UI module
                .addModule(DefaultUIModule.configure()
                        .setPublicRoomCreationEnabled(false)
                        .setPublicRoomsEnabled(false)
                        .build()
                )

                // Add modules to handle file uploads, push notifications
                .addModule(FirebaseFileStorageModule.shared())
                .addModule(FirebasePushModule.shared())
                .addModule(ProfilePicturesModule.shared())

                .addModule(ContactBookModule.shared())
        //                    .addModule(EncryptionModule.shared())
                .addModule(FileMessageModule.shared())
                .addModule(AudioMessageModule.shared())
                .addModule(StickerMessageModule.shared())
                .addModule(VideoMessageModule.shared())
                .addModule(FirebaseBlockingModule.shared())
                .addModule(FirebaseLastOnlineModule.shared())
                .addModule(FirebaseNearbyUsersModule.shared())
                .addModule(FirebaseReadReceiptsModule.shared())
                .addModule(FirebaseTypingIndicatorModule.shared())

                .addModule(ExtrasModule.configure(config -> {
                    if (Device.honor(this)) {
                        config.setDrawerEnabled(false);
                    }
                }))

                .addModule(FirebaseUIModule.configure()
                        .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
                        .build()
                )

                // Activate
                .build()
                .activate(this);

        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
            networkEvent.debug();
        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(t -> {
            t.printStackTrace();
        });

//            TestScript.run(context, config.firebaseRootPath);
//            new DummyData(200, 50);

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));



    }

//    public void xmpp() {
//        try {
//
//            ChatSDK.builder().configure()
//
//                    // Configure the library
//                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
//                    .setAnonymousLoginEnabled(false)
//                    .setDebugModeEnabled(true)
//                    .setIdenticonType(Config.IdenticonType.Gravatar).build()
//
//                    // Add modules to handle file uploads, push notifications
//                    .addModule(FirebaseFileStorageModule.shared())
//                    .addModule(FirebasePushModule.shared())
//
////                    .addModule(ContactBookModule.shared())
////                    .addModule(EncryptionModule.shared())
////                    .addModule(FileMessageModule.shared())
////                    .addModule(AudioMessageModule.shared())
////                    .addModule(StickerMessageModule.shared())
////                    .addModule(VideoMessageModule.shared())
//
//                    .addModule(XMPPReadReceiptsModule.shared())
//                    .addModule(
//                            Testing.myOpenFire(XMPPModule.configure())
//                                    .build()
//                                    .configureUI(config -> config.setResetPasswordEnabled(false))
//                    )
//                    .addModule(ExtrasModule.shared())
//
//                    .build().activate(this);
//
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            Logger.debug("Error");
//            assert(false);
//        }
//
//        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
//
//        });
//
//        d = ChatSDK.events().errorSourceOnMain().subscribe(throwable -> {
//            // Catch errors
//            throwable.printStackTrace();
//        });
//
//    }

}
