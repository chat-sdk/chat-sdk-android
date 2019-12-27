package firefly.sdk.chat.namespace;

import firefly.sdk.chat.Firefly;

/**
 * Just a convenience method to make invocations of Firefly more compact
 * Fire.fly.sendMessage()
 * instead of
 * Firefly.shared().sendMessage()
 */
public class Fire {
    public static final Firefly fly = Firefly.shared();
}
