package firestream.chat.namespace;

import firestream.chat.FireStream;
import firestream.chat.interfaces.IFireStream;

/**
 * Just a convenience method to make invocations of FireStream more compact
 * Fire.fly.sendMessage()
 * instead of
 * FireStream.shared().sendMessage()
 */
public class Fire {
    public static FireStream internal() {
        return FireStream.shared();
    }
    public static IFireStream stream() {
        return FireStream.shared();
    }
}
