package sdk.guru.common;

import androidx.annotation.NonNull;

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

    public static <T> Event<T> removed(@NonNull T payload) {
        return new Event<>(payload, EventType.Removed);
    }

    public static <T> Event<T> modified(@NonNull T payload) {
        return new Event<>(payload, EventType.Modified);
    }

    public <W> Event<W> to(@NonNull W payload) {
        return new Event<>(payload, type);
    }

    public boolean isAdded() {
        return typeIs(EventType.Added);
    }

    public boolean isRemoved() {
        return typeIs(EventType.Removed);
    }

    public boolean isModified() {
        return typeIs(EventType.Modified);
    }

}
