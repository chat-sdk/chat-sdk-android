package com.braunster.chatsdk.network.listeners;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
    private static final boolean DEBUG = true;

    private static List<String> usersIds = new ArrayList<String>();
    private String threadID;
    private Handler handler;
    private FirebaseEventCombo combo;
    private FirebaseGeneralEvent userDetailsChangeListener;

    public static UserAddedToThreadListener getNewInstance(String threadID, Handler handler) {
        UserAddedToThreadListener userAddedToThreadListener = new UserAddedToThreadListener(threadID,  handler);
        return userAddedToThreadListener;
    }

    public UserAddedToThreadListener(String threadID, Handler handler){
        super(ChildEvent);
        this.handler = handler;
        this.threadID = threadID;
    }



    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (DEBUG) Log.d(TAG, "Datasnapshot cildren: " + dataSnapshot.getChildren());
                BPath path = BPath.pathWithPath(dataSnapshot.getRef().toString());
                final String userFirebaseID = path.idForIndex(1);

                if (DEBUG) Log.e(TAG, "User, " + userFirebaseID + " , CurrentUser " + EventManager.currentUserId);
                if (userFirebaseID.equals(EventManager.currentUserId)) {
                    return;
                }

                BUser bUser;
                // If the user already has  listening to this user we can fetch it from the db because he is up to date.
//                if (usersIds.contains(userFirebaseID))
                    bUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);
//                else
////                    // For each user we'd then need to add them to the database
//                    bUser = (BUser) BFirebaseInterface.objectFromSnapshot(dataSnapshot);

                BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadID);
                if (thread.getType() != BThread.Type.Public)
                {
                    // Users that are members of threads are shown in contacts
                    BNetworkManager.sharedManager().getNetworkAdapter().currentUser().addContact(bUser);
                }

                // Attaching the user to the thread if needed.
                if (!thread.hasUser(bUser))
                    DaoCore.connectUserAndThread(bUser, thread);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (DEBUG) Log.v(TAG, "User Added to thread.");
                        Message message = new Message();
                        message.what = AppEvents.USER_ADDED_TO_THREAD;
                        Bundle data = new Bundle();
                        data.putString(EventManager.THREAD_ID, threadID);
                        data.putString(EventManager.USER_ID, userFirebaseID);
                        message.setData(data);
                        handler.sendMessage(message);
                    }
                }).start();
            }
        }).start();
    }
}
