package sdk.chat.examples;

import android.content.Context;
import android.graphics.Color;

import co.chatsdk.android.app.R;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firestream.FireStreamNetworkAdapter;
import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.ui.icons.Icons;
import sdk.chat.custom.AChatActivity;
import sdk.chat.custom.APrivateThreadsFragment;
import sdk.chat.test.MessageTestChatOption;

public class CustomiseInterfaceExample extends BaseExample {
    public CustomiseInterfaceExample(Context context) {

        // Set a custom theme!
        Configuration.Builder builder = new Configuration.Builder();
        builder.setTheme(R.style.CustomChatSDKTheme);

        // Set a logo
        builder.logoDrawableResourceID(R.drawable.ic_launcher_big);

        // Set the message color
        builder.messageColorMe(Color.RED);
        builder.messageColorReply("#334455");

        // There are many more configuration options,
        // explore them yourself by looking at the Configuration builder.
        try {
            ChatSDK.initialize(context, builder.build(), FireStreamNetworkAdapter.class, BaseInterfaceAdapter.class);

            // Override the chat activity
            // All activities can be overridden
            ChatSDK.ui().setChatActivity(AChatActivity.class);

            // Override a fragment
            // All fragments can be overridden
            ChatSDK.ui().setPrivateThreadsFragment(new APrivateThreadsFragment());

            // Add an extra tab
            ChatSDK.ui().addTab("Title", Icons.get(Icons.choose().search, R.color.gray), new APrivateThreadsFragment());

            // Remove a tab
            ChatSDK.ui().removeTab(0);

            // Define custom chat options that are displayed
            // in the chat activity when the options button is pressed
            ChatSDK.ui().addChatOption(new MessageTestChatOption("Title", null));

            // Define when notifications are shown
            ChatSDK.ui().setLocalNotificationHandler(thread -> {
                if (thread.typeIs(ThreadType.Private1to1)) {
                    return true;
                }
                return false;
            });



        } catch (Exception e) {

        }


        // Override some activities



    }
}
