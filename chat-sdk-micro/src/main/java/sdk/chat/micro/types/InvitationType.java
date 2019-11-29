package sdk.chat.micro.types;

public class InvitationType extends BaseType {

    public static String Group = "group";

    public InvitationType(String type) {
        super(type);
    }

    public InvitationType(BaseType type) {
        super(type);
    }

    public static InvitationType group() {
        return new InvitationType(Group);
    }

}
