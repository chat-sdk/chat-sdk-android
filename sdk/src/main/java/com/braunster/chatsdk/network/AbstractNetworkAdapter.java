package com.braunster.chatsdk.network;

import android.location.LocationManager;
import android.widget.ImageView;

import com.braunster.chatsdk.entities.BMessage;
import com.braunster.chatsdk.entities.BThread;
import com.braunster.chatsdk.entities.BUser;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */
public abstract class AbstractNetworkAdapter {

    private static final String TAG = AbstractNetworkAdapter.class.getSimpleName();
    private static final boolean DEBUG = false;

    private ActivityListener newDataListener;

    /*
    * ASK What is withProgress stand for? some progress dialog that pops up?
    * ASK about the assert stuff in the code, guessing that it is not relevant but interesting.
    * */

     /** Syncing the user details. first try to retrieve the user from the Local DB and then gather more data from the user FB Profile.*/
    public  abstract void syncWithProgress(CompletionListener completionListener);

    /** Get user facebook friends, Calls The FBManager for preforming the fetch friends task.*/
    public abstract void getFriendsListWithListener(CompletionListenerWithData<List<GraphUser>> completionListener);

    /** ASK I have no idea what is id<PUser> stands for.*/
    public abstract BUser currentUser();

    /** Send message by given data stored in the BMessage Obj.*/
    public abstract void sendMessage(BMessage message, CompletionListener completionListener);

    /** Create a new messaged thread with the given users as participants.*/ // ASK Is this private or public ?
    public abstract void createThreadWithUsers(ArrayList<BUser> users, CompletionListener completionListener);

    /** Create a public thread for given name.*/
    public abstract void createPublicThreadWithName(String name, CompletionListener completionListener);

    /** Set the last time the user has been online.*/
    public abstract void setLastOnline(Date lastOnline);

    /** Delete thread for given id.*/
    public abstract void deleteThreadWithEntityID(String entityId, CompletionListener completionListener);

    public abstract ArrayList<BThread> threadsWithType(BThread.threadType type/*ASK what this mean bThreadType type, and if truly return List of threads.*/);

    /** Delete thread by given BThread Obj given.*/
    public abstract void deleteThread(BThread thread, CompletionListener completionListener);

    /** Set a listener that will notify the registered class each time a thread or a message is added. */
    public void setNewDataListener(ActivityListener newDataListener){
        this.newDataListener = newDataListener;
    }

    public abstract String serverURL();

    /** Send text message.*/
    public void sendMessageWithText(String text, String threadEntityId, CompletionListener completionListener){
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
    }

    /** Send message with an image.*/
    public void sendMessageWithImage(ImageView imageView, String threadEntityId, CompletionListener completionListener){
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
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
    public ArrayList<BMessage> getMessagesForThreadForEntityID(String entityId){
        /* Get the messages by pre defined order*/
        return null;
    }
}
