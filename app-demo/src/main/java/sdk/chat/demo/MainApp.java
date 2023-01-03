package sdk.chat.demo;

import android.app.Application;

import org.pmw.tinylog.Logger;

import sdk.chat.app.firebase.ChatSDKFirebase;
import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Device;
import sdk.chat.firbase.online.FirebaseLastOnlineModule;
import sdk.chat.firebase.blocking.FirebaseBlockingModule;
import sdk.chat.firebase.receipts.FirebaseReadReceiptsModule;
import sdk.chat.message.audio.AudioMessageModule;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            // Setup Chat SDK
            ChatSDKFirebase.quickStartWithEmail(this, "pre_998", "AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE", !Device.honor(), "team@sdk.chat",
                    AudioMessageModule.shared(),
                    FirebaseBlockingModule.shared(),
                    FirebaseReadReceiptsModule.shared(),
                    FirebaseLastOnlineModule.shared(),
                    ContactBookModule.shared()
            );

            ChatSDK.events().sourceOnMain().subscribe(event -> {
                Logger.debug(event);
            });

            ChatSDK.events().errorSourceOnMain().subscribe(event -> {
                Logger.debug(event);
                event.printStackTrace();
            });

        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }
}
