package sdk.chat.demo.examples.override;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.examples.override.view.CustomPostRegistrationActivity;
import sdk.chat.demo.examples.override.view.CustomPrivateThreadsFragment;

public class OverrideViewExample {

    public static void run() {

        ////
        //// Override an activity
        ////

        // Now we are going to override the PostRegistrationActivity
        // Note our CustomPostRegistrationActivity is a subclass of the PostRegistrationActivity
        ChatSDK.ui().setPostRegistrationActivity(CustomPostRegistrationActivity.class);

        ////
        //// Override a fragment
        ////

        // Now we are going to override the PostRegistrationActivity
        // Note our CustomPostRegistrationActivity is a subclass of the PostRegistrationActivity
        ChatSDK.ui().setPrivateThreadsFragment(new CustomPrivateThreadsFragment());

    }


}
