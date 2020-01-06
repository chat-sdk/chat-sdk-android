package firestream.chat.namespace;

import firestream.chat.message.Message;
import firestream.chat.message.Sendable;

public class FirestreamMessage extends Message {

    public static FirestreamMessage fromMessage(Message message) {
        FirestreamMessage firestreamMessage = new FirestreamMessage();

        firestreamMessage.id = message.id;
        firestreamMessage.type = message.type;
        firestreamMessage.from = message.from;
        firestreamMessage.date = message.date;
        firestreamMessage.body = message.body;

        return firestreamMessage;
    }

    public static FirestreamMessage fromSendable(Sendable sendable) {
        FirestreamMessage message = new FirestreamMessage();
        sendable.copyTo(message);
        return message;
    }

}
