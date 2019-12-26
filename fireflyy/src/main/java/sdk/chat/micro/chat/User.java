package sdk.chat.micro.chat;

import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;

import sdk.chat.micro.firebase.service.Keys;
import sdk.chat.micro.namespace.Fly;
import sdk.chat.micro.types.ContactType;
import sdk.chat.micro.types.RoleType;

public class User {

    public String id;
    public RoleType roleType;
    public ContactType contactType;

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

    @Override
    public boolean equals(Object user) {
        if (user instanceof User) {
            return id.equals(((User) user).id);
        }
        return false;
    }

    public boolean isMe() {
        return id.equals(Fly.y.currentUserId());
    }

    public static User currentUser(RoleType role) {
        return new User(Fly.y.currentUserId(), role);
    }

    public static User currentUser() {
        return currentUser(null);
    }

    public interface DataProvider {
        HashMap<String, Object> data(User user);
    }

    public static DataProvider dateDataProvider() {
        return user -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put(Keys.Date, FieldValue.serverTimestamp());
            return data;
        };
    }

    public static DataProvider roleTypeDataProvider() {
        return user -> user.roleType.data();
    }

    public static DataProvider contactTypeDataProvider() {
        return user -> user.contactType.data();
    }

}
