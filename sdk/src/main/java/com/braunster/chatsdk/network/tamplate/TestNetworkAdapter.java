package com.braunster.chatsdk.network.tamplate;

import android.util.Log;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by itzik on 6/16/2014.
 */
public class TestNetworkAdapter extends AbstractNetworkAdapter {

    public static final String TAG = TestNetworkAdapter.class.getSimpleName();
    public static final boolean DEBUG = true;

    @Override
    public void syncWithProgress(CompletionListener completionListener) {
        currentUser = DaoCore.fetchEntityWithProperty(BUser.class, BThreadDao.Properties.Name, "Dan");

        if (completionListener == null)
            return;

        if (currentUser == null)
        {
            if (DEBUG) Log.e(TAG, "User is null");
            completionListener.onDoneWithError();
        }
        else
            completionListener.onDone();
    }

    @Override
    public void getFriendsListWithListener(CompletionListenerWithData<List<GraphUser>> completionListener) {

    }

    @Override
    public BUser currentUser() {
        return currentUser;
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
    public void deleteThread(BThread thread, CompletionListener completionListener) {

    }

    @Override
    public String serverURL() {
        return null;
    }
}
