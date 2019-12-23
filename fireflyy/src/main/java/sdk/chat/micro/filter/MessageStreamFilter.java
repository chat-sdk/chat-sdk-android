package sdk.chat.micro.filter;

import io.reactivex.functions.Predicate;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.types.SendableType;

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
