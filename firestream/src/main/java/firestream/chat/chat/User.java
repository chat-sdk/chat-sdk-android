package firestream.chat.chat;

import java.util.HashMap;
import java.util.Map;

import firestream.chat.events.ListData;
import firestream.chat.firebase.service.Keys;
import firestream.chat.namespace.Fire;
import firestream.chat.types.ContactType;
import firestream.chat.types.RoleType;
import sdk.guru.common.Event;

public class User {

    protected String id;
    protected RoleType roleType;
    protected ContactType contactType;

    public User(String id) {
        this.id = id;
    }

    public User(String id, RoleType roleType) {
        this(id);
        this.roleType = roleType;
    }

    public User(String id, ContactType contactType) {
        this(id);
        this.contactType = contactType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public void setContactType(ContactType contactType) {
        this.contactType = contactType;
    }

    public boolean equalsRoleType(RoleType rt) {
        return this.roleType.equals(rt);
    }

    public boolean equalsRoleType(User user) {
        return this.roleType.equals(user.getRoleType());
    }

    public boolean equalsContactType(User user) {
        return this.contactType.equals(user.getContactType());
    }

    public boolean equalsContactType(ContactType ct) {
        return this.contactType.equals(ct);
    }

    @Override
    public boolean equals(Object user) {
        if (user instanceof User) {
            return id.equals(((User) user).id);
        }
        return false;
    }

    public boolean isMe() {
        return id.equals(Fire.stream().currentUserId());
    }

    public static User currentUser(RoleType role) {
        return new User(Fire.stream().currentUserId(), role);
    }

    public static User currentUser() {
        return currentUser(null);
    }

    public interface DataProvider {
        Map<String, Object> data(User user);
    }

    public static DataProvider dateDataProvider() {
        return user -> {
            Map<String, Object> data = new HashMap<>();
            data.put(Keys.Date, Fire.stream().getFirebaseService().core.timestamp());
            return data;
        };
    }

    public static DataProvider roleTypeDataProvider() {
        return user -> user.roleType.data();
    }

    public static DataProvider contactTypeDataProvider() {
        return user -> user.contactType.data();
    }

    public static User from(Event<ListData> event) {
        if (event.get().get(Keys.Role) instanceof String) {
            return new User(event.get().getId(), new RoleType((String) event.get().get(Keys.Role)));
        }
        if (event.get().get(Keys.Type) instanceof String) {
            return new User(event.get().getId(), new ContactType((String) event.get().get(Keys.Type)));
        }
        return new User(event.get().getId());
    }

}
