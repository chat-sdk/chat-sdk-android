package firestream.chat.events;

import java.util.Map;

public class ListData {

    protected String id;
    protected Map<String, Object> data;

    public ListData(String id, Map<String, Object> data) {
        this.id = id;
        this.data = data;
    }

    public Object get(String key) {
        if (data != null) {
            return data.get(key);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getData() {
        return data;
    }

}
