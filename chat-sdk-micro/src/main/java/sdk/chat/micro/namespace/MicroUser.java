package sdk.chat.micro.namespace;

import sdk.chat.micro.User;
import sdk.chat.micro.types.RoleType;

public class MicroUser extends User {

    public MicroUser(String id) {
        super(id);
    }

    public MicroUser(String id, RoleType roleType) {
        super(id, roleType);
    }

    public static MicroUser fromUser(User user) {
        MicroUser mu = new MicroUser(user.id);
        mu.contactType = user.contactType;
        mu.roleType = user.roleType;
        return mu;
    }
}
