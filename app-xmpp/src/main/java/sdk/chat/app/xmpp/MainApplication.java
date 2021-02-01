package sdk.chat.app.xmpp;

import android.app.Application;

import org.pmw.tinylog.Logger;

import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.receipts.XMPPReadReceiptsModule;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.ui.activities.thread.details.ThreadDetailsActivity;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        xmpp();
    }

    public void xmpp() {
        try {


            ChatSDK.builder()

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(false)
                    .setThreadDestructionEnabled(false)
                    .build()

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseUploadModule.shared())
                    .addModule(FirebasePushModule.shared())

                    .addModule(XMPPModule.builder()
                            .setXMPP("xmpp.app", "xmpp.app")
//                            .setXMPP("sysnet-ecs.multidemos.com", "sysnet-ecs.multidemos.com")
                            .setAllowServerConfiguration(false)
                            .setPingInterval(5)
                            .build())

                    .addModule(AudioMessageModule.shared())
                    .addModule(LocationMessageModule.shared())
                    .addModule(VideoMessageModule.shared())
                    .addModule(StickerMessageModule.builder()
                            .build())
                    .addModule(UIModule.builder()
                            .setMessageSelectionEnabled(false)
                            .setUsernameHint("JID")
                            .setResetPasswordEnabled(false)
                            .build())

                    .addModule(XMPPReadReceiptsModule.shared())
                    .addModule(ExtrasModule.builder()
                            .setQrCodesEnabled(true)
                            .setDrawerEnabled(false)
                            .build())

//                    .addModule(EncryptionModule.shared())

                    .build().activate(this, "Ben");

//            ChatSDK.config().setDebugUsername(Device.honor() ? "a3": "a4");
//            ChatSDK.config().setDebugPassword("123");

            ChatSDK.ui().setThreadDetailsActivity(ThreadDetailsActivity.class);


        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Error");
            assert(false);
        }




//        ChatSDK.auth().authenticate(AccountDetails.username()).subscribe()

        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
            networkEvent.debug();
        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(throwable -> {
            // Catch errors
            throwable.printStackTrace();
        });

    }
}