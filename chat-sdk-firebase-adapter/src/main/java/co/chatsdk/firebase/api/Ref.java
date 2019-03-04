package co.chatsdk.firebase.api;

import java.util.ArrayList;

public class Ref {

    protected String collection;
    protected String id;

    public Ref(String collection, String id) {

    }

    public String getCollection () {
        return collection;
    }

    public String getId () {
        return id;
    }

}
