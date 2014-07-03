package com.braunster.chatsdk.listeners;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.network.firebase.BPath;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.object.FirebaseEventCombo;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Config;
import com.firebase.client.DataSnapshot;
import com.firebase.client.EventTarget;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


import static com.braunster.chatsdk.object.FirebaseEventCombo.*;

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
    private int type = -1;
    private static String currentUserEntityID;

    public static UserAddedToThreadListener getNewInstance(String threadID, int type, Handler handler) {
        UserAddedToThreadListener userAddedToThreadListener = new UserAddedToThreadListener(threadID,type,  handler);
        return userAddedToThreadListener;
    }

    public UserAddedToThreadListener(String threadID, int type, Handler handler){
        super(ChildEvent);
        this.handler = handler;
        this.threadID = threadID;
        this.type = type;
    }

    public static void setCurrentUserEntityID(String currentUserEntityID) {
        UserAddedToThreadListener.currentUserEntityID = currentUserEntityID;
    }

    public static void removeEvent(){

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
////                if (DEBUG) Log.d(TAG, "Datasnapshot cildren: " + dataSnapshot.getChildren());
//                BPath path = BPath.pathWithPath(dataSnapshot.getRef().toString());
//                String userFirebaseID = path.idForIndex(1);
//
//                if (userFirebaseID.equals(currentUserEntityID)) {
//                    return;
//                }
//
//                BUser bUser;
//                // If the user already has  listening to this user we can fetch it from the db because he is up to date.
//                if (usersIds.contains(userFirebaseID))
//                    bUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);
//                else
////                    // For each user we'd then need to add them to the database
//                    bUser = (BUser) BFirebaseInterface.objectFromSnapshot(dataSnapshot);
//
//                if (type != BThread.Type.Public)
//                {
//                    // Users that are members of threads are shown in contacts
////                    BNetworkManager.sharedManager().getNetworkAdapter().currentUser().addContact(bUser);
//                }
//
//                // If this isn't the current user //TODO check if dosen't have listener allready.
//                if (!usersIds.contains(bUser.getEntityID()))
//                {
//                    usersIds.add(bUser.getEntityID());
//                    // We use this variable so that we don't add multiple listeners to one
//                    // user object - this would happen otherwise because one user
//                    // could be in multiple threads
////                    bUser.listenerAdded = YES;
//                    // ASK is this saved in the db or its a veriable of user.
//
//                    // Monitor the user to see if it changes in the future
//                    final FirebasePaths userRef = FirebasePaths.userRef(bUser.getEntityID());
//
//                    userDetailsChangeListener = new FirebaseGeneralEvent(ValueEvent) {
//                        @Override
//                        public void onDataChange(final DataSnapshot snapshot) {
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (DEBUG) Log.v(TAG, "User Details has changed.");
//                                    BUser user = (BUser) BFirebaseInterface.objectFromSnapshot(snapshot);
//                                    Message message = new Message();
//                                    message.what = 0;
//                                    message.obj = user;
//                                    handler.sendMessage(message);
//                                }
//                            }).start();
//                        }
//
//                        @Override
//                        public void onCancelled(FirebaseError firebaseError) {
//
//                        }
//                    };


                    final FirebasePaths ref = FirebasePaths.threadRef(threadID);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.e(TAG, "#####################33"+ref.toString());
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

//                    userRef.addValueEventListener(userDetailsChangeListener);
//
//                    combo = FirebaseEventCombo.getNewInstance(userDetailsChangeListener, userRef, bUser.getEntityID());
//                }
            }
        }).start();
    }

    
    public void diatachFromUser(){
        ((Query) combo.getRef()).removeEventListener((ValueEventListener) userDetailsChangeListener);
//        for (FirebaseEventCombo combo : refsAndListener)
//        {
//            if (combo.getTag()!=null && combo.getTag().equals(userID))
//            {
//                ((Query) combo.getRef()).removeEventListener((ValueEventListener) combo.getListener());
//                refsAndListener.remove(combo);
//                usersIds.remove(userID);
//                break;
//            }
//        }
    }
}
