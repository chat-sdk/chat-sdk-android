/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import android.os.Handler;
import android.os.Message;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseEventsManager;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseGeneralEvent;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.jdeferred.Deferred;

import timber.log.Timber;

public class ThreadUpdateChangeListener extends FirebaseGeneralEvent {

    private static final String TAG = ThreadUpdateChangeListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.ThreadDetailsChangeListener;

    private String threadID;
    private Handler handler;
    private Deferred<BThread, Void, Void> deferred;
    
    public ThreadUpdateChangeListener(String threadID, Handler handler, Deferred<BThread, Void, Void> deferred){
        super(ValueEvent);
        this.deferred = deferred;
        this.threadID = threadID;
        this.handler = handler;
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
        if (DEBUG) Timber.i("Thread details changed, Alive: %s", isAlive());
        if (isAlive())
            FirebaseEventsManager.Executor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    if (dataSnapshot.getValue() == null)
                    {      
                        if (deferred != null && deferred.isPending())
                            deferred.reject(null);
                        return;
                    }
                    
                    BThreadWrapper wrapper = new BThreadWrapper(threadID);
                    
                    wrapper.deserialize((java.util.Map<String, Object>) dataSnapshot.getValue());

                    if (deferred != null && deferred.isPending())
                        deferred.resolve(wrapper.model);
                    
                    Message message = new Message();
                    message.what = AppEvents.THREAD_DETAILS_CHANGED;
                    message.obj = threadID;
                    handler.sendMessage(message);
                }
            });
    }

    @Override
    public void onCancelled(DatabaseError firebaseError) {

    }
}
