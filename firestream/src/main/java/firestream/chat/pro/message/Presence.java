package firestream.chat.pro.message;

import firestream.chat.message.Sendable;
import firestream.chat.pro.types.PresenceType;
import firestream.chat.types.SendableType;

public class Presence extends Sendable {

    public static String PresenceKey = "presence";

    public Presence() {
        type = SendableType.Presence;
    }

    public Presence(PresenceType type) {
        this();
        super.setBodyType(type);
    }

    public PresenceType getBodyType() {
        return new PresenceType(super.getBodyType());
    }

    public static Presence fromSendable(Sendable sendable) {
        Presence presence = new Presence();
        sendable.copyTo(presence);
        return presence;
    }

}
