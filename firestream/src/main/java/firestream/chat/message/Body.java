package firestream.chat.message;

import java.util.HashMap;
import java.util.Map;

import firestream.chat.firebase.service.Keys;
import firestream.chat.types.BaseType;

public class Body {

    protected BaseType type;
    protected Map<String, Object> values;

    public Body() {
        values = new HashMap<>();
        type = BaseType.none();
    }

    public Body(Map<String, Object> data) {
        Object type = data.get(Keys.Type);
        if (type instanceof String) {
            setType((String) type);
        }
        data.remove(Keys.Type);
        values = data;
    }

    public BaseType getType() {
        return type;
    }

    public void setType(BaseType type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = new BaseType(type);
    }

    public String stringForKey(String key) {
        Object value = values.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public Object get(String key) {
        return values.get(key);
    }

    public void put(String key, Object value){
        values.put(key, value);
    }

    public Map<String, Object> get() {
        Map<String, Object> data = new HashMap<>(values);
        data.put(Keys.Type, type.get());
        return data;
    }

}
