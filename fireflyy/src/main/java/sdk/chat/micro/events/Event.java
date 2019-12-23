package sdk.chat.micro.events;

import com.google.firebase.firestore.DocumentChange;

public class Event {
    public EventType type;

    public Event(EventType type) {
        this.type = type;
    }

    public static EventType typeForDocumentChange(DocumentChange change) {
        switch (change.getType()) {
            case ADDED:
                return EventType.Added;
            case REMOVED:
                return EventType.Removed;
            case MODIFIED:
                return EventType.Modified;
            default:
                return null;
        }
    }
}
