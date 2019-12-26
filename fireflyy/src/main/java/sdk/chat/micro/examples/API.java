package sdk.chat.micro.examples;

import android.content.Context;

import io.reactivex.disposables.Disposable;
import sdk.chat.micro.Config;
import sdk.chat.micro.chat.User;
import sdk.chat.micro.namespace.Fly;
import sdk.chat.micro.types.RoleType;

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
        Fly.y.initialize(context, config);

        // Send a message with text
        Disposable d1 = Fly.y.sendMessageWithText("user-id", "Hello").subscribe(s -> {
            // Success
        }, throwable -> {
            // Failure
        });

        // Receive message events
        Disposable d2 = Fly.y.getEvents().getMessages().subscribe(message -> {
            // Handle message
        });

        // Stop receiving message events
        d2.dispose();

        // Create a group chat

        User u1 = new User("user1-id", RoleType.watcher());
        User u2 = new User("user2-id", RoleType.admin());

        Disposable d3 = Fly.y.createChat("Name", "Image URL", u1, u2).subscribe((chat, throwable) -> {
            if (throwable == null) {
                // Success
            } else {
                // Handle error
            }
        });


    }

}
