package firestream.chat.message;

import java.util.Date;
import java.util.HashMap;

import firestream.chat.types.SendableType;

public class BaseMessage {

    protected String from;
    protected Date date = new Date();
    protected HashMap<String, Object> body = new HashMap<>();
    protected String type;

    public BaseMessage() {

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public HashMap<String, Object> getBody() {
        return body;
    }

    public void setBody(HashMap<String, Object> body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isType(SendableType type) {
        return getType().equals(type.get());
    }

}
