package com.braunster.chatsdk.network;

import android.location.LocationManager;
import android.util.Log;
import android.widget.ImageView;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.DaoCore;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.braunster.chatsdk.dao.BMessage.Type.bText;

/**
 * Created by itzik on 6/8/2014.
 */
public abstract class AbstractNetworkAdapter {

    private static final String TAG = AbstractNetworkAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;

    private ActivityListener newDataListener;

    protected BUser currentUser;
    /*
    * ASK What is withProgress stand for? some progress dialog that pops up?
    * ASK about the assert stuff in the code, guessing that it is not relevant but interesting.
    * ASK no undo for green dao
    * */

     /** Syncing the user details. first try to retrieve the user from the Local DB and then gather more data from the user FB Profile.*/
    public abstract void syncWithProgress(CompletionListener completionListener);

    /** Get user facebook friends, Calls The FBManager for preforming the fetch friends task.*/
    public abstract void getFriendsListWithListener(CompletionListenerWithData<List<GraphUser>> completionListener);

    /** ASK I have no idea what is id<PUser> stands for.*/
    public abstract BUser currentUser();

    /** Send message by given data stored in the BMessage Obj.*/
    public abstract void sendMessage(BMessage message, CompletionListener completionListener);

    /** Create a new messaged thread with the given users as participants.*/
    public abstract void createThreadWithUsers(ArrayList<BUser> users, CompletionListener completionListener);

    /** Create a public thread for given name.*/
    public abstract void createPublicThreadWithName(String name, CompletionListener completionListener);

    /** Set the last time the user has been online.*/
    public abstract void setLastOnline(Date lastOnline);

    /** Delete thread for given id.*/
    public abstract void deleteThreadWithEntityID(String entityId, CompletionListener completionListener);

    public ArrayList<BThread> threadsWithType(BThread.Type type){
        if (DEBUG) Log.v(TAG, "threadsWithType, Type: " + type.ordinal());
        ArrayList<BThread> threads = new ArrayList<BThread>();
        List<BThread> list = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, type.ordinal());

        if (DEBUG) Log.d(TAG, "Thread, Amount: " + list.size() );
        for (BThread th : list)
        {
            if (DEBUG) Log.d(TAG, "Messages, Amount: " + th.getMessages().size() );
            if (th.getMessages().size() > 0 || th.getBUser().equals(currentUser()))
                threads.add(th);
        }

        // TODO sort thread so the one with newest message will be on top.
        // http://stackoverflow.com/questions/18895915/how-to-sort-an-array-of-objects-in-java
        // http://stackoverflow.com/questions/12449766/java-sorting-sort-an-array-of-objects-by-property-object-not-allowed-to-use-co

        return threads;
    }

    /** Delete thread by given BThread Obj given.*/
    public abstract void deleteThread(BThread thread, CompletionListener completionListener);

    /** Set a listener that will notify the registered class each time a thread or a message is added. */
    public void setNewDataListener(ActivityListener newDataListener){
        this.newDataListener = newDataListener;
    }

    public abstract String serverURL();

    /** Send text message.*/
    public void sendMessageWithText(String text, String threadEntityId, final CompletionListener completionListener){
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
        final BMessage message = new BMessage();
        message.setText(text);
        message.setOwnerThread(threadEntityId);
        message.setType(bText.ordinal());
        message.setDate(new Date(System.currentTimeMillis()));
        message.setBUserSender(currentUser());

        sendMessage(message, new CompletionListener() {
            @Override
            public void onDone() {
                DaoCore.createEntity(message);
                completionListener.onDone();
            }

            @Override
            public void onDoneWithError() {
                completionListener.onDoneWithError();
            }
        });
    }

    /** Send message with an image.*/
    public void sendMessageWithImage(ImageView imageView, String threadEntityId, CompletionListener completionListener){
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/

        // TODO Encode Image from 64 Bit Encoding
        // http://stackoverflow.com/questions/13119306/base64-image-encoding-using-java
    }

    /**@see "http://developer.android.com/guide/topics/location/strategies.html"
     * Send user location.*/
    public void sendMessageWithLocation(LocationManager locationManager, String threadEntityId, CompletionListener completionListener){
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
    }

    /** Save user data to the local database*/
    public void save(){
        /* Save data in local db.*/
    }

    // TODO add order veriable for the data.
    /** Get all messages for given thread id ordered Ascending/Descending*/
    public List<BMessage> getMessagesForThreadForEntityID(String entityId){
        /* Get the messages by pre defined order*/
        return DaoCore.fetchEntitiesWithProperty(BMessage.class, BMessageDao.Properties.OwnerThread, entityId);
    }
}
