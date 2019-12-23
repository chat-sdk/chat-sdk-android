package sdk.chat.micro.events;

import sdk.chat.micro.User;
import sdk.chat.micro.firestore.Keys;
import sdk.chat.micro.namespace.MicroUser;
import sdk.chat.micro.types.ContactType;
import sdk.chat.micro.types.RoleType;

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

    public static UserEvent from(ListEvent listEvent) {
        if (listEvent.get(Keys.Role) instanceof String) {
            return new UserEvent(new User(listEvent.id, new RoleType((String) listEvent.get(Keys.Role))), listEvent.type);
        }
        if (listEvent.get(Keys.Type) instanceof String) {
            return new UserEvent(new User(listEvent.id, new ContactType((String) listEvent.get(Keys.Type))), listEvent.type);
        }
        return new UserEvent(new User(listEvent.id), listEvent.type);
    }

    public MicroUser getMicroUser() {
        return MicroUser.fromUser(user);
    }
}
