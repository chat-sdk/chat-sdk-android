package firestream.chat.util;

import java.util.HashMap;
import java.util.Map;

public class TypingMap {

    Map<String, Typing> typingMap = new HashMap<>();

    public Typing get(String userId) {
        Typing typing = typingMap.get(userId);
        if (typing == null) {
            typing = new Typing();
            typingMap.put(userId, typing);
        }
        return typing;
    }

}
