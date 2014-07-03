package com.braunster.chatsdk.listeners;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by braunster on 30/06/14.
 */
public class ThreadDetailsChangeListener extends FirebaseGeneralEvent {

    private static final String TAG = ThreadDetailsChangeListener.class.getSimpleName();
    private static final boolean DEBUG = true;

    private String threadID;
    private Handler handler;

    public ThreadDetailsChangeListener(String threadID, Handler handler){
        super(ValueEvent);
        this.threadID = threadID;
        this.handler = handler;
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
        if (DEBUG) Log.i(TAG, "Thread details changed.");
        new Thread(new Runnable() {
            @Override
            public void run() {
                BFirebaseInterface.objectFromSnapshot(dataSnapshot);
                Message message = new Message();
                message.what = 2;
                message.obj = threadID;
                handler.sendMessage(message);
            }
        }).start();

//        EventManager.getInstance().onThreadAdded(thread);
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
