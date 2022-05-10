package sdk.chat.core.push;

import java.util.Map;

public class PushQueueAction {

    public enum Type {
        openThread
    }

    public Type type;
    public Map<String, String> payload;

    public PushQueueAction(Type type, Map<String, String> payload) {
        this.type = type;
        this.payload = payload;
    }
}
