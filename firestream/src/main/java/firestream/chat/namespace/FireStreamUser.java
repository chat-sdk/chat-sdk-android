package firestream.chat.namespace;

import firestream.chat.chat.User;
import firestream.chat.types.RoleType;

public class FireStreamUser extends User {

    public FireStreamUser(String id) {
        super(id);
    }

    public FireStreamUser(String id, RoleType roleType) {
        super(id, roleType);
    }

    public static FireStreamUser fromUser(User user) {
        FireStreamUser mu = new FireStreamUser(user.getId());
        mu.setContactType(user.getContactType());
        mu.setRoleType(user.getRoleType());
        return mu;
    }
}
