package firestream.chat.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import firestream.chat.firebase.service.Keys;
import firestream.chat.namespace.Fire;

import firestream.chat.types.BaseType;

public class Sendable extends BaseMessage {

    protected String id;

    public Sendable() {
        from = Fire.stream().currentUserId();
    }

    public Sendable(String sendableId, String fromUserId) {
        from = fromUserId;
        id = sendableId;
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

    public boolean valid() {
        return from != null && date != null && body != null && type != null;
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
        sendable.setId(id);
        sendable.setFrom(from);
        sendable.setBody(body);
        sendable.setDate(date);
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
        data.put(Keys.Date, Fire.internal().getFirebaseService().core.timestamp());
        data.put(Keys.Type, type);
        return data;
    }

    public static class Converter<T extends Sendable> {

        protected Class<T> clazz;

        public Converter(Class<T> clazz) {
            this.clazz = clazz;
        }

        public T convert(Sendable s) {
            try {
                T instance = clazz.newInstance();
                s.copyTo(instance);
                return instance;
            } catch (Exception e) {
                return null;
            }
        }

        public List<T> convert(List<Sendable> sendables) {
            ArrayList<T> list = new ArrayList<>();
            for (Sendable sendable: sendables) {
                if (clazz.isInstance(sendable)) {
                    list.add(convert(sendable));
                }
            }
            return list;
        }

    }

    public Message toMessage() {
        return new Converter<>(Message.class).convert(this);
    }

    public TypingState toTypingState() {
        return new Converter<>(TypingState.class).convert(this);
    }

    public DeliveryReceipt toDeliveryReceipt() {
        return new Converter<>(DeliveryReceipt.class).convert(this);
    }

    public Invitation toInvitation() {
        return new Converter<>(Invitation.class).convert(this);
    }

    public Presence toPresence() {
        return new Converter<>(Presence.class).convert(this);
    }

    public TextMessage toTextMessage() {
        return new Converter<>(TextMessage.class).convert(this);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean equals(Sendable message) {
        return getId().equals(message.getId());
    }

}
