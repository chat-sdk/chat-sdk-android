package firestream.chat.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import firestream.chat.R;
import firestream.chat.firebase.service.Keys;
import firestream.chat.namespace.Fire;

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

    public Map<String, Object> data () {
        Map<String, Object> data = new HashMap<>();
        data.put(Keys.Role, get());
        return data;
    }

    public boolean ge(RoleType permission) {
        return toLevel() <= permission.toLevel();
    }

    protected int toLevel() {
        if (type != null) {
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
        }
        return 5;
    }

    public String stringValue() {
        int resId = -1;

        if (equals(RoleType.owner())) {
            resId = R.string.owner;
        }
        if (equals(RoleType.admin())) {
            resId = R.string.admin;
        }
        if (equals(RoleType.member())) {
            resId = R.string.member;
        }
        if (equals(RoleType.watcher())) {
            resId = R.string.watcher;
        }
        if (equals(RoleType.banned())) {
            resId = R.string.banned;
        }

        if (resId != -1) {
            return Fire.internal().context().getString(resId);
        }
        return null;
    }

    public static List<String> allStringValues() {
        return allStringValuesExcluding();
    }

    public static List<String> allStringValuesExcluding(RoleType... excluding) {
        return rolesToStringValues(allExcluding(excluding));
    }

    public static List<RoleType> all() {
        return allExcluding();
    }

    public static List<RoleType> allExcluding(RoleType... excluding) {
        List<RoleType> list = new ArrayList<RoleType>() {{
            add(RoleType.owner());
            add(RoleType.admin());
            add(RoleType.member());
            add(RoleType.watcher());
            add(RoleType.banned());
        }};
        for (RoleType rt: excluding) {
            list.remove(rt);
        }
        return list;
    }

    public static List<String> rolesToStringValues(List<RoleType> roleTypes) {
        List<String> stringValues = new ArrayList<>();

        for (RoleType rt: roleTypes) {
            stringValues.add(rt.stringValue());
        }

        return stringValues;
    }

    public static Map<String, RoleType> reverseMap() {
        Map<String, RoleType> map = new HashMap<>();
        for (RoleType rt: all()) {
            map.put(rt.stringValue(), rt);
        }
        return map;
    }

    @Override
    public boolean equals(Object roleType) {
        return roleType instanceof RoleType && get().equals(((RoleType)roleType).get());
    }
}
