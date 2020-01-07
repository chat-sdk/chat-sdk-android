package firestream.chat.filter;

import firestream.chat.events.EventType;
import firestream.chat.events.SendableEvent;
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
        return s -> !s.getFrom().equals(Fire.Stream.currentUserId());
    }

    public static Predicate<SendableEvent> byEventType(final EventType... types) {
        return s -> {
            for (EventType type : types) {
                if (s.getType().equals(type)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<SendableEvent> eventBySendableType(final SendableType... types) {
        return s -> {
            for (SendableType type : types) {
                if (s.getSendable().getType().equals(type.get())) {
                    return true;
                }
            }
            return false;
        };
    }

}
