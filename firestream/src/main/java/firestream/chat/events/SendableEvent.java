package firestream.chat.events;

import com.google.firebase.database.annotations.NotNull;

import firestream.chat.message.Sendable;

public class SendableEvent extends Event {

    protected Sendable sendable;

    public SendableEvent(@NotNull Sendable sendable, EventType type) {
        super(type);
        this.sendable = sendable;
    }

    public static SendableEvent added(@NotNull Sendable sendable) {
        return new SendableEvent(sendable, EventType.Added);
    }

    public static SendableEvent removed(@NotNull Sendable sendable) {
        return new SendableEvent(sendable, EventType.Removed);
    }

    public static SendableEvent modified(@NotNull Sendable sendable) {
        return new SendableEvent(sendable, EventType.Modified);
    }

    public Sendable getSendable() {
        return sendable;
    }

}
