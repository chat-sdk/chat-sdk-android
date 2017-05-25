/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

@Deprecated
public class ThreadUpdateChangeListener {

//    private static final String TAG = ThreadUpdateChangeListener.class.getSimpleName();
//    private static final boolean DEBUG = Debug.ThreadDetailsChangeListener;
//
//    private String threadID;
//    private Handler handler;
//    private Deferred<BThread, Void, Void> deferred;
//
//    public ThreadUpdateChangeListener(String threadID, Handler handler, Deferred<BThread, Void, Void> deferred){
//        super(ValueEvent);
//        this.deferred = deferred;
//        this.threadID = threadID;
//        this.handler = handler;
//    }
//
//    @Override
//    public void onDataChange(final DataSnapshot dataSnapshot) {
//        if (DEBUG) Timber.i("CoreThread details changed, Alive: %s", isAlive());
//        if (isAlive())
//            Executor.getInstance().execute(new Runnable() {
//                @Override
//                public void run() {
//                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//
//                    if (dataSnapshot.getValue() == null)
//                    {
//                        if (deferred != null && deferred.isPending())
//                            deferred.reject(null);
//                        return;
//                    }
//
//                    ThreadWrapper wrapper = new ThreadWrapper(threadID);
//
//                    wrapper.deserialize((java.util.Map<String, Object>) dataSnapshot.getValue());
//
//                    if (deferred != null && deferred.isPending())
//                        deferred.resolve(wrapper.model);
//
//                    CoreMessage message = new CoreMessage();
//                    message.what = AppEvents.THREAD_DETAILS_CHANGED;
//                    message.obj = threadID;
//                    handler.sendMessage(message);
//                }
//            });
//    }
//
//    @Override
//    public void onCancelled(DatabaseError firebaseError) {
//
//    }
}
