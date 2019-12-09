package sdk.chat.micro.firestore;

import java.util.Date;
import java.util.HashMap;

public class FSMessage {

    public String from;
    public Date date = new Date();
    public HashMap<String, Object> body = new HashMap<>();
    public String type;

    public FSMessage () {

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

}
