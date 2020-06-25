package sdk.chat.demo;

import android.app.Application;

import sdk.chat.app.firebase.ChatSDKFirebase;
import sdk.chat.demo.examples.OverrideViewExample;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            // Setup Chat SDK
            ChatSDKFirebase.quickStart(this, "pre_1", "your_key_here", false);

            OverrideViewExample.run();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
