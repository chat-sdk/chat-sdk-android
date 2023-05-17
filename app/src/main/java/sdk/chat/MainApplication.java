package sdk.chat;

import android.app.Application;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.ImageMessageModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Device;
import sdk.chat.custom.CustomProvider;
import sdk.chat.firbase.online.FirebaseLastOnlineModule;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.blocking.FirebaseBlockingModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.receipts.FirebaseReadReceiptsModule;
import sdk.chat.firebase.typing.FirebaseTypingIndicatorModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.file.FileMessageModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
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

        Disposable d3 = ChatSDK.events().errorSourceOnMain().subscribe(throwable -> {
            //
            throwable.printStackTrace();
        });
    }

    public void generateMessages() {
        ChatSDK.hook().addHook(Hook.sync(data -> {

            Lorem lorem = LoremIpsum.getInstance();

            List<Completable> createThreadEvents = new ArrayList<>();

            for (int i = 1; i < 2; i++) {
                createThreadEvents.add(ChatSDK.thread().createThread(i + ": " + lorem.getName(), ChatSDK.contact().contacts()).flatMapCompletable(thread -> {

                    List<Completable> sendMessageEvents = new ArrayList<>();

                    // Add some messages
                    for (int j = 1; j < 101; j++) {
                        sendMessageEvents.add(ChatSDK.thread().sendMessageWithText(j + ": " +lorem.getCity(), thread));
                    }

                    Completable current = Completable.complete();
                    for (Completable c: sendMessageEvents) {
                        current = current.andThen(c);
                    }

                    return current;

                }).doOnError(throwable -> {
                    Logger.info("");
                }));
            }

            Completable current = Completable.complete();
            for (Completable c: createThreadEvents) {
                current = current.andThen(c);
            }

//                current.subscribe();

        }), HookEvent.DidAuthenticate);
    }

    public void firebase() throws Exception {
        String rootPath = "pre_988";

        String username = "1@d.co";
        if (Device.honor()) {
            username = "2@d.co";
        }
        if (Device.pixel()) {
            username = "3@d.co";
        }
        if (Device.galaxy()) {
            username = "4@d.co";
        }

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
                .setLogLevel(Level.DEBUG)
                .addProvider(new CustomProvider())
//                .setMessagesToLoadPerBatch(5)

                .setRemoteConfigEnabled(false)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                .setSendSystemMessageWhenRoleChanges(true)
                .setRemoteConfigEnabled(true)
//                .setDatabaseEncryptionKey("test")

                .setDebugUsername(username)
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

//        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new V2TextMessageRegistration());
//        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new V2ImageMessageRegistration());
//        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new VideoMessageRegistration());


    }

}
