package firestream.chat.types;

import java.util.HashMap;

import firestream.chat.chat.User;
import firestream.chat.firebase.service.Keys;

public class RoleType extends BaseType {

    /**
     * They have full access rights, can add and remove admins
     */
    public static String Owner = "owner";

    /**
     * An admin can change the status of any lower member, also update the name, image and custom data
     */
    public static String Admin = "admin";

    /**
     * Standard member of the chat, has write access but can't change roles
     */
    public static String Member = "member";

    /**
     * Read-only access
     */
    public static String Watcher = "watcher";

    /**
     * Cannot access the chat, cannot be added
     */
    public static String Banned = "banned";

    public RoleType(String type) {
        super(type);
    }

    public RoleType(BaseType type) {
        super(type);
    }

    public static RoleType owner() {
        return new RoleType(Owner);
    }

    public static RoleType admin() {
        return new RoleType(Admin);
    }

    public static RoleType member() {
        return new RoleType(Member);
    }

    public static RoleType watcher() {
        return new RoleType(Watcher);
    }

    public static RoleType banned() {
        return new RoleType(Banned);
    }

    public HashMap<String, Object> data () {
        HashMap<String, Object> data = new HashMap<>();
        data.put(Keys.Role, get());
        return data;
    }

    public boolean test(User user) {
        return user.roleType.toLevel() <= toLevel();
    }

    protected int toLevel() {
        if (type.equals(Owner)) {
            return 0;
        }
        if (type.equals(Admin)) {
            return 1;
        }
        if (type.equals(Member)) {
            return 2;
        }
        if (type.equals(Watcher)) {
            return 3;
        }
        if (type.equals(Banned)) {
            return 4;
        }
        return 5;
    }
}
