package firefly.sdk.chat.events;

import com.google.firebase.firestore.DocumentChange;

public class Event {
    public EventType type;

    public Event(EventType type) {
        this.type = type;
    }


}
