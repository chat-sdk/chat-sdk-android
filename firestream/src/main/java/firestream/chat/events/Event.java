package firestream.chat.events;

import androidx.annotation.NonNull;

import com.google.firebase.database.annotations.NotNull;

public class Event<T> {

    protected T payload;
    protected EventType type;

    public Event(T payload, EventType type) {
        this.payload = payload;
        this.type = type;
    }

    public Event(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public boolean typeIs(EventType type) {
        return this.type == type;
    }

    public T get() {
        return payload;
    }

    public static <T> Event<T> added(@NonNull T payload) {
        return new Event<>(payload, EventType.Added);
    }

    public static <T> Event<T> removed(@NotNull T payload) {
        return new Event<>(payload, EventType.Removed);
    }

    public static <T> Event<T> modified(@NotNull T payload) {
        return new Event<>(payload, EventType.Modified);
    }

    public <W> Event<W> to(@NonNull W payload) {
        return new Event<>(payload, type);
    }

}
