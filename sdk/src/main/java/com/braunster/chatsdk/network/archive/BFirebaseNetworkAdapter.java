package com.braunster.chatsdk.network.archive;/*
package com.braunster.chatsdk.network;

import android.util.Log;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

*/
/**
 * Created by itzik on 6/8/2014.
 *//*

public class BFirebaseNetworkAdapter extends AbstractNetworkAdapter {

    private static final String TAG = BFirebaseNetworkAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;

    //TODO need to implement
    @Override
    public void syncWithProgress(final CompletionListener completionListener) {
        if (DEBUG) Log.v(TAG, "SyncWithProgress");
        //TODO get facebook id from preferences;
        // TODO update progress dialog.
        String facebookID = BFacebookManager.getUserFacebookID();

        // If there is no facebook id cancel sync operation.
        if (facebookID == null)
        {
            if (DEBUG) Log.d(TAG, "Facebook Id is null");
            return;
        }

        // Loading the user entity from the local db.
        final BUser user = DaoCore.fetchOrCreateUserWithEntityAndFacebookID("", facebookID);

        if (DEBUG) Log.i(TAG, "Syncing user with facebookID, ID: " + facebookID +
                ", Name: " + (user.getName() != null ? user.getName() : "No name") );

        BFirebaseInterface.sharedManager().selectEntity(user, new CompletionListenerWithData<BUser>() {
            @Override
            public void onDone(BUser bUser) {
                if (DEBUG) Log.i(TAG, "OnDone, Entity fetched from Firebase, BUser name: " + (bUser == null ? "User is null" :bUser.getName()));

                // Delete Temp entity
                if (user.getEntityID() == null)
                {
                    if (DEBUG) Log.d(TAG, "Deleting tmp user");
                    DaoCore.deleteEntity(user);
                }

                // ASK not sure if looking for null.
                if (bUser == null)
                {
                    if (DEBUG) Log.d(TAG, "Fetched user from firebase is null");
                    bUser = new BUser();
                    bUser = DaoCore.createEntity(bUser);
                }

                Log.i(TAG, "Updating the user...");

                bUser.setName(BFacebookManager.userFacebookName);
                bUser.setFacebookID(BFacebookManager.userFacebookID);

                bUser.pictureExist = true;
                bUser.pictureURL = BFacebookManager.getCurrentUserProfilePicURL();
                bUser.setLastOnline(new Date());

                // Update the user.
                bUser.setLastUpdated(new Date());

                Log.i(TAG, "Pushing the updated user back to firebase...");
                BFirebaseInterface.sharedManager().pushEntity(bUser, new CompletionListenerWithData<BUser>() {
                    @Override
                    public void onDone(BUser bUser) {
                        Log.i(TAG, "OnDone, User is pushed.");
                        // TODO subscribe to push channel and add observer to this user.

                        // Saving the user again because the user now has entityID from the server.
                        DaoCore.updateEntity(bUser);
                        completionListener.onDone();
                    }

                    @Override
                    public void onDoneWithError() {
                        if (DEBUG) Log.e(TAG, "DoneWithError, PushEntity");
                        completionListener.onDoneWithError();
                    }
                });
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "DoneWithError, selectEntity");
                completionListener.onDoneWithError();
            }
        });
    }

    @Override
    public void getFriendsListWithListener(CompletionListenerWithData completionListener) {
        BFacebookManager.getUserFriendList(completionListener);
    }

    @Override
    public BUser currentUser() {
        return DaoCore.fetchEntityWithFacebookID(BUser.class, BFacebookManager.getUserFacebookID());
    }

    @Override
    public void sendMessage(BMessage message, final CompletionListenerWithData<BMessage> completionListener) {

        if (message.getOwnerThread() != null)
        {
            BFirebaseInterface.sharedManager().pushEntity(message, new CompletionListenerWithData<BMessage>() {
                @Override
                public void onDone(BMessage message) {

                    // This will return to the abstract Adapter call, The adapter then save the entity and callback to the calling method.
                    completionListener.onDone(message);
                    // TODO if the thread is private send push notificatin to all the other users.
                    // Check when recipients was last online
                    // Don't use push notifications for public threads because
                    // they could have hundreds of users and we don't want to be spammed
                    // with push notifications
                }

                @Override
                public void onDoneWithError() {
                    completionListener.onDoneWithError();
                }
            });
        }
    }

    @Override
    public void createThreadWithUsers(List<BUser> users, CompletionListenerWithData<Long> completionListener) {
        BUser user = currentUser();

        users.add(user);
    }

    @Override
    public void createPublicThreadWithName(String name, CompletionListener completionListener) {

    }

    @Override
    public void setLastOnline(Date lastOnline) {
        this.currentUser().setLastOnline(lastOnline);
    }

    @Override
    public void deleteThreadWithEntityID(String entityId, CompletionListener completionListener) {

    }

    //
    @Override
    public ArrayList<BThread> threadsWithType(BThread.Type type) {
        return null;
    }

    @Override
    public void deleteThread(BThread thread, CompletionListener completionListener) {

    }

    @Override
    public String serverURL() {
        return FirebasePaths.FIREBASE_PATH;
    }

    private void updateLastOnline(){
        setLastOnline(new Date());
    }
}
*/
