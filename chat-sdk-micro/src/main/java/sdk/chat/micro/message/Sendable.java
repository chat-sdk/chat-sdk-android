package sdk.chat.micro.message;

import java.util.Date;
import java.util.HashMap;

import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.firestore.FSKeys;
import sdk.chat.micro.firestore.FSMessage;
import sdk.chat.micro.types.BaseType;

public class Sendable extends FSMessage {


    public String id;

    public Sendable() {
        fromId = MicroChatSDK.shared().currentUserId();
    }

    public Sendable (String id, HashMap<String, Object> data) {
        this.id = id;

        if (data.get(FSKeys.From) instanceof String) {
            fromId = (String) data.get(FSKeys.From);
        }
        if (data.get(FSKeys.Date) instanceof Date) {
            date = (Date) data.get(FSKeys.Date);
        }
        if (data.get(FSKeys.Body) instanceof HashMap) {
            body = (HashMap<String, Object>) data.get(FSKeys.Body);
        }
        if (data.get(FSKeys.Type) instanceof Integer) {
            type = (Integer) data.get(FSKeys.Type);
        }
    }

    public void setType(BaseType type) {
        body.put(FSKeys.Type, type.get());
    }

    public BaseType getBodyType() {
        if (body.get(FSKeys.Type) instanceof String) {
            String type = (String) body.get(FSKeys.Type);
            return new BaseType(type);
        }
        return BaseType.none();
    }

    public String getBodyString(String key) throws Exception {
        if (body.get(key) instanceof String) {
            return (String) body.get(key);
        }
        throw new Exception("Body doesn't contain key: " + key);
    }

}
