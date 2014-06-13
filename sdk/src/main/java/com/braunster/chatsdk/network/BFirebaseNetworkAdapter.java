package com.braunster.chatsdk.network;

import com.braunster.chatsdk.entities.BMessage;
import com.braunster.chatsdk.entities.BThread;
import com.braunster.chatsdk.entities.BUser;
import com.braunster.chatsdk.firebase.FirebasePaths;
import com.braunster.chatsdk.firebase.FirebaseTags;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.facebook.model.GraphUser;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */
public class BFirebaseNetworkAdapter extends AbstractNetworkAdapter {
    //TODO need to implement
    @Override
    public void syncWithProgress(CompletionListener completionListener) {

    }

    @Override
    public void getFriendsListWithListener(CompletionListenerWithData<List<GraphUser>> completionListener) {
        BFacebookManager.getUserFriendList(completionListener);
    }

    @Override
    public BUser currentUser() {
        //TODO return the user from the local database
        return null;
    }

    @Override
    public void sendMessage(BMessage message, CompletionListener completionListener) {

    }

    @Override
    public void createThreadWithUsers(ArrayList<BUser> users, CompletionListener completionListener) {

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

    @Override
    public ArrayList<BThread> threadsWithType(BThread.threadType type) {
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
