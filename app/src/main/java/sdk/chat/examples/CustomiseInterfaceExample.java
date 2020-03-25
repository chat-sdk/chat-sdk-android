package sdk.chat.examples;

import android.content.Context;
import android.graphics.Color;

import co.chatsdk.android.app.R;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Config;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firestream.FireStreamNetworkAdapter;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.module.DefaultUIModule;
import sdk.chat.custom.AChatActivity;
import sdk.chat.custom.APrivateThreadsFragment;
import sdk.chat.test.MessageTestChatOption;
import sdk.chat.ui.extras.ExtrasModule;

public class CustomiseInterfaceExample extends BaseExample {
    public CustomiseInterfaceExample(Context context) {

        // There are many more configuration options,
        // explore them yourself by looking at the Configuration builder.
        try {

            ChatSDK.configure(config -> config

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(true)
                    .setLogoDrawableResourceID(R.drawable.ic_launcher_big))

                    // Add the network adapter module
                    .addModule(FirebaseModule.shared(config -> config
                            .setFirebaseRootPath("rootPath")
                    ))

                    // Add the UI module
                    .addModule(DefaultUIModule.shared(config -> config
                            .setPublicRoomCreationEnabled(true)
                            .setPublicChatRoomLifetimeMinutes(60 * 24)
                            .setTheme(R.style.CustomChatSDKTheme)
                    ))

                    .addModule(ExtrasModule.shared(config -> config
                            .setDrawerEnabled(true)
                    ))

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseFileStorageModule.shared())
                    .addModule(FirebasePushModule.shared())
                    .addModule(ProfilePicturesModule.shared())

//                    .addModule(FirestreamModule.shared(config -> config
//                            .setRoot(rootPath)
//                            .setStartListeningFromLastSentMessageDateEnabled(false)
//                            .setListenToMessagesWithTimeAgo(FirestreamConfig.TimePeriod.days(7))
//                            .setDatabaseType(FirestreamConfig.DatabaseType.Realtime)
//                            .setDeleteMessagesOnReceiptEnabled(false)
//                            .setDeliveryReceiptsEnabled(false)
//                    ))

//                     Enable Firebase UI
//                    .addModule(FirebaseUIModule.shared(config -> config
//                            .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
//                    ))

                    // Activate
                    .activate(context);

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
            assert(false);
            // Handle error
        }


        // Override some activities



    }
}
