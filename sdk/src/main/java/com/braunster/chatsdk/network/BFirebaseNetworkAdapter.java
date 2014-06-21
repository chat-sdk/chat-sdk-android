package com.braunster.chatsdk.network;

import android.util.Log;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.firebase.FirebasePaths;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */
public class BFirebaseNetworkAdapter extends AbstractNetworkAdapter {

    private static final String TAG = BFirebaseNetworkAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;

    //TODO need to implement
    @Override
    public void syncWithProgress(CompletionListener completionListener) {
        //TODO get facebook id from preferences;
        // TODO update progress dialog.
        String facebookID = BFacebookManager.getUserFacebookID();

        if (facebookID == null)
        {
            if (DEBUG) Log.d(TAG, "Facebook Id is null");
            return;
        }

        BUser user = DaoCore.fetchOrCreateUserWithEntityID(null, facebookID);

//        Firebase ref = FirebasePaths.firebaseRef().appendPathComponent()
/*
        Firebase * ref = [[Firebase firebaseRef] appendPathComponent:entity.getPath.getPath];

        // If possible get the entity by it's address
        if(entity.entityID && entity.entityID.length) {

            [ref observeSingleEventOfType:FEventTypeValue withBlock:^(FDataSnapshot * snapshot) {
                id object = [BFirebaseInterface objectForSnapshot:snapshot];
                if ([object isKindOfClass:[NSArray class]]) {
                    object = ((NSArray *) object).firstObject;
                }
                completion(object);
            }];
        }
*/

    }

    @Override
    public void getFriendsListWithListener(CompletionListenerWithData completionListener) {
        BFacebookManager.getUserFriendList(completionListener);
    }

    @Override
    public BUser currentUser() {
        //TODO return the user from the local database
        return DaoCore.fetchEntityWithFacebookID(BUser.class, BFacebookManager.getUserFacebookID());
    }

    @Override
    public void sendMessage(BMessage message, CompletionListenerWithData<BMessage> completionListener) {

    }

    @Override
    public void createThreadWithUsers(List<BUser> users, CompletionListenerWithData<String> completionListener) {

    }

    @Override
    public void createPublicThreadWithName(String name, CompletionListener completionListener) {

    }

    @Override
    public void setLastOnline(Date lastOnline) {

    }

    @Override
    public void deleteThreadWithEntityID(String entityId, CompletionListener completionListener) {

    }

    // FIXME type
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
}
