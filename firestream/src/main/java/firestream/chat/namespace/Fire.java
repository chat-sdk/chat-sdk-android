package firestream.chat.namespace;

import firestream.chat.FireStream;
import firestream.chat.interfaces.IFireStream;
import firestream.chat.pro.FireStreamPro;
import firestream.chat.pro.interfaces.IFirestreamPro;

/**
 * Just a convenience method to make invocations of FireStream more compact
 * Fire.fly.sendMessage()
 * instead of
 * FireStream.shared().sendMessage()
 */
public class Fire {

    public static FireStream instance;

    public static FireStream internal() {
        return FireStream.shared();
    }
    public static IFireStream stream() {
        if (instance == null) {
            instance = FireStream.shared();
        }
        return instance;
    }
    public static IFirestreamPro pro() {
        if (instance == null) {
            instance = FireStreamPro.shared();
        }
        if (instance instanceof FireStreamPro) {
            return (FireStreamPro) instance;
        }
        return null;
    }
}
