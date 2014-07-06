package com.braunster.chatsdk.object;

import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;

/**
 * Created by braunster on 30/06/14.
 */
public class FirebaseEventCombo {
    private FirebaseGeneralEvent listener;
    private String ref;
    private Object tag;

    public FirebaseEventCombo(FirebaseGeneralEvent listener, String ref) {
        this.listener = listener;
        this.ref = ref;
    }

    public FirebaseEventCombo(FirebaseGeneralEvent listener, String ref, Object tag) {
        this.listener = listener;
        this.ref = ref;
        this.tag = tag;
    }

    public static FirebaseEventCombo getNewInstance(FirebaseGeneralEvent listener, String ref) {
        return new FirebaseEventCombo(listener, ref);
    }

    public static FirebaseEventCombo getNewInstance(FirebaseGeneralEvent listener, String ref, Object tag) {
        return new FirebaseEventCombo(listener, ref, tag);
    }

    public FirebaseGeneralEvent getListener() {
        return listener;
    }

    public String getRef() {
        return ref;
    }

    public Object getTag() {
        return tag;
    }
}
