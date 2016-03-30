package com.braunster.chatsdk.network.tamplate;/*
package com.braunster.chatsdk.network.tamplate;

import android.util.Log;

import com.braunster.chatsdk.dao.BLinkData;
import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BLinkedContactDao;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

*/
/**
 * Created by itzik on 6/16/2014.
 *//*

public class TestNetworkAdapter extends AbstractNetworkAdapter {

    public static final String TAG = TestNetworkAdapter.class.getSimpleName();
    public static final boolean DEBUG = true;

    private List<BUser> list;

    private static boolean isTestCreated = false;

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

    // ASK if needed
    public  void getContactListWithListener(CompletionListenerWithData<List<BLinkedContact>> completionListenerWithData){
        // So we dont create the friends again.
        */
/*
          * ASK if the owner is the user that added the contact or that owner is a llink to the contact profile.
          * ASK Do each user has one Linked contact that has in it a list of all the users that are his friends?.
            *//*


        // Get the user contacts.
        List<BLinkedContact> linkedContacts = DaoCore.fetchEntitiesWithProperty(BLinkedContact.class, BLinkedContactDao.Properties.EntityID, currentUser().getEntityID());
        completionListenerWithData.onDone(linkedContacts);
        return;
    }

    @Override
    public void getFriendsListWithListener(final CompletionListenerWithData completionListener) {

        getContactListWithListener(new CompletionListenerWithData<List<BLinkedContact>>() {
            @Override
            public void onDone(List<BLinkedContact> bLinkedContacts) {
                List<BUser> users = new ArrayList<BUser>();
                for (BLinkedContact c : bLinkedContacts)
                    users.add(c.getContact());

                completionListener.onDone(users);
            }

            @Override
            public void onDoneWithError() {

            }
        });
    }

    @Override
    public BUser currentUser() {
        return currentUser;
    }

    @Override
    public void sendMessage(BMessage message, CompletionListenerWithData<BMessage> completionListener) {
        if (DEBUG) Log.v(TAG, "sendMessage");
        */
/* DO server stuff with the message*//*

        // Generate id for the message, The id will come from the server.
        message.setEntityId(DaoCore.generateEntity());
        completionListener.onDone(message);
    }

    @Override
    public void createThreadWithUsers(List<BUser> users, CompletionListenerWithData<Long> completionListener) {

        // IF one on one chat
        if (users.size() == 2)
        {
            // Check if there's already a thread for this two users.
            List<BThread> firUserThreads, secUserThread;

            BUser one = users.get(0), sec = users.get(1);

            //region Check Null
            if (one == null)
            {
                if (DEBUG) Log.e(TAG, "First user is null");
                return;
            }
            else if (one.getEntityID() == null)
            {
                if (DEBUG) Log.e(TAG, "First user entity is null");
                return;
            }

            if (sec == null)
            {
                if (DEBUG) Log.e(TAG, "Second user is null");
                return;
            }
            else if (one.getEntityID() == null)
            {
                if (DEBUG) Log.e(TAG, "First user entity is null");
                return;
            }
            //endregion

            firUserThreads = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Creator, one.getId());
            secUserThread = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Creator, sec.getId());

            for (BThread t : firUserThreads)
                if (t.getUsers().size() == 2)
                    if (t.getUsers().get(0).getId().equals(sec.getId())
                            || t.getUsers().get(1).getId().equals(sec.getId()))
                    {
                        completionListener.onDone(t.getId());
                        return;
                    }

            for (BThread t : secUserThread)
                if (t.getUsers().size() == 2)
                {
                    if (t.getUsers().get(0).getEntityID().equals(one.getEntityID())
                            || t.getUsers().get(1).getEntityID().equals(one.getEntityID()))
                    {
                        completionListener.onDone(t.getId());
                        return;
                    }
                }
        }
        // Creating the thread.
        BThread thread = new BThread();
        thread.setEntityID(DaoCore.generateEntity());
        thread.setCreator(currentUser().getId());
        thread.setType(BThread.Type.Private.ordinal());

        DaoCore.createEntity(thread);

        // Linking the users to the thread.
        BLinkData linkData;
        for (BUser u : users)
        {
            linkData = new BLinkData();
            linkData.setUserID(u.getId());
            linkData.setThreadID(thread.getId());
            DaoCore.createEntity(linkData);
        }

        activityListener.onThreadDetailsChanged(thread);
        completionListener.onDone(thread.getId());
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
*/
