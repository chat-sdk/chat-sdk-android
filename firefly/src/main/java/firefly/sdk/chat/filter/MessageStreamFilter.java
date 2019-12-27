package firefly.sdk.chat.filter;

import io.reactivex.functions.Predicate;
import firefly.sdk.chat.message.Sendable;
import firefly.sdk.chat.types.SendableType;

public class MessageStreamFilter {

    public static Predicate<Sendable> bySendableType(final SendableType... types) {
        return s -> {
            for (SendableType type : types) {
                if (s.getType().equals(type.get())) {
                    return true;
                }
            }
            return false;
        };
    }

}
