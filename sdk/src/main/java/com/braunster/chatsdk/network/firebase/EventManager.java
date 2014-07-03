package com.braunster.chatsdk.network.firebase;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.Utils.MsgSorter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.events.Event;
import com.braunster.chatsdk.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.events.AppEventListener;
import com.braunster.chatsdk.events.MessageEventListener;
import com.braunster.chatsdk.events.ThreadEventListener;
import com.braunster.chatsdk.events.UserEventListener;
import com.braunster.chatsdk.listeners.IncomingMessagesListener;
import com.braunster.chatsdk.listeners.ThreadDetailsChangeListener;
import com.braunster.chatsdk.listeners.UserAddedToThreadListener;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.object.FirebaseEventCombo;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.Entry;

/**
 * Created by braunster on 30/06/14.
 */

public class EventManager implements AppEvents {

    private static final String TAG = EventManager.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static EventManager instance;

    private static final String MSG_PREFIX = "msg_";
    private static final String USER_PREFIX = "user_";

    private List<Event> eventsList = new ArrayList<Event>();
    private List<UserEventListener> userEventList = new ArrayList<UserEventListener>();
    private List<MessageEventListener> messageEventList = new ArrayList<MessageEventListener>();
    private List<ThreadEventListener> threadEventList = new ArrayList<ThreadEventListener>();
    private List<String> tags = new ArrayList<String>();
    public List<String> threadsIds = new ArrayList<String>();

    public Map<String, FirebaseEventCombo> listenerAndRefs = new HashMap<String, FirebaseEventCombo>();

    public static EventManager getInstance(){
        if (instance == null)
            instance = new EventManager();

        return instance;
    }

    private EventManager(){

    }

    /*TODO Events first triggered the the specific event listeners saved in the list's.
    *  If one of those events return true then the event wont trigger any AppEventListener. */

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 0:
                    onUserDetailsChange((BUser) msg.obj);
                    break;

                case 1:
                    onMessageReceived((BMessage) msg.obj);
                    break;

                case 2:
                    onThreadAdded((String) msg.obj);
                    break;
            }
        }
    } ;

    @Override
    public boolean onUserDetailsChange(BUser user) {
        for (Event ae : eventsList)
            ae.onUserDetailsChange(user);

        for (UserEventListener ue : userEventList)
        {
            // We check to see if the listener specified a specific user that he wants to listen to.
            // If we could find and match the data we ignore it.
            if (!ue.getEntityId().equals(""))
                if (user.getEntityID() != null && !ue.getEntityId().equals("") && user.getEntityID().equals(ue.getEntityId()))
                    return false;

            ue.onUserDetailsChange(user);
        }

        return false;
    }

    @Override
    public boolean onMessageReceived(BMessage message) {

        for (MessageEventListener me : messageEventList)
        {
            // We check to see if the listener specified a specific thread that he wants to listen to.
            // If we could find and match the data we ignore it.
            if (!me.getEntityId().equals(""))
                if (message.getBThreadOwner() != null && message.getBThreadOwner().getEntityID() != null)
                    if (!message.getBThreadOwner().getEntityID().equals(me.getEntityId()))
                        return false;

            me.onMessageReceived(message);
        }

        for (AppEvents ae : eventsList)
            ae.onMessageReceived(message);

        return false;
    }

    @Override
    public boolean onThreadAdded(String threadId) {
        for (Event ae : eventsList)
            ae.onThreadAdded(threadId);

        // TODO add option to listen to specific thread and from specific type.
        for (ThreadEventListener te : threadEventList)
            te.onThreadAdded(threadId);

        return false;
    }

    public void addEventIfNotExist(Event event){
         if (isTagExist(event.getTag()))
             return;

        if (event instanceof ThreadEventListener)
            threadEventList.add((ThreadEventListener) event);

        else if (event instanceof MessageEventListener)
            messageEventList.add((MessageEventListener) event);

        else if (event instanceof UserEventListener)
            userEventList.add((UserEventListener) event);

        else eventsList.add(event);
    }

    public void addAppEvent(AppEventListener appEvents){
        eventsList.add(appEvents);
        tags.add(appEvents.getTag());
    }

    public void addThreadEvent(ThreadEventListener threadEvent){
        threadEventList.add(threadEvent);
        tags.add(threadEvent.getTag());
    }

    public void addMessageEvent(MessageEventListener messgaeEvent){
        messageEventList.add(messgaeEvent);
        tags.add(messgaeEvent.getTag());
    }

    public void addUserEvent(UserEventListener userEvent){
        userEventList.add(userEvent);
        tags.add(userEvent.getTag());
    }

    public void removeEventByTag(String tag){
        tags.remove(tag);

        for (Event ae : eventsList)
            if (ae.getTag().equals(tag))
            {
                eventsList.remove(ae);
                return;
            }

        for (MessageEventListener me : messageEventList)
            if (me.getTag().equals(tag))
            {
                messageEventList.remove(me);
                return;
            }

        for (ThreadEventListener te : threadEventList)
            if (te.getTag().equals(tag))
            {
                threadEventList.remove(te);
                return;
            }

        for (UserEventListener ue : userEventList)
            if (ue.getTag().equals(tag))
            {
                userEventList.remove(ue);
                return;
            }
    }

    public void removeAllEvents(){
        Set<String> Keys = listenerAndRefs.keySet();

        FirebaseEventCombo combo;

        for (String key : Keys)
        {
            if (DEBUG) Log.d(TAG, "Removing listener, Key: " + key);

            combo = listenerAndRefs.get(key);

            if (combo.getListener().getType() == FirebaseGeneralEvent.ChildEvent)
            {
                if (combo.getListener() instanceof UserAddedToThreadListener)
                    ((UserAddedToThreadListener) combo.getListener()).diatachFromUser();

                if (DEBUG) Log.d(TAG, "Removing ChildEvent");
                ((Query) combo.getRef()).removeEventListener((ChildEventListener) combo.getListener());
            }
            else if (combo.getListener().getType() == FirebaseGeneralEvent.ValueEvent)
            {
                if (DEBUG) Log.d(TAG, "Removing ValueEvent.");
                ((Query) combo.getRef()).removeEventListener((ValueEventListener) combo.getListener());
            }
        }

        listenerAndRefs.clear();

        eventsList.clear();
        userEventList.clear();
        messageEventList.clear();
        threadEventList.clear();
        tags.clear();
        threadsIds.clear();
    }

    private void habdleThreadDetails(final BThread thread, FirebasePaths threadRef){
        // Add an observer to the thread details so we get
        // updated when the thread details change
        FirebasePaths detailsRef = threadRef.appendPathComponent(BFirebaseDefines.Path.BDetailsPath);
        FirebaseEventCombo combo = getCombo(thread.getEntityID(), detailsRef, new ThreadDetailsChangeListener(thread.getEntityID(), handler));

        detailsRef.addValueEventListener(combo.getListener());
    }

    private void handleUsers(final BThread thread ){
        // Also listen to the thread users
        // This will allow us to update the users in the database
        Firebase threadUsers = FirebasePaths.threadRef(thread.getEntityID()).child(BFirebaseDefines.Path.BUsersPath);

        FirebaseEventCombo combo = getCombo(USER_PREFIX + thread.getEntityID(), threadUsers, UserAddedToThreadListener.getNewInstance(thread.getEntityID(), thread.getType(), handler));

        threadUsers.addChildEventListener(combo.getListener());
    }

    private void handleMessages(final BThread thread, final FirebasePaths threadRef){
        FirebasePaths messagesRef = threadRef.appendPathComponent(BFirebaseDefines.Path.BMessagesPath);

        // TODO check that it is working
        List<BMessage> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC);

        Date startDate = null;
        Query query = messagesRef;

        // If the message exists we only listen for newer messages
        if (messages.size() > 0)
        {
            startDate = messages.get(0).getDate();//TODO check the zero is the last
            if (DEBUG) Log.d(TAG, "Fetching messages, Starting at: " + startDate.getTime() + ", Msg Text: " + messages.get(0).getText());
        }
        else
        {
            if (DEBUG) Log.d(TAG, "No Messages");
            startDate = new Date((long) (thread.lastMessageAdded().getTime() - BDefines.Time.BDays * 7));
            /*startDate = [thread.lastMessageAdded dateByAddingTimeInterval:-bDays * 7];
            // TODO: Remove this
            startDate = [[NSDate date] dateByAddingTimeInterval:-bHours];*/
        }


        // The plus 1 is needed so we wont receive the last message again.
        query = query.startAt(startDate.getTime() + 1).limit(BDefines.MAX_MESSAGES_TO_PULL);

        FirebaseEventCombo combo = getCombo(MSG_PREFIX + thread.getEntityID(), query, new IncomingMessagesListener(handler));

        query.addChildEventListener(combo.getListener());
    }

    public void handleThread(final BThread thread){

        if (thread == null)
        {
            if (DEBUG) Log.e(TAG, "observeThread, Thread is null");
            return;
        }

        if (DEBUG) Log.v(TAG, "observeThread, ThreadID: " + thread.getEntityID());

        if (thread.getEntityID() == null)
            return;

        if (!isListeningToThread(thread.getEntityID()))
        {
            threadsIds.add(thread.getEntityID());

            FirebasePaths threadRef = FirebasePaths.threadRef(thread.getEntityID());

            // Add an observer to the thread details so we get
            // updated when the thread details change
//            habdleThreadDetails(thread, threadRef);

            // Also listen to the thread users
            // This will allow us to update the users in the database
            handleUsers(thread);

            // Handle incoming messages
//            handleMessages(thread, threadRef);
        }
        else if (DEBUG) Log.e(TAG, "Thread is already handled..");
    }

    public boolean isTagExist(String tag){
        for (Event ae : eventsList)
            if (ae.getTag().equals(tag))
                return true;

        for (MessageEventListener me : messageEventList)
            if (me.getTag().equals(tag))
                return true;

        for (ThreadEventListener te : threadEventList)
            if (te.getTag().equals(tag))
                return true;

        for (UserEventListener ue : userEventList)
            if (ue.getTag().equals(tag))
                return true;

        return false;
    }

    public boolean isListeningToThread(String entityID){
        return threadsIds.contains(entityID);
    }

    private FirebaseEventCombo getCombo(String index, Object ref, FirebaseGeneralEvent listener){
        FirebaseEventCombo combo = FirebaseEventCombo.getNewInstance(listener, ref);
        saveCombo(index, combo);
        return combo;
    }

    private void saveCombo(String index, FirebaseEventCombo combo){
        listenerAndRefs.put(index, combo);
    }
}

