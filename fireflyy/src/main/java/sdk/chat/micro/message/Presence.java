package sdk.chat.micro.message;

import com.google.firebase.firestore.Exclude;

import sdk.chat.micro.types.PresenceType;
import sdk.chat.micro.types.SendableType;

public class Presence extends Sendable {

    public static String PresenceKey = "presence";

    public Presence() {
        type = SendableType.Presence;
    }

    public Presence(PresenceType type) {
        this();
        super.setBodyType(type);
    }

    @Exclude
    public PresenceType getBodyType() {
        return new PresenceType(super.getBodyType());
    }

    public static Presence fromSendable(Sendable sendable) {
        Presence presence = new Presence();
        sendable.copyTo(presence);
        return presence;
    }

}
