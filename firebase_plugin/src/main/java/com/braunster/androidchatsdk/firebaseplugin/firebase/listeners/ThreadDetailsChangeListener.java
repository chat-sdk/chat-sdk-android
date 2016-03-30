package com.braunster.androidchatsdk.firebaseplugin.firebase.listeners;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseInterface;
import com.braunster.androidchatsdk.firebaseplugin.firebase.EventManager;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseGeneralEvent;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by braunster on 30/06/14.
 */
public class ThreadDetailsChangeListener extends FirebaseGeneralEvent {

    private static final String TAG = ThreadDetailsChangeListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.ThreadDetailsChangeListener;

    private String threadID;
    private Handler handler;

    public ThreadDetailsChangeListener(String threadID, Handler handler){
        super(ValueEvent);
        this.threadID = threadID;
        this.handler = handler;
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
        if (DEBUG) Log.i(TAG, "Thread details changed, Alive: " + isAlive());
        if (isAlive())
            EventManager.Executor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    BFirebaseInterface.objectFromSnapshot(dataSnapshot);
                    Message message = new Message();
                    message.what = AppEvents.THREAD_DETAILS_CHANGED;
                    message.obj = threadID;
                    handler.sendMessage(message);
                }
            });
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
