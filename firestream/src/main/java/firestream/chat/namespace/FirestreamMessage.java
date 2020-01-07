package firestream.chat.namespace;

import firestream.chat.message.Message;
import firestream.chat.message.Sendable;

public class FirestreamMessage extends Message {

    public static FirestreamMessage fromMessage(Message message) {
        FirestreamMessage firestreamMessage = new FirestreamMessage();
        message.copyTo(firestreamMessage);
        return firestreamMessage;
    }

    public static FirestreamMessage fromSendable(Sendable sendable) {
        FirestreamMessage message = new FirestreamMessage();
        sendable.copyTo(message);
        return message;
    }

}
