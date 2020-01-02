package firestream.chat.events;

import java.util.Date;

import firestream.chat.chat.Chat;
import firestream.chat.firebase.service.Keys;

public class ChatEvent extends Event {

    public Chat chat;

    public ChatEvent(Chat chat, EventType type) {
        super(type);
        this.chat = chat;
    }

    public static ChatEvent added(Chat chat) {
        return new ChatEvent(chat, EventType.Added);
    }

    public static ChatEvent removed(Chat chat) {
        return new ChatEvent(chat, EventType.Removed);
    }

    public static ChatEvent modified(Chat chat) {
        return new ChatEvent(chat, EventType.Modified);
    }

    public static ChatEvent from(ListEvent listEvent) {
        if (listEvent.get(Keys.Date) instanceof Date) {
            return new ChatEvent(new Chat(listEvent.id, (Date) listEvent.get(Keys.Date)), listEvent.type);
        }
        return new ChatEvent(new Chat(listEvent.id), listEvent.type);
    }
}
