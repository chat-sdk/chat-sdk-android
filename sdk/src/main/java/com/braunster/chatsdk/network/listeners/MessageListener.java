package com.braunster.chatsdk.network.listeners;

import android.util.Log;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by braunster on 30/06/14.
 */
public class MessageListener extends FirebaseGeneralEvent implements ChildEventListener {

    /* A listener to listen to incoing messages from thread.
    *  It is important to use the getNewInstance method so each instance will be saved and we could unregister it if needed later on.
    *  It is also important to add the thread entity id and check before adding a new thread so we wont receive duplicates notifications.*/

    private static final String TAG = MessageListener.class.getSimpleName();
    private static boolean DEBUG = true;

    public static List<String> threadsIds = new ArrayList<String>();
    public static List<MessageListener> instances = new ArrayList<MessageListener>();

    public MessageListener(){
        super(ChildEvent);
    }

    public static MessageListener getInstance() {
        MessageListener messageListener = new MessageListener();
        instances.add(messageListener);
        return messageListener;
    }

    public static void addThread(String entityID){
        if (!threadsIds.contains(entityID))
            threadsIds.add(entityID);
    }

    public static boolean isListeningToThread(String entityID){
        return threadsIds.contains(entityID);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (DEBUG) Log.d(TAG, "Message is added.");
        BMessage message = (BMessage) BFirebaseInterface.objectFromSnapshot(dataSnapshot);
        EventManager.getInstance().onMessageReceived(message);
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
    public void onCancelled(FirebaseError firebaseError) {

    }
}
