package com.braunster.chatsdk.object;

import com.braunster.chatsdk.events.FirebaseGeneralEvent;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Objects;

/**
 * Created by braunster on 30/06/14.
 */
public class FirebaseEventCombo {
    private FirebaseGeneralEvent listener;
    private Object ref;
    private Object tag;

    public FirebaseEventCombo(FirebaseGeneralEvent listener, Object ref) {
        this.listener = listener;
        this.ref = ref;
    }

    public FirebaseEventCombo(FirebaseGeneralEvent listener, Object ref, Object tag) {
        this.listener = listener;
        this.ref = ref;
        this.tag = tag;
    }

    public static FirebaseEventCombo getNewInstance(FirebaseGeneralEvent listener, Object ref) {
        return new FirebaseEventCombo(listener, ref);
    }

    public static FirebaseEventCombo getNewInstance(FirebaseGeneralEvent listener, Object ref, String tag) {
        return new FirebaseEventCombo(listener, ref, tag);
    }

    public FirebaseGeneralEvent getListener() {
        return listener;
    }

    public Object getRef() {
        return ref;
    }

    public Object getTag() {
        return tag;
    }
}
