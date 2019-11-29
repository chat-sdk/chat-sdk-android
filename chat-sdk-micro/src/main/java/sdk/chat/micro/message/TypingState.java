package sdk.chat.micro.message;

import sdk.chat.micro.types.SendableType;
import sdk.chat.micro.types.TypingStateType;

public class TypingState extends Sendable {

    public enum Type {
        typing,
        none,
    }

    public TypingState() {
        type = SendableType.TypingState;
    }

    public TypingState(TypingStateType type) {
        this();
        setType(type);
    }

    public TypingStateType getBodyType() {
        return new TypingStateType(super.getBodyType());
    }
}
