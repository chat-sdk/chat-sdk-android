package firefly.sdk.chat.examples;

import android.content.Context;

import io.reactivex.disposables.Disposable;
import firefly.sdk.chat.Config;
import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.RoleType;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class API {

    public API(Context context) {

        // Configure the chat
        Config config = new Config();

        config.root = "messenger";
        config.sandbox = "dev";
        config.messageHistoryLimit = 100;
        config.autoAcceptChatInvite = false;

        // etc...

        // Initialize
        Fl.y.initialize(context, config);

        // Send a message with text
        Disposable d1 = Fl.y.sendMessageWithText("user-id", "Hello", messageId -> {
            // Handle Message ID
        }).subscribe(() -> {
            // Complete
        }, throwable -> {
            // Error
        });

        // Receive message events
        Disposable d2 = Fl.y.getEvents().getMessages().subscribe(message -> {
            // Handle message
        });

        // Stop receiving message events
        d2.dispose();

        // Create a group chat

        User u1 = new User("user1-id", RoleType.watcher());
        User u2 = new User("user2-id", RoleType.admin());

        Disposable d3 = Fl.y.createChat("Name", "Image URL", u1, u2).subscribe((chat, throwable) -> {
            if (throwable == null) {
                // Success
            } else {
                // Handle error
            }
        });


    }

}
