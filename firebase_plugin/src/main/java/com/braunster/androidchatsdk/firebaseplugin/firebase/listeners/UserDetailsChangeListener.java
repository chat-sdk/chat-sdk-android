package com.braunster.androidchatsdk.firebaseplugin.firebase.listeners;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseInterface;
import com.braunster.androidchatsdk.firebaseplugin.firebase.EventManager;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseGeneralEvent;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by braunster on 30/06/14.
 */
public class UserDetailsChangeListener extends FirebaseGeneralEvent {

    private static final String TAG = UserDetailsChangeListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.UserDetailsChangeListener;

    private String userID;
    private Handler handler;

    public UserDetailsChangeListener(String userId, Handler handler){
        super(ValueEvent);
        this.userID = userId;
        this.handler = handler;
    }

    @Override
    public void onDataChange(final DataSnapshot snapshot) {
        if (DEBUG) Log.v(TAG, "User Details has changed, Alive: " + isAlive());
        if (isAlive())
            EventManager.UserDetailsExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    BUser user = (BUser) BFirebaseInterface.objectFromSnapshot(snapshot);
                    Message message = new Message();
                    message.what = AppEvents.USER_DETAILS_CHANGED;
                    message.obj = user;
                    handler.sendMessage(message);
                }
            });
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
