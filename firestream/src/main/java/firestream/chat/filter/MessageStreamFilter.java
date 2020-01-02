package firestream.chat.filter;

import firestream.chat.namespace.Fire;

import io.reactivex.functions.Predicate;
import firestream.chat.message.Sendable;
import firestream.chat.types.SendableType;

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

    public static Predicate<Sendable> notFromMe() {
        return s -> !s.from.equals(Fire.Stream.currentUserId());
    }

}
