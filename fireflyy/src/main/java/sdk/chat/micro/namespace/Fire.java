package sdk.chat.micro.namespace;

import sdk.chat.micro.Fireflyy;

/**
 * Just a convenience method to make invocations of Fireflyy more compact
 * Fire.flyy.sendMessage()
 * instead of
 * Fireflyy.shared().sendMessage()
 */
public class Fire {
    public static final Fireflyy flyy = Fireflyy.shared();
}
