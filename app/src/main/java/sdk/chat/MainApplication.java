package sdk.chat;

import android.app.Application;

import org.pmw.tinylog.Logger;

import co.chatsdk.contact.ContactBookModule;
import co.chatsdk.core.avatar.gravatar.GravatarAvatarGenerator;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Config;
import co.chatsdk.firebase.blocking.FirebaseBlockingModule;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.nearby_users.FirebaseNearbyUsersModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.last_online.FirebaseLastOnlineModule;
import co.chatsdk.message.file.FileMessageModule;
import co.chatsdk.message.sticker.StickerMessageModule;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.read_receipts.FirebaseReadReceiptsModule;
import co.chatsdk.typing_indicator.FirebaseTypingIndicatorModule;
import co.chatsdk.ui.module.DefaultUIModule;
import co.chatsdk.xmpp.module.XMPPModule;
import co.chatsdk.xmpp.read_receipt.XMPPReadReceiptsModule;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.audio.AudioMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.test.Testing;
import sdk.chat.ui.extras.ExtrasModule;

/**
 * Created by Ben Smiley on 6/8/2014.
 */
//public class MainApplication extends MultiDexApplication {
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String rootPath = "pre_1";

        try {

            ChatSDK.configure(config -> config

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(true)
                    .setRemoteConfigEnabled(true)
                    .setIdenticonType(Config.IdenticonType.Gravatar))

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

                    .addModule(ContactBookModule.shared())
//                    .addModule(EncryptionModule.shared())
                    .addModule(FileMessageModule.shared())
                    .addModule(AudioMessageModule.shared())
                    .addModule(StickerMessageModule.shared())
                    .addModule(VideoMessageModule.shared())
                    .addModule(FirebaseBlockingModule.shared())
                    .addModule(FirebaseLastOnlineModule.shared())
//                    .addModule(FirebaseNearbyUsersModule.shared())
                    .addModule(FirebaseReadReceiptsModule.shared())
                    .addModule(FirebaseTypingIndicatorModule.shared())

//                    .addModule(XMPPReadReceiptsModule.shared())
//                    .addModule(XMPPModule.shared(Testing::myOpenFire, config -> config
//                            .setResetPasswordEnabled(false)))

                    .addModule(ExtrasModule.shared())

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

//            ChatSDK.ui().addChatOption(new MessageTestChatOption("BaseMessage Burst"));

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Error");
            assert(false);
        }

        Disposable d = ChatSDK.events().sourceOnMain().subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(NetworkEvent networkEvent) throws Exception {

            }
        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });

    }

//    @Override
//    protected void attachBaseContext (Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }
}
