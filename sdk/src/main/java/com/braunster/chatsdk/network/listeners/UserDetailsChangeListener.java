package com.braunster.chatsdk.network.listeners;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by braunster on 30/06/14.
 */
public class UserDetailsChangeListener extends FirebaseGeneralEvent {

    private static final String TAG = UserDetailsChangeListener.class.getSimpleName();
    private static final boolean DEBUG = true;

    private String userID;
    private Handler handler;

    public UserDetailsChangeListener(String userId, Handler handler){
        super(ValueEvent);
        this.userID = userId;
        this.handler = handler;
    }

    @Override
    public void onDataChange(final DataSnapshot snapshot) {
        if (DEBUG) Log.v(TAG, "User Details has changed.");
        if (isAlive())
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BUser user = (BUser) BFirebaseInterface.objectFromSnapshot(snapshot);
                    Message message = new Message();
                    message.what = AppEvents.USER_DETAILS_CHANGED;
                    message.obj = user;
                    handler.sendMessage(message);
                }
            }).start();

//        EventManager.getInstance().onThreadDetailsChanged(thread);
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
