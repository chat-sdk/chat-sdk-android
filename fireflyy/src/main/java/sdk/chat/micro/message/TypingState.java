package sdk.chat.micro.message;

import com.google.firebase.firestore.Exclude;

import sdk.chat.micro.types.SendableType;
import sdk.chat.micro.types.TypingStateType;

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
}
