package sdk.chat;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.ImageMessageModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firbase.online.FirebaseLastOnlineModule;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.blocking.FirebaseBlockingModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.receipts.FirebaseReadReceiptsModule;
import sdk.chat.firebase.typing.FirebaseTypingIndicatorModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.V2TextMessageRegistration;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.file.FileMessageModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

/**
 * Created by Ben Smiley on 6/8/2014.
 */
//public class MainApplication extends MultiDexApplication {
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PublishSubject<NetworkEvent> ps = PublishSubject.create();

        ps.subscribe(networkEvent -> {
            if (networkEvent.typeIs(EventType.ContactAdded)) {
                System.out.println("");
            }
        });

        long start = System.currentTimeMillis();
        ps.onNext(new NetworkEvent(EventType.MessageAdded));
        long split1 = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            if (i%100 == 0) {
                Disposable d = ps.subscribe(networkEvent -> {
                    if (networkEvent.typeIs(EventType.ContactAdded)) {
                        System.out.println("");
                    }
                });
            } else {
                Disposable d = ps.subscribe(networkEvent -> {
                    if (networkEvent.typeIs(EventType.MessageAdded)) {
                        System.out.println("Ok");
                    }
                });
            }
        }

        long split2 = System.currentTimeMillis();
        ps.onNext(new NetworkEvent(EventType.MessageAdded));
        long split3 = System.currentTimeMillis();

        System.out.println("Diff 1: " + (split1 - start));
        System.out.println("Diff 2: " + (split3 - split2));
        System.out.println("Done");





        try {
            firebase();

//            ChatSDK.hook().addHook(Hook.sync(data -> {
//
//                Lorem lorem = LoremIpsum.getInstance();
//
//                for (int i = 0; i < 50; i++) {
//                    Disposable d = ChatSDK.thread().createThread(lorem.getName(), ChatSDK.contact().contacts()).subscribe(thread -> {
//                        // Add some messages
//                        for (int j = 0; j < 100; j++) {
//                            ChatSDK.thread().sendMessageWithText(lorem.getCity(), thread).subscribe();
//                        }
//                    });
//                }
//
//
//            }), HookEvent.DidAuthenticate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logTime() {

    }

    public void firebase() throws Exception {
        String rootPath = "test_1";

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()
//                .penaltyLog()
//                .build());

//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .build());

        ChatSDK.builder()
                .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                .setAnonymousLoginEnabled(false)
                .setReuseDeleted1to1Threads(false)

                .setRemoteConfigEnabled(false)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                .setSendSystemMessageWhenRoleChanges(true)
                .setRemoteConfigEnabled(true)
                .setDatabaseEncryptionKey("test")

                .setDebugUsername("2@d.co")
                .setDebugPassword("123456")

                .build()

                // Add the network adapter module
                .addModule(
                        FirebaseModule.builder()
                                .setFirebaseRootPath(rootPath)
                                .setDisableClientProfileUpdate(false)
                                .setDevelopmentModeEnabled(true)
                                .build()
                )
                .addModule(ImageMessageModule.shared())

                // Add the UI module
                .addModule(UIModule.builder()
                                .setPublicRoomCreationEnabled(false)
                                .setPublicRoomsEnabled(false)
                                .build()
                )

                // Add modules to handle file uploads, push notifications
                .addModule(FirebaseUploadModule.shared())
                .addModule(LocationMessageModule.shared())
                .addModule(FirebasePushModule.shared())
                .addModule(ContactBookModule.shared())
                .addModule(FileMessageModule.shared())
                .addModule(AudioMessageModule.shared())
                .addModule(StickerMessageModule.shared())
                .addModule(VideoMessageModule.shared())
                .addModule(FirebaseBlockingModule.shared())
                .addModule(FirebaseLastOnlineModule.shared())
                .addModule(FirebaseReadReceiptsModule.shared())
                .addModule(FirebaseTypingIndicatorModule.shared())

//    .addModule(SinchModule.builder()
//            .setApplicationKey("90c5e8c0-7a3d-4bd5-8d8f-075e5c24cd1f")
//            .setSecret("bM0AbXIhG0eIVMlTLcHYrQ==")
//            .build())

                .addModule(ExtrasModule.builder(config -> {
                    config.setDrawerEnabled(false);
                }))
                .build()
                .activateWithEmail(this, "team@sdk.chat");

        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new V2TextMessageRegistration());

//        ChatSDK.shared()
        //
        // User, Thread, Message
        Disposable d = ChatSDK.events().sourceOnMain()
                .subscribe(networkEvent -> {
                    networkEvent.debug();
        });
        d.dispose();

        ChatSDK.hook().addHook(Hook.sync(data -> {

        }), HookEvent.ContactWasAdded);


    }

}
