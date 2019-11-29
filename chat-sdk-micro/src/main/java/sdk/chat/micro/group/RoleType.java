package sdk.chat.micro.group;

import sdk.chat.micro.types.BaseType;

public class RoleType extends BaseType {

    public static String Owner = "owner";
    public static String Admin = "admin";
    public static String Member = "Member";

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
}
