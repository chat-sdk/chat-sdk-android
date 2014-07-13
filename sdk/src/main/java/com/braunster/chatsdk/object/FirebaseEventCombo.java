package com.braunster.chatsdk.object;

import android.util.Log;

import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

/**
 * Created by braunster on 30/06/14.
 */
public class FirebaseEventCombo {

    private static final String TAG = FirebaseEventCombo.class.getSimpleName();
    private static final boolean DEBUG = true;

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

    /** Remove the saved listener from the saved ref.*/
    public void breakCombo(){
        if (DEBUG) Log.d(TAG, "Ref Path: " + ref);
        Firebase comboRef = new Firebase(ref);

        listener.killEvent();

        if (listener.getType() == FirebaseGeneralEvent.ChildEvent)
        {
            if (DEBUG) Log.d(TAG, "Removing ChildEvent");
            comboRef.removeEventListener((ChildEventListener) listener);
        }
        else if (listener.getType() == FirebaseGeneralEvent.ValueEvent)
        {
            if (DEBUG) Log.d(TAG, "Removing ValueEvent.");
            comboRef.removeEventListener((ValueEventListener) listener);
        }
    }
}
