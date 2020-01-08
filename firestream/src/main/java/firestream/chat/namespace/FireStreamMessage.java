package firestream.chat.namespace;

import firestream.chat.message.Message;
import firestream.chat.message.Sendable;

public class FireStreamMessage extends Message {

    public static FireStreamMessage fromMessage(Message message) {
        FireStreamMessage firestreamMessage = new FireStreamMessage();
        message.copyTo(firestreamMessage);
        return firestreamMessage;
    }

    public static FireStreamMessage fromSendable(Sendable sendable) {
        FireStreamMessage message = new FireStreamMessage();
        sendable.copyTo(message);
        return message;
    }

}
