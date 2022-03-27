package sdk.chat;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.ImageMessageModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.dcom.DComFirebaseProvider;
import sdk.chat.dcom.DComNetworkAdapter;
import sdk.chat.firbase.online.FirebaseLastOnlineModule;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.blocking.FirebaseBlockingModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.receipts.FirebaseReadReceiptsModule;
import sdk.chat.firebase.typing.FirebaseTypingIndicatorModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.video.VideoMessageModule;
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

        try {
            firebase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void firebase() throws Exception {
        String rootPath = "x_1";

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
                                .setNetworkAdapter(DComNetworkAdapter.class)
                                .setFirebaseProvider(new DComFirebaseProvider())

//                                .setNetworkAdapter(ZFirebaseNetworkAdapter.class)
//                                .setFirebaseProvider(new V4V5FirebaseProvider())

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
                .addModule(FirebasePushModule.shared())
                .addModule(ContactBookModule.shared())
//                .addModule(FileMessageModule.shared())
                .addModule(AudioMessageModule.shared())
//                .addModule(StickerMessageModule.shared())
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

//        ChatSDK.shared()
        //
        // User, Thread, Message
        Disposable d = ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .subscribe(networkEvent -> {
                    networkEvent.getMessage();
        });
        d.dispose();

        ChatSDK.hook().addHook(Hook.sync(data -> {

        }), HookEvent.ContactWasAdded);


    }

}
