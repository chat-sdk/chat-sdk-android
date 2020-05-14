package firestream.chat.message;

import firestream.chat.types.SendableType;
import firestream.chat.types.TypingStateType;

public class TypingState extends Sendable {

    public TypingState() {
        type = SendableType.TypingState;
    }

    public TypingState(TypingStateType type) {
        this();
        setBodyType(type);
    }

    public TypingStateType getTypingStateType() {
        return new TypingStateType(super.getBodyType());
    }

    public static TypingState fromSendable(Sendable sendable) {
        TypingState typingState = new TypingState();
        sendable.copyTo(typingState);
        return typingState;
    }

}
