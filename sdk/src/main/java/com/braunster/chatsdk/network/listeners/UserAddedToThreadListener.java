package com.braunster.chatsdk.network.listeners;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.network.firebase.BPath;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.object.FirebaseEventCombo;
import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by braunster on 30/06/14.
 */
public class UserAddedToThreadListener extends FirebaseGeneralEvent {

    private static final String TAG = UserAddedToThreadListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.UserAddedToThreadListener;

    private static List<String> usersIds = new ArrayList<String>();
    private String threadID, observedUserId;
    private Handler handler;
    private FirebaseEventCombo combo;
    private FirebaseGeneralEvent userDetailsChangeListener;

    public static UserAddedToThreadListener getNewInstance(String observedUserId, String threadID, Handler handler) {
        UserAddedToThreadListener userAddedToThreadListener = new UserAddedToThreadListener(observedUserId, threadID,  handler);
        return userAddedToThreadListener;
    }

    public UserAddedToThreadListener(String obeservedUserId, String threadID, Handler handler){
        super(ChildEvent);
        this.observedUserId = obeservedUserId;
        this.handler = handler;
        this.threadID = threadID;
    }



    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
        if (DEBUG) Log.v(TAG, "User Added to thread, Alive: " + isAlive());

        if (isAlive())
            EventManager.Executor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
                    if (!currentUser.getEntityID().equals(observedUserId))
                        return;

                    BPath path = BPath.pathWithPath(dataSnapshot.getRef().toString());
                    final String userFirebaseID = path.idForIndex(1);

                    BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadID);

                    BUser bUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);

                    // Attaching the user to the thread if needed.
                    if (!thread.hasUser(bUser))
                        DaoCore.connectUserAndThread(bUser, thread);

                    // Dont notify the system for the current user added.
                    if (userFirebaseID.equals(currentUser.getEntityID())) {
                        return;
                    }

                    if (thread.getType() != BThread.Type.Public) {
                        // Users that are members of threads are shown in contacts
                        currentUser.addContact(bUser);
                    }

                    Message message = new Message();
                    message.what = AppEvents.USER_ADDED_TO_THREAD;
                    Bundle data = new Bundle();
                    data.putString(EventManager.THREAD_ID, threadID);
                    data.putString(EventManager.USER_ID, userFirebaseID);
                    message.setData(data);
                    handler.sendMessage(message);
                }
            });
    }
}
