package sdk.chat;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import sdk.chat.android.live.R;
import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.location.FirebaseNearbyUsersModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.ui.module.UIModule;

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
//            firestream();
//            xmpp();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void firebase() throws Exception {
        String rootPath = "pre_1";


        ChatSDK.builder()
                .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                .setAnonymousLoginEnabled(false)

//                .setDebugModeEnabled(true)
                .setRemoteConfigEnabled(false)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                .setSendSystemMessageWhenRoleChanges(true)
                .setRemoteConfigEnabled(true)

                .build()

                // Add the network adapter module
                .addModule(
                        FirebaseModule.builder()
                                .setFirebaseRootPath(rootPath)
                                .setDisableClientProfileUpdate(false)
                                .setEnableCompatibilityWithV4(true)
                                .setDevelopmentModeEnabled(true)
                                .build()
                )

                // Add the UI module
                .addModule(UIModule.builder()
                        .setPublicRoomCreationEnabled(true)
                        .setPublicRoomsEnabled(true)
                        .setTheme(R.style.GGTheme)
                        .build()
                )




        // Add modules to handle file uploads, push notifications
                .addModule(FirebaseUploadModule.shared())
                .addModule(FirebasePushModule.shared())
//                .addModule(EncryptionModule.shared())
//                .addModule(ProfilePicturesModule.shared())
//
                .addModule(ContactBookModule.shared())
//                            .addModule(EncryptionModule.shared())
//                .addModule(FileMessageModule.shared())
//                .addModule(AudioMessageModule.shared())
//                .addModule(StickerMessageModule.shared())
//                .addModule(VideoMessageModule.shared())
//                .addModule(FirebaseBlockingModule.shared())
//                .addModule(FirebaseLastOnlineModule.shared())
                .addModule(FirebaseNearbyUsersModule.builder().build())
//                .addModule(FirebaseReadReceiptsModule.shared())
//                .addModule(FirebaseTypingIndicatorModule.shared())

//                .addModule(ExtrasModule.builder(config -> {
//                    if (Device.honor(this)) {
//                        config.setDrawerEnabled(false);
//                    }
//                }))

//
//                .addModule(FirebaseUIModule.builder()
//                        .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
//                        .build()
//                )

                // Activate
                .build()
                .activate(this);

//        ChatSDK.ui().setTab("Debug", null, new DebugFragment(), 99);
//        ChatSDK.ui().removeTab(0);


        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
            networkEvent.debug();
        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(t -> {
            t.printStackTrace();
        });


        ChatSDK.ui().startProfileActivity(this, "UserID");

        ChatSDK.db().fetchEntityWithEntityID("UserID", User.class);

        Thread thread = ChatSDK.db().createEntity(Thread.class);


        //

        ChatSDK.core().currentUser();

        ChatSDK.auth().isAuthenticated();

//        ChatSDK.thread().createThread(null, ).subscribe();

        Disposable di = ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.MessageAdded)).subscribe(networkEvent -> {

        });





//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
//        FirebaseCrashlytics.getInstance().log("Start");
//            TestScript.run(context, config.firebaseRootPath);
//            new DummyData(
//            0, 50);

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));

    }

//    public void firestream() throws Exception {
//        String rootPath = "pre_4";
//
//        ChatSDK.builder()
//                .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
//                .setAnonymousLoginEnabled(false)
//                .setMessagesToLoadPerBatch(10)
//                .setRemoteConfigEnabled(false)
//                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
//                .setSendSystemMessageWhenRoleChanges(true)
//                .build()
//
//                // Add the network adapter module
//                .addModule(
//                        //
//
//                        /*
//                         * Delete messages on receipt
//                         * Read receipts on / off
//                         * Listening from last message sent
//                         */
//
//                        FireStreamModule.builder(FirebaseServiceType.Realtime)
//                                .setRoot(rootPath)
//                                .setSandbox("firestream")
//                                .setDeleteMessagesOnReceiptEnabled(false)
//                                .setAutoMarkReceivedEnabled(true)
//                                .setAutoAcceptChatInviteEnabled(true)
//                                .setDebugEnabled(true)
//                                .setDeleteDeliveryReceiptsOnReceipt(true)
//                                .build()
//                )
//
//                // Add the UI module
//                .addModule(UIModule.builder()
//                        .setPublicRoomsEnabled(false)
//                        .build()
//                )
//
//                // Add modules to handle file uploads, push notifications
//                .addModule(FirebaseUploadModule.shared())
//                .addModule(FirebasePushModule.shared())
//                .addModule(ProfilePicturesModule.shared())
////                .addModules(FireStreamTypingIndicatorModule.shared())
//                .addModules(FireStreamReadReceiptsModule.shared())
//                .addModule(ContactBookModule.shared())
////                            .addModule(EncryptionModule.shared())
//                .addModule(FileMessageModule.shared())
//                .addModule(AudioMessageModule.shared())
//                .addModule(StickerMessageModule.shared())
//                .addModule(VideoMessageModule.shared())
//
//                .addModule(FirebaseNearbyUsersModule.builder()
//                        .setTabIndex(100)
//                        .build())
//
//                .addModule(ExtrasModule.builder(config -> {
//                    config.setDrawerEnabled(false);
////                    if (Device.honor(this)) {
////                        config.setDrawerEnabled(false);
////                    }
//                }))
//
//                .addModule(FirebaseUIModule.builder()
//                        .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
//                        .build()
//                )
//
//                // Activate
//                .build()
//                .activate(this);
//
//
//
//        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
//            networkEvent.debug();
//        });
//
//        d = ChatSDK.events().errorSourceOnMain().subscribe(t -> {
//            t.printStackTrace();
//        });
//
//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
//        FirebaseCrashlytics.getInstance().log("Start");
////            TestScript.run(context, config.firebaseRootPath);
////            new DummyData(20z23,!Bear
////            0, 50);
//
////            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));
//
//    }


}
