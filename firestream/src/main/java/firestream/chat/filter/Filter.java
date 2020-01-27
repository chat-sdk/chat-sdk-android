package firestream.chat.filter;

import java.util.Arrays;
import java.util.List;

import firestream.chat.events.Event;
import firestream.chat.events.EventType;
import firestream.chat.namespace.Fire;

import io.reactivex.functions.Predicate;
import firestream.chat.message.Sendable;
import firestream.chat.types.SendableType;

public class Filter {

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
        return e -> !e.get().getFrom().equals(Fire.stream().currentUserId());
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

    public static Predicate<Event<? extends Sendable>> and(Predicate<Event<? extends Sendable>>... predicates) {
        return and(Arrays.asList(predicates));
    }

    public static Predicate<Event<? extends Sendable>> and(List<Predicate<Event<? extends Sendable>>> predicates) {
        return event -> {
            for (Predicate<Event<? extends Sendable>> p: predicates) {
                if (!p.test(event)) {
                    return false;
                }
            }
            return true;
        };
    }

}
