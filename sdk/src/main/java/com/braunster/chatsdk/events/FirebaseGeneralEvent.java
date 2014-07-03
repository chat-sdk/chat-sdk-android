package com.braunster.chatsdk.events;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by braunster on 30/06/14.
 */
public class FirebaseGeneralEvent implements ValueEventListener, ChildEventListener {

    public static final int ValueEvent = 0;
    public static final int ChildEvent = 1;

    private int type = -1;

    public int getType(){
        return type;
    }

    public FirebaseGeneralEvent(int type){
        this.type = type;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}