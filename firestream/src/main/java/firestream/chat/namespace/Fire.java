package firestream.chat.namespace;

import firestream.chat.FireStream;

/**
 * Just a convenience method to make invocations of FireStream more compact
 * Fire.fly.sendMessage()
 * instead of
 * FireStream.shared().sendMessage()
 */
public class Fire {
    public static final FireStream Stream = FireStream.shared();
    public static FireStream api() {
        return FireStream.shared();
    }
}
