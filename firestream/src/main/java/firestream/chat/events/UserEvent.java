package firestream.chat.events;

import firestream.chat.chat.User;
import firestream.chat.namespace.FireStreamUser;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;

public class UserEvent extends Event {

    public User user;

    public UserEvent(User user, EventType type) {
        super(type);
        this.user = user;
    }

    public static UserEvent added(User user) {
        return new UserEvent(user, EventType.Added);
    }

    public static UserEvent removed(User user) {
        return new UserEvent(user, EventType.Removed);
    }

    public static UserEvent modified(User user) {
        return new UserEvent(user, EventType.Modified);
    }

    public FireStreamUser getFireStreamUser() {
        return FireStreamUser.fromUser(user);
    }
}
