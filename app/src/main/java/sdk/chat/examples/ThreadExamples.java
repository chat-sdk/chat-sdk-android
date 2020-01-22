package sdk.chat.examples;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.GoogleUtils;
import sdk.chat.examples.BaseExample;

public class ThreadExamples extends BaseExample {
    public ThreadExamples(Context context, ArrayList<User> users) {

        // Create a thread
        dm.add(ChatSDK.thread().createThread("Name", users, ThreadType.PrivateGroup, "custom-id", "http://image-url").subscribe(thread -> {

            // Launch the thread activity
            ChatSDK.ui().startChatActivityForID(context, thread.getEntityID());

            User user1 = ChatSDK.db().fetchUserWithEntityID("User1 ID");
            User user2 = ChatSDK.db().fetchUserWithEntityID("User2 ID");
            User user3 = ChatSDK.db().fetchUserWithEntityID("User3 ID");

            // Add users
            if (ChatSDK.thread().addUsersEnabled(thread)) {
                dm.add(ChatSDK.thread().addUsersToThread(thread, user1, user2).subscribe(() -> {

                }, this));
            }

            // Remove users
            if (ChatSDK.thread().removeUsersEnabled(thread)) {
                dm.add(ChatSDK.thread().removeUsersFromThread(thread, user1, user2).subscribe(() -> {

                }, this));
            }

            // Load some historic messages
            dm.add(ChatSDK.thread().loadMoreMessagesForThread(new Date(), thread, true).subscribe(messages -> {

                // Forward a message
                dm.add(ChatSDK.thread().forwardMessage(thread, messages.get(0)).subscribe(() -> {

                }, this));

                // Reply to a message
                dm.add(ChatSDK.thread().replyToMessage(thread, messages.get(0), "Your Reply").subscribe(() -> {

                }, this));

                // Delete message
                dm.add(ChatSDK.thread().deleteMessage(messages.get(0)).subscribe(() -> {

                }, this));

            }, this));

            // Send a text message
            dm.add(ChatSDK.thread().sendMessageWithText("Hello", thread).subscribe(() -> {

            }, this));

            // Send an image file
            File imageFile = new File("/path/to/your/image.png");
            dm.add(ChatSDK.imageMessage().sendMessageWithImage(imageFile, thread).subscribe(() -> {

            }, this));

            LatLng latLng = new LatLng(0.0d, 1.1d);
            String imageURL = GoogleUtils.getMapImageURL(latLng, 100, 100);

            // Send a location
            dm.add(ChatSDK.locationMessage().sendMessageWithLocation(imageURL, latLng, thread).subscribe(() -> {

            }, this));

            // Set custom data on thread
            thread.setMetaValue("Value", "Key");
            dm.add(ChatSDK.thread().pushThread(thread).subscribe(() -> {

                // Get data
                String value = thread.metaValueForKey("Key").getValue();

            }, this));

        }, this));


        // Create a thread
        dm.add(ChatSDK.thread().createThread(users).subscribe(thread -> {

        }, this));

    }
}
