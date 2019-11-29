package sdk.chat.micro.message;

import sdk.chat.micro.types.PresenceType;
import sdk.chat.micro.types.SendableType;

public class Presence extends Sendable {

    public static String PresenceKey = "presence";

    public Presence() {
        type = SendableType.Presence;
    }

    public Presence(PresenceType type) {
        this();
        super.setType(type);
    }

    public PresenceType getBodyType() {
        return new PresenceType(super.getBodyType());
    }

}
