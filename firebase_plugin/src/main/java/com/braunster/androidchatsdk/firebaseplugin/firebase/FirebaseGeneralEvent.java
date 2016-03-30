/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;


import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

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