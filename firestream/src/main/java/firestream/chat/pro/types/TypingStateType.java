package firestream.chat.pro.types;

import firestream.chat.types.BaseType;

public class TypingStateType extends BaseType {

    public static String Typing = "typing";

    public TypingStateType(String type) {
        super(type);
    }

    public TypingStateType(BaseType type) {
        super(type);
    }

    public static TypingStateType typing() {
        return new TypingStateType(Typing);
    }

    public static TypingStateType none() {
        return new TypingStateType("");
    }

}
