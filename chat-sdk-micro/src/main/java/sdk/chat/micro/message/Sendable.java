package sdk.chat.micro.message;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;

import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.firestore.Keys;
import sdk.chat.micro.firestore.FSMessage;
import sdk.chat.micro.types.BaseType;

@IgnoreExtraProperties
public class Sendable extends FSMessage {

    @Exclude
    public String id;

    public Sendable() {
        from = MicroChatSDK.shared().currentUserId();
    }

    @SuppressWarnings("unchecked")
    public Sendable (String id, HashMap<String, Object> data) {
        this.id = id;

        if (data.get(Keys.From) instanceof String) {
            from = (String) data.get(Keys.From);
        }
        if (data.get(Keys.Date) instanceof Date) {
            date = (Date) data.get(Keys.Date);
        }
        if (data.get(Keys.Body) instanceof HashMap) {
            body = (HashMap<String, Object>) data.get(Keys.Body);
        }
        if (data.get(Keys.Type) instanceof String) {
            type = (String) data.get(Keys.Type);
        }
    }

    @Exclude
    public void setBodyType(BaseType type) {
        body.put(Keys.Type, type.get());
    }

    @Exclude
    public BaseType getBodyType() {
        if (body.get(Keys.Type) instanceof String) {
            String type = (String) body.get(Keys.Type);
            return new BaseType(type);
        }
        return BaseType.none();
    }

    @Exclude
    public String getBodyString(String key) throws Exception {
        if (body.get(key) instanceof String) {
            return (String) body.get(key);
        }
        throw new Exception("Body doesn't contain key: " + key);
    }

}
