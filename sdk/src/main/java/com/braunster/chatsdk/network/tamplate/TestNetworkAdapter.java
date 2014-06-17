package com.braunster.chatsdk.network.tamplate;

import android.util.Log;

import com.braunster.chatsdk.dao.BLinkData;
import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BLinkedContactDao;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by itzik on 6/16/2014.
 */
public class TestNetworkAdapter extends AbstractNetworkAdapter {

    public static final String TAG = TestNetworkAdapter.class.getSimpleName();
    public static final boolean DEBUG = true;

    private List<BUser> list;

    private static boolean isTestCreated = false;

    @Override
    public void syncWithProgress(CompletionListener completionListener) {
        currentUser = DaoCore.fetchEntityWithProperty(BUser.class, BThreadDao.Properties.Name, "Dan");

        getFriendsListWithListener(new CompletionListenerWithData() {
            @Override
            public void onDone(Object o) {

            }

            @Override
            public void onDoneWithError() {

            }
        });

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
        // ASK if the owner is the user that added the contact or that owner is a llink to the contact profile.
        // Get the user contacts.
        List<BLinkedContact> linkedContacts = DaoCore.fetchEntitiesWithProperty(BLinkedContact.class, BLinkedContactDao.Properties.EntityID, currentUser().getEntityID());
        completionListenerWithData.onDone(linkedContacts);
        return;
    }

    @Override
    public void getFriendsListWithListener(CompletionListenerWithData completionListener) {

        if (isTestCreated)
        {
            completionListener.onDone(list);
            return;
        }

        isTestCreated = true;

        BUser user = new BUser();
        user.setEntityId(DaoCore.generateEntity());
        user.setName("Bob");
        user.hasApp = true;
        user.setOnline(true);
        user.pictureExist = true;
        user.pictureURL = "http://www.thedrinksbusiness.com/wordpress/wp-content/uploads/2012/05/Brad.jpg";

        BUser user1 = new BUser();
        user1.setEntityId(DaoCore.generateEntity());
        user1.setName("Giorgio");
        user1.hasApp = true;
        user1.setOnline(false);
        user1.pictureExist = true;
        user1.pictureURL = "http://www.insidespanishfootball.com/wp-content/uploads/2013/07/Cheillini-300x203.jpg";

        BUser user2 = new BUser();
        user2.setEntityId(DaoCore.generateEntity());
        user2.setName("Claudio");
        user2.setOnline(false);
        user2.hasApp = true;
        user2.pictureExist = true;
        user2.pictureURL = "http://www.affashionate.com/wp-content/uploads/2013/04/Claudio-Marchisio-season-2012-2013-claudio-marchisio-32347274-741-1024.jpg";

        BUser user3 = new BUser();
        user3.setEntityId(DaoCore.generateEntity());
        user3.setName("John");
        user3.hasApp = true;
        user3.setOnline(true);
        user3.pictureExist = true;
        user3.pictureURL = "http://images2.alphacoders.com/249/249012.jpg";

        list = new ArrayList<BUser>();
        list.add(user);
        list.add(user1);
        list.add(user2);
        list.add(user3);

        DaoCore.createEntity(user);
        DaoCore.createEntity(user1);
        DaoCore.createEntity(user2);
        DaoCore.createEntity(user3);

        BLinkedContact linkedContact = new BLinkedContact();
        linkedContact.setEntityId(currentUser().getEntityID());
        linkedContact.setOwner(user.getEntityID());
        DaoCore.createEntity(linkedContact);

        BLinkedContact linkedContact1 = new BLinkedContact();
        linkedContact1.setEntityId(currentUser().getEntityID());
        linkedContact1.setOwner(user1.getEntityID());
        DaoCore.createEntity(linkedContact1);

        BLinkedContact linkedContact2 = new BLinkedContact();
        linkedContact2.setEntityId(currentUser().getEntityID());
        linkedContact2.setOwner(user2.getEntityID());
        DaoCore.createEntity(linkedContact2);

        BLinkedContact linkedContact3 = new BLinkedContact();
        linkedContact3.setEntityId(currentUser().getEntityID());
        linkedContact3.setOwner(user3.getEntityID());
        DaoCore.createEntity(linkedContact3);


        completionListener.onDone(list);
    }

    @Override
    public BUser currentUser() {
        return currentUser;
    }

    @Override
    public void sendMessage(BMessage message, CompletionListenerWithData<BMessage> completionListener) {
        if (DEBUG) Log.v(TAG, "sendMessageWithText");
        /* DO server stuff with the message*/
        // Generate id for the message, The id will come from the server.
        message.setEntityId(DaoCore.generateEntity());
        completionListener.onDone(message);
    }

    @Override
    public void createThreadWithUsers(List<BUser> users, CompletionListenerWithData<String> completionListener) {

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

            firUserThreads = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Creator, one.getEntityID());
            secUserThread = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Creator, sec.getEntityID());

            for (BThread t : firUserThreads)
                if (t.getUsers().size() == 2)
                    if (t.getUsers().get(0).getBUser().getEntityID().equals(sec.getEntityID())
                            || t.getUsers().get(1).getBUser().getEntityID().equals(sec.getEntityID()))
                    {
                        completionListener.onDone(t.getEntityID());
                        return;
                    }

            for (BThread t : secUserThread)
                if (t.getUsers().size() == 2)
                {
                    if (t.getUsers().get(0).getBUser().getEntityID().equals(one.getEntityID())
                            || t.getUsers().get(1).getBUser().getEntityID().equals(one.getEntityID()))
                    {
                        completionListener.onDone(t.getEntityID());
                        return;
                    }
                }
        }
        // Creating the thread.
        BThread thread = new BThread();
        thread.setEntityID(DaoCore.generateEntity());
        thread.setCreator(currentUser().getEntityID());
        thread.setType(BThread.Type.Private.ordinal());

        DaoCore.createEntity(thread);

        // Linking the users to the thread.
        BLinkData linkData;
        for (BUser u : users)
        {
            linkData = new BLinkData();
            linkData.setUserID(u.getEntityID());
            linkData.setThreadID(thread.getEntityID());
            DaoCore.createEntity(linkData);
        }

        activityListener.onThreadAdded(thread);
        completionListener.onDone(thread.getEntityID());
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
