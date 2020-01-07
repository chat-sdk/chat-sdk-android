package firestream.chat.events;

public class Event {
    protected EventType type;

    public Event(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public boolean typeIs(EventType type) {
        return this.type == type;
    }

}
