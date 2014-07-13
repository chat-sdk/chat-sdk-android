package com.braunster.chatsdk.network.events;

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
    private boolean alive = true;

    public int getType(){
        return type;
    }

    /** This is a workaround to make sure events are not trigger after they are removed.
     * If the event is killed even if the Firebase Child/Value Event will be trigger,
     * the data wont be parsed and the EventManager wont get any notification. */
    public void killEvent(){
        alive = false;
    }

    /** @return true if the event listener is still alive.*/
    public boolean isAlive(){
        return alive;
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