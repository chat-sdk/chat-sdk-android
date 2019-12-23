package sdk.chat.micro.examples;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import sdk.chat.micro.User;
import sdk.chat.micro.chat.Chat;
import sdk.chat.micro.namespace.Fly;
import sdk.chat.micro.types.RoleType;

public class API {

    public API() {

        // Initialize
        Fly.y.initialize();

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
