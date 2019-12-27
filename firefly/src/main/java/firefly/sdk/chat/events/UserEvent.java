package firefly.sdk.chat.events;

import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.namespace.FireflyUser;
import firefly.sdk.chat.types.ContactType;
import firefly.sdk.chat.types.RoleType;

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

    public FireflyUser getFireflyUser() {
        return FireflyUser.fromUser(user);
    }
}
