package firestream.chat.filter;

import firestream.chat.events.Event;
import firestream.chat.events.EventType;
import firestream.chat.namespace.Fire;

import io.reactivex.functions.Predicate;
import firestream.chat.message.Sendable;
import firestream.chat.types.SendableType;

public class MessageStreamFilter {

    public static Predicate<Event<? extends Sendable>> bySendableType(final SendableType... types) {
        return e -> {
            for (SendableType type : types) {
                if (e.get().getType().equals(type.get())) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<Event<? extends Sendable>> notFromMe() {
        return e -> !e.get().getFrom().equals(Fire.Stream.currentUserId());
    }

    public static Predicate<Event<? extends Sendable>> byEventType(final EventType... types) {
        return e -> {
            for (EventType type : types) {
                if (e.getType().equals(type)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<Event<? extends Sendable>> eventBySendableType(final SendableType... types) {
        return e -> {
            for (SendableType type : types) {
                if (e.get().getType().equals(type.get())) {
                    return true;
                }
            }
            return false;
        };
    }

}
