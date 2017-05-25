/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

@Deprecated
public class UserMetaChangeListener {

//    private static final String TAG = UserMetaChangeListener.class.getSimpleName();
//    private static final boolean DEBUG = Debug.UserDetailsChangeListener;
//
//    private String userID;
//    private Handler handler;
//    private Deferred<Void, Void, Void> deferred;
//
//    public UserMetaChangeListener(String userId, Deferred<Void, Void, Void> deferred, Handler handler){
//        super(ValueEvent);
//        this.deferred = deferred;
//        this.userID = userId;
//        this.handler = handler;
//    }

//    @Override
    public void onDataChange(final DataSnapshot snapshot) {
//        if (DEBUG) Timber.v("CoreUser Details has changed, Alive: %s", isAlive());
//        if (isAlive())
//            Executor.getInstance().execute(new Runnable() {
//                @Override
//                public void run() {
//                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//
//                    if (snapshot.getValue() != null)
//                    {
//                        UserWrapper user = UserWrapper.initWithEntityId(userID);
//                        user.deserializeMeta((java.util.Map<String, Object>) snapshot.getValue());
//
//                        if (deferred != null && deferred.isPending())
//                            deferred.resolve(null);
//
//                        CoreMessage message = new CoreMessage();
//                        message.what = AppEvents.USER_DETAILS_CHANGED;
//                        message.obj = user.model;
//                        handler.sendMessage(message);
//                    }
//                }
//            });
    }

    //@Override
    public void onCancelled(DatabaseError firebaseError) {

    }
}
