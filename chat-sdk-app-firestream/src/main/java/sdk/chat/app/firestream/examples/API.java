package sdk.chat.app.firestream.examples;

import android.content.Context;

import firestream.chat.FirestreamConfig;
import firestream.chat.chat.User;
import firestream.chat.firestore.FirestoreService;
import firestream.chat.interfaces.IChat;
import firestream.chat.interfaces.IFireStream;
import firestream.chat.namespace.Fire;
import firestream.chat.realtime.RealtimeService;
import firestream.chat.types.RoleType;
import io.reactivex.disposables.Disposable;

public class API {

    public API(Context context) {

        // Configure the chat
        FirestreamConfig<IFireStream> config = new FirestreamConfig<>(Fire.stream());

        try {
            // Set the root path
            config.setRoot("messenger");

            // Set the sandbox
            config.setSandbox("dev");
        } catch (Exception e) {
            // Handle Exception
        }

        config.autoAcceptChatInvite = false;

        // etc...

        // Initialize
        Fire.stream().initialize(context, config, new RealtimeService());

        // or
        Fire.stream().initialize(context, config, new FirestoreService());

        // Send a message from text
        Disposable d1 = Fire.stream().sendMessageWithText("user-id", "Hello", messageId -> {
            // Handle Message ID
        }).subscribe(() -> {
            // Complete
        }, throwable -> {
            // Error
        });

        // Receive message events
        Disposable d2 = Fire.stream().getSendableEvents().getMessages().subscribe(message -> {
            // Handle message
        });

        // Stop receiving message events
        d2.dispose();

        // Create a group chat

        User u1 = new User("user1-id", RoleType.watcher());
        User u2 = new User("user2-id", RoleType.admin());

        Disposable d3 = Fire.stream().createChat("Name", "Image URL", u1, u2).subscribe((chat, throwable) -> {
            if (throwable == null) {
                // Success
            } else {
                // Handle error
            }
        });

        Disposable d4 = Fire.stream().getChatEvents().subscribe(chatEvent -> {
            IChat chat = chatEvent.get();
            if (chatEvent.isAdded()) {
                // A chat was added!

                // Get the chat to dispose of the disposable when we log out or leave
                chat.manage(chat.getUserEvents().subscribe(userEvent -> {
                    User user = userEvent.get();
                    if (userEvent.isAdded()) {
                        // Get the role of the user
                        RoleType role = user.getRoleType();

                    }
                    if (userEvent.isRemoved()) {

                    }
                }));

                // Get the name
                chat.manage(chat.getNameChangeEvents().subscribe(s -> {
                    // The name changed!
                }));
                chat.manage(chat.getImageURLChangeEvents().subscribe(s -> {
                    // The image url changed!
                }));

            }
            if (chatEvent.isRemoved()) {
                // A chat was removed
            }
        });
        // Get a


    }

}
