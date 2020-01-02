package firestream.chat.message;

import java.util.HashMap;

import firestream.chat.types.SendableType;

public class Message extends Sendable {

    public Message () {
        type = SendableType.Message;
    }

    public Message (HashMap<String, Object> body) {
        this();
        this.body = body;
    }

    public Message (String id, HashMap<String, Object> body) {
        this(body);
        this.id = id;
    }

    public static Message fromSendable(Sendable sendable) {
        Message message = new Message();
        sendable.copyTo(message);
        return message;
    }

}
