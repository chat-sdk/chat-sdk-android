package sdk.chat.app.xmpp;

import android.app.Application;

import org.pmw.tinylog.Logger;

import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.receipts.XMPPReadReceiptsModule;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.encryption.EncryptionModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
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
                    .build()

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseUploadModule.shared())
                    .addModule(FirebasePushModule.shared())

                    .addModule(XMPPModule.builder()
                            .setXMPP("185.62.137.45", "bear")
//                            .setXMPP("sysnet-ecs.multidemos.com", "sysnet-ecs.multidemos.com")
                            .setAllowServerConfiguration(false)
                            .setPingInterval(5)
                            .build())

                    .addModule(VideoMessageModule.shared())
                    .addModule(AudioMessageModule.shared())
                    .addModule(StickerMessageModule.shared())
                    .addModule(LocationMessageModule.shared())
                    .addModule(UIModule.builder()
                            .setMessageSelectionEnabled(false)
                            .setUsernameHint("JID")
                            .setResetPasswordEnabled(false)
                            .build())

                    .addModule(XMPPReadReceiptsModule.shared())
                    .addModule(LocationMessageModule.shared())
                    .addModule(ExtrasModule.builder()
                            .setQrCodesEnabled(true)
                            .setDrawerEnabled(false)
                            .build())

                    .addModule(EncryptionModule.shared())

                    .build().activate(this, "Ben");

//            ChatSDK.config().setDebugUsername(Device.honor() ? "a3": "a4");
//            ChatSDK.config().setDebugPassword("123");



        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Error");
            assert(false);
        }


//        ChatSDK.auth().authenticate(AccountDetails.username()).subscribe()

        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {

        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(throwable -> {
            // Catch errors
            throwable.printStackTrace();
        });

    }
}