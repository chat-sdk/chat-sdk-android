package sdk.chat.app.xmpp;

import android.app.Application;

import org.pmw.tinylog.Logger;

import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.receipts.XMPPReadReceiptsModule;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.ui.module.UIModule;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            xmpp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xmpp() {
        try {

            ChatSDK.builder()

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(true)
                    .build()

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseUploadModule.shared())
                    .addModule(FirebasePushModule.shared())

                    .addModule(XMPPModule.builder()
                            .setXMPP("185.62.137.45", "bear")
                            .setAllowServerConfiguration(false)
                            .build())
                    .addModule(UIModule.builder().setUsernameHint("JID").build())
                    .addModule(XMPPReadReceiptsModule.shared())
                    .addModule(LocationMessageModule.shared())

                    .build().activate(this, "Ben");

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Error");
            assert(false);
        }

        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {

        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(throwable -> {
            // Catch errors
            throwable.printStackTrace();
        });

    }
}