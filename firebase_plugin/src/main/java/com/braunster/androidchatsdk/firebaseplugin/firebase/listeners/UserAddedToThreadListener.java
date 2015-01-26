package com.braunster.androidchatsdk.firebaseplugin.firebase.listeners;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.androidchatsdk.firebaseplugin.firebase.EventManager;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseEventCombo;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseGeneralEvent;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BLinkData;
import com.braunster.chatsdk.dao.BLinkDataDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.BPath;
import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.Property;

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
        doLogic(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        super.onChildChanged(dataSnapshot, s);
        doLogic(dataSnapshot);
    }

    private void doLogic(final DataSnapshot dataSnapshot){
        if (isAlive())
            EventManager.UserDetailsExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

                    // If listener is old return, We can check if he is old if the observed user id does not match the current user id.
                    if (!currentUser.getEntityID().equals(observedUserId))
                        return;

                    // Find the user entity id.
                    BPath path = BPath.pathWithPath(dataSnapshot.getRef().toString());
                    final String userFirebaseID = path.idForIndex(1);

                    BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadID);

                    BUser bUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);

                    // Attaching the user to the thread if needed.
                    if (!thread.hasUser(bUser))
                        DaoCore.connectUserAndThread(bUser, thread);

                    Log.d(TAG, "Value: " + dataSnapshot.getValue());
                    Map<String, Object> values = null;
                    try {
                        values = (Map<String, Object>) dataSnapshot.getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Dont notify the system for the current user added.
                    if (userFirebaseID.equals(currentUser.getEntityID())) {

                        // Check to see if the thread was deleted from the user db.
                        if (values != null && values.containsKey(BDefines.Keys.BDeleted))
                        {
                            thread.setDeleted(true);
                            DaoCore.updateEntity(thread);
                        }
                        return;
                    }

                    if (thread.getTypeSafely() != BThread.Type.Public) {

                        // Check to see if the user has left this thread. If so we unlink it from the thread.
                        if (values != null &&  values.containsKey(BDefines.Keys.BLeaved))
                        {
                            BLinkData data =
                                    DaoCore.fetchEntityWithProperties(com.braunster.chatsdk.dao.BLinkData.class,
                                            new Property[]{BLinkDataDao.Properties.ThreadID, BLinkDataDao.Properties.UserID}, thread.getId(), bUser.getId());

                            if (data != null)
                            {
                                DaoCore.deleteEntity(data);
                            }
                        }
                        else
                        {
                            // Users that are members of threads are shown in contacts
                            currentUser.addContact(bUser);
                        }
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
