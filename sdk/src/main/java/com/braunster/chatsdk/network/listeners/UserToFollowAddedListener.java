package com.braunster.chatsdk.network.listeners;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BFollower;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by braunster on 30/06/14.
 */
public class UserToFollowAddedListener extends FirebaseGeneralEvent {

    private static final String TAG = UserToFollowAddedListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.FollowerAddedListener;

    private Handler handler;

    public UserToFollowAddedListener(Handler handler) {
        super(ChildEvent);
        this.handler = handler;
    }

    @Override
    public void onChildAdded(final DataSnapshot snapshot, String s) {
        if (isAlive())
            EventManager.Executor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    if (DEBUG) Log.i(TAG, "Follower is added. SnapShot Ref: " + snapshot.getRef().toString());
                    BFollower follower = (BFollower) BFirebaseInterface.objectFromSnapshot(snapshot);

                    Message message = new Message();
                    message.what = AppEvents.USER_TO_FOLLOW_ADDED;
                    message.obj = follower;
                    handler.sendMessage(message);
                }
            });
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String s) {

    }

    @Override
    public void onChildRemoved(final DataSnapshot snapshot) {
        if (isAlive())
            EventManager.Executor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    if (DEBUG) Log.i(TAG, "Follower is removed. SnapShot Ref: " + snapshot.getRef().toString());
                    BFollower follower = (BFollower) BFirebaseInterface.objectFromSnapshot(snapshot);

                    DaoCore.deleteEntity(follower);

                    Message message = new Message();
                    message.what = AppEvents.USER_TO_FOLLOW_REMOVED;
                    handler.sendMessage(message);
                }
            });
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
