package firestream.chat.chat;

import java.util.Date;
import java.util.HashMap;

import firestream.chat.firebase.service.Keys;
import firestream.chat.namespace.Fire;

public class Meta {

    protected String name = "";
    protected String imageURL = "";
    protected Date created;
    protected HashMap<String, Object> data;

    public Meta() {
    }

    public Meta(String name, String imageURL) {
        this(name, imageURL, null);
    }

    public Meta(String name, String imageURL, Date created) {
        this.name = name;
        this.imageURL = imageURL;
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public HashMap<String, Object> toData() {
        return toData(false);
    }

    public HashMap<String, Object> toData(boolean includeTimestamp) {
        HashMap<String, Object> toWrite = new HashMap<>();

        toWrite.put(Keys.Name, name);
        toWrite.put(Keys.ImageURL, imageURL);
        toWrite.put(Keys.Data, data);

        if (includeTimestamp) {
            toWrite.put(Keys.Created, Fire.Stream.getFirebaseService().core.timestamp());
        }

        HashMap<String, Object> meta = new HashMap<>();
        meta.put(Keys.Meta, toWrite);

        return meta;
    }

    public Meta copy() {
        Meta meta = new Meta(name, imageURL);
        meta.created = created;
        meta.data = data;
        return meta;
    }

    public static Meta with(String name, String imageURL) {
        return new Meta(name, imageURL);
    }


}
