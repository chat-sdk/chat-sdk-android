package sdk.chat.demo.examples;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.examples.helper.CustomPostRegistrationActivity;
import sdk.chat.demo.examples.helper.CustomPrivateThreadsFragment;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.activities.MainActivity;

public class OverrideViewExample {

    public static void run() {

        ////
        //// Override an activity
        ////

        // Now we are going to override the PostRegistrationActivity
        // Note our CustomPostRegistrationActivity is a subclass of the PostRegistrationActivity
        ChatSDKUI.setPostRegistrationActivity(CustomPostRegistrationActivity.class);

        ////
        //// Override a fragment
        ////

        // Now we are going to override the PostRegistrationActivity
        // Note our CustomPostRegistrationActivity is a subclass of the PostRegistrationActivity
        ChatSDKUI.setPrivateThreadsFragment(new CustomPrivateThreadsFragment());

        // If you are overriding the ChatActivity, you will also need to define the main activity for your app
        ChatSDK.ui().setMainActivity(MainActivity.class);


    }


}
