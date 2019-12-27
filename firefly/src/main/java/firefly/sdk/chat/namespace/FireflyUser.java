package firefly.sdk.chat.namespace;

import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.types.RoleType;

public class FireflyUser extends User {

    public FireflyUser(String id) {
        super(id);
    }

    public FireflyUser(String id, RoleType roleType) {
        super(id, roleType);
    }

    public static FireflyUser fromUser(User user) {
        FireflyUser mu = new FireflyUser(user.id);
        mu.contactType = user.contactType;
        mu.roleType = user.roleType;
        return mu;
    }
}
