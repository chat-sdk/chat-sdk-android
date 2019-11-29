package sdk.chat.micro.firestore;

import java.util.Date;
import java.util.HashMap;

public class FSMessage {

    public String fromId;
    public Date date = new Date();
    public HashMap<String, Object> body = new HashMap<>();
    public Integer type;

    public FSMessage () {

    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
