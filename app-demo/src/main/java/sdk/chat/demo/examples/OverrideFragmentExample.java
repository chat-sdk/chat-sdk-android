package sdk.chat.demo.examples;

import android.content.Context;

import sdk.chat.app.firebase.ChatSDKFirebase;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.examples.override.fragment.CustomPrivateThreadsFragment;

public class OverrideFragmentExample {

    public static void run(Context context) {

        try {
            // Setup Chat SDK
            ChatSDKFirebase.quickStart(context, "pre_1", "your_key_here", false);

            // Now we are going to override the PostRegistrationActivity
            // Note our CustomPostRegistrationActivity is a subclass of the PostRegistrationActivity
            ChatSDK.ui().setPrivateThreadsFragment(new CustomPrivateThreadsFragment());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
