package sdk.chat.examples;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.functions.Predicate;
import sdk.chat.examples.BaseExample;

public class UserExamples extends BaseExample {

    public UserExamples() {

        // Get the current user
        User currentUser = ChatSDK.core().currentUser();

        // Add some custom data
        currentUser.setMetaValue("Key", "Value");

        // Update the user's details on Firebase
        dm.add(ChatSDK.core().pushUser().subscribe(() -> {

        }, this));

        // Go online or offline
        ChatSDK.core().goOffline();
        ChatSDK.core().goOnline();

        // Set the user's presence
        dm.add(ChatSDK.core().setUserOnline().subscribe(() -> {
            // Completion
        }, this));

        dm.add(ChatSDK.core().setUserOnline().subscribe(() -> {
            // Completion
        }, this));

        // If you need a user for a given ID
        dm.add(ChatSDK.core().getUserForEntityID("user-entity-id").subscribe(user -> {

            String name = user.getName();
            String avatarURL = user.getAvatarURL();
            String customString = user.metaStringForKey("Key");

            // etc...
        }, this));

        // User Events
        Predicate<NetworkEvent> filter = NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated);
        dm.add(ChatSDK.events().sourceOnMain().filter(filter).subscribe(networkEvent -> {
            // Handle event
        }));

    }

}
