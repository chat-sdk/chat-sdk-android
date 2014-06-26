/*
package com.braunster.chatsdk.network;

import android.location.LocationManager;
import android.util.Base64;
import android.util.Log;

import com.braunster.chatsdk.Utils.MsgSorter;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.braunster.chatsdk.dao.BMessage.Type.bImage;
import static com.braunster.chatsdk.dao.BMessage.Type.bLocation;
import static com.braunster.chatsdk.dao.BMessage.Type.bText;

*/
/**
 * Created by itzik on 6/8/2014.
 *//*

public abstract class AbstractNetworkAdapter {

    private static final String TAG = AbstractNetworkAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;

    public ActivityListener activityListener;

    protected BUser currentUser;
    */
/*
    * ASK What is withProgress stand for? some progress dialog that pops up?
    * ASK about the assert stuff in the code, guessing that it is not relevant but interesting.
    * ASK no undo for green dao
    * *//*


     */
/** Syncing the user details. first try to retrieve the user from the Local DB and then gather more data from the user FB Profile.*//*

    public abstract void syncWithProgress(CompletionListener completionListener);

    */
/** Get user facebook friends, Calls The FBManager for preforming the fetch friends task.*//*

    public abstract void getFriendsListWithListener(CompletionListenerWithData completionListener);

    */
/** ASK I have no idea what is id<PUser> stands for.*//*

    public abstract BUser currentUser();

    */
/** Send message by given data stored in the BMessage Obj.*//*

    public abstract void sendMessage(BMessage message, CompletionListenerWithData<BMessage> completionListener);

    */
/** Create a new messaged thread with the given users as participants.*//*

    public abstract void createThreadWithUsers(List<BUser> users, CompletionListenerWithData<Long> completionListener);

    */
/** Create a public thread for given name.*//*

    public abstract void createPublicThreadWithName(String name, CompletionListener completionListener);

    */
/** Set the last time the user has been online.*//*

    public abstract void setLastOnline(Date lastOnline);

    */
/** Delete thread for given id.*//*

    public abstract void deleteThreadWithEntityID(String entityId, CompletionListener completionListener);

    public ArrayList<BThread> threadsWithType(BThread.Type type){
        if (DEBUG) Log.v(TAG, "threadsWithType, Type: " + type.ordinal());
        ArrayList<BThread> threads = new ArrayList<BThread>();
        List<BThread> list = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, type.ordinal());

        if (DEBUG) Log.d(TAG, "Thread, Amount: " + list.size() );
        for (BThread th : list)
        {
            // FIXME: Thread messages is not up do date for some reason...
            // FIXME: Calling messages separately for each thread.
            th.setMessages(getMessagesForThreadForEntityID(th.getId()));
//            if (DEBUG) Log.d(TAG, "Messages from method: " + getMessagesForThreadForEntityID(th.getEntityID()).size());
            if (DEBUG) Log.d(TAG, "Messages, Amount: " + th.getMessages().size() );
            if (th.getMessages().size() > 0 || th.getBUser().equals(currentUser()))
            {
                threads.add(th);

//                Log.i(TAG, "Before - FirstMessageText: " + th.getMessages().get(0).getText());
                Collections.sort(th.getMessages(), new MsgSorter());
//                Log.i(TAG, "After - FirstMessageText: " + th.getMessages().get(0).getText());
            }
        }

        // TODO sort thread so the one with newest message will be on top.
        // http://stackoverflow.com/questions/18895915/how-to-sort-an-array-of-objects-in-java
        // http://stackoverflow.com/questions/12449766/java-sorting-sort-an-array-of-objects-by-property-object-not-allowed-to-use-co

        return threads;
    }

    */
/** Delete thread by given BThread Obj given.*//*

    public abstract void deleteThread(BThread thread, CompletionListener completionListener);

    */
/** Set a listener that will notify the registered class each time a thread or a message is added. *//*

    public void setActivityListener(ActivityListener newDataListener){
        this.activityListener = newDataListener;
    }

    public abstract String serverURL();

    */
/** Send text message.*//*

    public void sendMessageWithText(String text, long threadEntityId, final CompletionListenerWithData<BMessage> completionListener){
        if (DEBUG) Log.v(TAG, "sendMessageWithText");
        */
/* Prepare the message object for sending, after ready send it using send message abstract method.*//*

        final BMessage message = new BMessage();
        message.setText(text);
        message.setOwnerThread(threadEntityId);
        message.setType(bText.ordinal());
        message.setDate(new Date());
        message.setBUserSender(currentUser());

        sendMessage(message, new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage bMessage) {
                DaoCore.createEntity(bMessage);
                completionListener.onDone(bMessage);
            }

            @Override
            public void onDoneWithError() {
                completionListener.onDoneWithError();
            }
        });
    }

    */
/** Send message with an image.*//*

    public void sendMessageWithImage(File image, long threadEntityId, final CompletionListenerWithData<BMessage> completionListener){
        */
/* Prepare the message object for sending, after ready send it using send message abstract method.*//*


        // http://stackoverflow.com/questions/13119306/base64-image-encoding-using-java

        final BMessage message = new BMessage();
        message.setOwnerThread(threadEntityId);
        message.setType(bImage.ordinal());
        message.setDate(new Date());
        message.setBUserSender(currentUser());
        try {
            message.setText(Base64.encodeToString(FileUtils.readFileToByteArray(image), Base64.DEFAULT));
        } catch (IOException e) {
            e.printStackTrace();
            if (DEBUG) Log.e(TAG, "Error encoding file");
            completionListener.onDoneWithError();
            return;
        }

        sendMessage(message, new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage bMessage) {
                if (DEBUG) Log.v(TAG, "sendMessageWithImage, onDone. Message ID: " + bMessage.getEntityID());
                DaoCore.createEntity(bMessage);
                completionListener.onDone(bMessage);
            }

            @Override
            public void onDoneWithError() {
                completionListener.onDoneWithError();
            }
        });


    }

    */
/**@see "http://developer.android.com/guide/topics/location/strategies.html"
     * Send user location.*//*

    public void sendMessageWithLocation(String base64File, LatLng location, long threadEntityId, final CompletionListenerWithData<BMessage> completionListener){
        */
/* Prepare the message object for sending, after ready send it using send message abstract method.*//*

        final BMessage message = new BMessage();
        message.setOwnerThread(threadEntityId);
        message.setType(bLocation.ordinal());
        message.setDate(new Date());
        message.setBUserSender(currentUser());

        // Add the LatLng data to the message and the base64 picture of the message if has any.
        message.setText(String.valueOf(location.latitude) + "&" + String.valueOf(location.longitude) + (base64File!=null? "&" + base64File : ""));

        sendMessage(message, new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage bMessage) {
                if (DEBUG) Log.v(TAG, "sendMessageWithLocation, onDone. Message ID: " + bMessage.getEntityID());
                DaoCore.createEntity(bMessage);
                completionListener.onDone(bMessage);
            }

            @Override
            public void onDoneWithError() {
                completionListener.onDoneWithError();
            }
        });
    }

    */
/** Save user data to the local database*//*

    public void save(){
        */
/* Save data in local db.*//*

    }

    // TODO add order veriable for the data.
    */
/** Get all messages for given thread id ordered Ascending/Descending*//*

    public List<BMessage> getMessagesForThreadForEntityID(Long id){
        */
/* Get the messages by pre defined order*//*

        return DaoCore.fetchEntitiesWithProperty(BMessage.class, BMessageDao.Properties.OwnerThread, id);
    }
}
*/
