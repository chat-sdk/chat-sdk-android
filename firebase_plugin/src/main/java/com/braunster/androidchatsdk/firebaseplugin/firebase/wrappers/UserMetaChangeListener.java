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
import com.braunster.chatsdk.interfaces.AppEvents;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.jdeferred.Deferred;

import timber.log.Timber;

public class UserMetaChangeListener extends FirebaseGeneralEvent {

    private static final String TAG = UserMetaChangeListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.UserDetailsChangeListener;

    private String userID;
    private Handler handler;
    private Deferred<Void, Void, Void> deferred;

    public UserMetaChangeListener(String userId, Deferred<Void, Void, Void> deferred, Handler handler){
        super(ValueEvent);
        this.deferred = deferred;
        this.userID = userId;
        this.handler = handler;
    }

    @Override
    public void onDataChange(final DataSnapshot snapshot) {
        if (DEBUG) Timber.v("User Details has changed, Alive: %s", isAlive());
        if (isAlive())
            FirebaseEventsManager.Executor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    if (snapshot.getValue() != null)
                    {
                        BUserWrapper user = BUserWrapper.initWithEntityId(userID);
                        user.deserializeMeta((java.util.Map<String, Object>) snapshot.getValue());

                        if (deferred != null && deferred.isPending())
                            deferred.resolve(null);

                        Message message = new Message();
                        message.what = AppEvents.USER_DETAILS_CHANGED;
                        message.obj = user.model;
                        handler.sendMessage(message);
                    }
                }
            });
    }

    @Override
    public void onCancelled(DatabaseError firebaseError) {

    }
}
