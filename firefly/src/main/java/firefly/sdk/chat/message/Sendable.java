package firefly.sdk.chat.message;

import java.util.Date;
import java.util.HashMap;

import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.BaseType;
import firefly.sdk.chat.types.DeliveryReceiptType;
import io.reactivex.Completable;

public class Sendable extends BaseMessage {

    public String id;

    public Sendable() {
        from = Fl.y.currentUserId();
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

    public void setBodyType(BaseType type) {
        body.put(Keys.Type, type.get());
    }

    public BaseType getBodyType() {
        if (body.get(Keys.Type) instanceof String) {
            String type = (String) body.get(Keys.Type);
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

    public void copyTo(Sendable sendable) {
        sendable.id = id;
        sendable.from = from;
        sendable.body = body;
        sendable.date = date;
    }

    public BaseMessage toBaseMessage() {
        BaseMessage message = new BaseMessage();
        message.from = from;
        message.body = body;
        message.date = date;
        message.type = type;
        return message;
    }

    public HashMap<String, Object> toData() {
        HashMap<String, Object> data = new HashMap<>();
        data.put(Keys.From, from);
        data.put(Keys.Body, body);
        data.put(Keys.Date, Fl.y.getFirebaseService().core.timestamp());
        data.put(Keys.Type, type);
        return data;
    }

    /**
     * Send a read receipt
     * @return completion
     */
    public Completable markRead() {
        return Fl.y.sendDeliveryReceipt(from, DeliveryReceiptType.read(), id);
    }

    /**
     * Send a received receipt
     * @return completion
     */
    public Completable markReceived() {
        return Fl.y.sendDeliveryReceipt(from, DeliveryReceiptType.received(), id);
    }

}
