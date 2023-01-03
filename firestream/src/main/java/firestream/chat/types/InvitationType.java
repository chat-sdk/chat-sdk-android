package firestream.chat.types;

public class InvitationType extends BaseType {

    public static String Chat = "chat";

    public InvitationType(String type) {
        super(type);
    }

    public InvitationType(BaseType type) {
        super(type);
    }

    public static InvitationType chat() {
        return new InvitationType(Chat);
    }

}
