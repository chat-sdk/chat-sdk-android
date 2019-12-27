package firefly.sdk.chat.events;

import java.util.Map;

public class ListEvent extends Event {

    public String id;
    public Map<String, Object> data;

    public ListEvent(String id, Map<String, Object> data, EventType type) {
        super(type);
        this.id = id;
        this.data = data;
    }

    public Object get(String key) {
        if (data != null) {
            return data.get(key);
        }
        return null;
    }

    public static ListEvent added(String id, Map<String, Object> data) {
        return new ListEvent(id, data, EventType.Added);
    }

    public static ListEvent removed(String id, Map<String, Object> data) {
        return new ListEvent(id, data, EventType.Removed);
    }

    public static ListEvent modified(String id, Map<String, Object> data) {
        return new ListEvent(id, data, EventType.Modified);
    }

}
