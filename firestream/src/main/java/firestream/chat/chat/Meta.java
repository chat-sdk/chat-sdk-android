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
    protected Object timestamp;
    protected boolean wrap = false;

    public Meta() {
    }

    public Meta(String name, String imageURL) {
        this(name, imageURL, null);
    }

    public Meta(String name, String imageURL, HashMap<String, Object> data) {
        this(name, imageURL, null, data);
    }

    public Meta(String name, String imageURL, Date created, HashMap<String, Object> data) {
        this.name = name;
        this.imageURL = imageURL;
        this.created = created;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Meta setName(String name) {
        this.name = name;
        return this;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Meta setImageURL(String imageURL) {
        this.imageURL = imageURL;
        return this;
    }

    public Meta setData(HashMap<String, Object> data) {
        this.data = data;
        return this;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public Meta addTimestamp() {
        timestamp = Fire.internal().getFirebaseService().core.timestamp();
        return this;
    }

    public Meta wrap() {
        wrap = true;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public static HashMap<String, Object> nameData(String name) {
        return new HashMap<String, Object>(){{
            put(Keys.Name, name);
        }};
    }

    public static HashMap<String, Object> imageURLData(String imageURL) {
        return new HashMap<String, Object>(){{
            put(Keys.ImageURL, imageURL);
        }};
    }

    public static HashMap<String, Object> dataData(HashMap<String, Object> data) {
        return new HashMap<String, Object>(){{
            put(Keys.Data, data);
        }};
    }

    public HashMap<String, Object> toData() {
        HashMap<String, Object> data = new HashMap<>();

        if (name != null) {
            data.put(Keys.Name, name);
        }
        if (imageURL != null) {
            data.put(Keys.ImageURL, imageURL);
        }
        if (this.data != null) {
            data.put(Keys.Data, this.data);
        }
        if (timestamp != null) {
            data.put(Keys.Created, timestamp);
        }
        if (wrap) {
            return wrap(data);
        }
        return data;
    }

    protected static HashMap<String, Object> wrap(HashMap<String, Object> map) {
        return new HashMap<String, Object>() {{
            put(Keys.Meta, map);
        }};
    }

    public Meta copy() {
        Meta meta = new Meta(name, imageURL);
        meta.created = created;
        meta.data = data;
        return meta;
    }

    public static Meta from(String name, String imageURL) {
        return new Meta(name, imageURL);
    }

    public static Meta from(String name, String imageURL, HashMap<String, Object> data) {
        return new Meta(name, imageURL, data);
    }


}
