package firestream.chat.events;

import firestream.chat.chat.User;
import firestream.chat.firebase.service.Keys;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.ContactType;
import firestream.chat.types.RoleType;

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

    public FireStreamUser getFireStreamUser() {
        return FireStreamUser.fromUser(user);
    }
}
