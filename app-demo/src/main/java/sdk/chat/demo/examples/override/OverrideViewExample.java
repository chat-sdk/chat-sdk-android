package sdk.chat.demo.examples.override;

import sdk.chat.demo.examples.override.view.CustomPostRegistrationActivity;
import sdk.chat.demo.examples.override.view.CustomPrivateThreadsFragment;
import sdk.chat.ui.ChatSDKUI;

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


    }


}
