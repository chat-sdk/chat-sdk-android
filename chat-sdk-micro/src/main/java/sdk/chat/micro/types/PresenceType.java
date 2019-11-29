package sdk.chat.micro.types;

public class PresenceType extends BaseType {

    public static String Unavailable = "unavailable";
    public static String Busy = "busy";
    public static String ExtendedAway = "xa";
    public static String Available = "available";

    public PresenceType(String type) {
        super(type);
    }

    public PresenceType(BaseType type) {
        super(type);
    }

    public static PresenceType unavailable() {
        return new PresenceType(Unavailable);
    }

    public static PresenceType busy() {
        return new PresenceType(Busy);
    }

    public static PresenceType extendedAway() {
        return new PresenceType(ExtendedAway);
    }

    public static PresenceType available() {
        return new PresenceType(Available);
    }

}
