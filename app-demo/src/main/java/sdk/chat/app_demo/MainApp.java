package sdk.chat.app_demo;

import android.app.Application;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import sdk.chat.core.session.ChatSDK;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ChatSDK.shared().setOnActivateListener(() -> {
            // Pass non-fatal exceptions to Crashlytics
            ChatSDK.events().errorSourceOnMain().doOnNext(throwable -> {
                FirebaseCrashlytics.getInstance().recordException(throwable);
            }).ignoreElements().subscribe(ChatSDK.events());
        });

    }
}
