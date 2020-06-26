package sdk.chat.demo;

import android.app.Application;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.pmw.tinylog.Logger;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.module.UIModule;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Add this in case the app is killed while in background
        DemoConfigBuilder.shared().load(this);
        if (DemoConfigBuilder.shared().isConfigured() && DemoConfigBuilder.shared().isValid()) {
            try {
                DemoConfigBuilder.shared().setupChatSDK(this);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }


//        ChatSDK.shared().setOnActivateListener(() -> {
//            ChatSDK.hook().addHook(Hook.sync(data -> ChatSDK.core().getUserForEntityID("4hekpBRhB6gO03EvUwcl53W9xX83").subscribe(user -> {
//                Logger.debug("");
//            })), HookEvent.DidAuthenticate);
//        });

    }


}
