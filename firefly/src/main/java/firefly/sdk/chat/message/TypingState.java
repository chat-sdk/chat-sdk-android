package firefly.sdk.chat.message;

import com.google.firebase.firestore.Exclude;

import firefly.sdk.chat.types.SendableType;
import firefly.sdk.chat.types.TypingStateType;

public class TypingState extends Sendable {

    public TypingState() {
        type = SendableType.TypingState;
    }

    public TypingState(TypingStateType type) {
        this();
        setBodyType(type);
    }

    @Exclude
    public TypingStateType getBodyType() {
        return new TypingStateType(super.getBodyType());
    }

    public static TypingState fromSendable(Sendable sendable) {
        TypingState typingState = new TypingState();
        sendable.copyTo(typingState);
        return typingState;
    }

}
