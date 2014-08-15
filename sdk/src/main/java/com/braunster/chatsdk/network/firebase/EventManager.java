package com.braunster.chatsdk.network.firebase;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.AppEventListener;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.network.events.MessageEventListener;
import com.braunster.chatsdk.network.events.ThreadEventListener;
import com.braunster.chatsdk.network.events.UserEventListener;
import com.braunster.chatsdk.network.listeners.IncomingMessagesListener;
import com.braunster.chatsdk.network.listeners.ThreadDetailsChangeListener;
import com.braunster.chatsdk.network.listeners.UserAddedToThreadListener;
import com.braunster.chatsdk.network.listeners.UserDetailsChangeListener;
import com.braunster.chatsdk.object.FirebaseEventCombo;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by braunster on 30/06/14.
 */

public class EventManager implements AppEvents {

    private static final String TAG = EventManager.class.getSimpleName();
    private static final boolean DEBUG = Debug.EventManager;

    public static final String THREAD_ID = "threadID";
    public static final String USER_ID = "userID";


    private static EventManager instance;

    private static final String MSG_PREFIX = "msg_";
    private static final String USER_PREFIX = "user_";

    private List<Event> eventsList = new ArrayList<Event>();
    private List<UserEventListener> userEventList = new ArrayList<UserEventListener>();
    private List<MessageEventListener> messageEventList = new ArrayList<MessageEventListener>();
    private List<ThreadEventListener> threadEventList = new ArrayList<ThreadEventListener>();
    private List<String> tags = new ArrayList<String>();
    public List<String> threadsIds = new ArrayList<String>();
    public List<String> handledAddedUsersToThreadIDs = new ArrayList<String>();
    public List<String> handledMessagesThreadsID = new ArrayList<String>();
    public List<String> usersIds = new ArrayList<String>();

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
                case AppEvents.USER_DETAILS_CHANGED:
                    onUserDetailsChange((BUser) msg.obj);
                    break;

                case AppEvents.MESSAGE_RECEIVED:
                    onMessageReceived((BMessage) msg.obj);
                    break;

                case AppEvents.THREAD_DETAILS_CHANGED:
                    onThreadDetailsChanged((String) msg.obj);
                    break;

                case AppEvents.USER_ADDED_TO_THREAD:
                    onUserAddedToThread(msg.getData().getString(THREAD_ID), msg.getData().getString(USER_ID));
                    break;
            }
        }
    } ;

    @Override
    public boolean onUserAddedToThread(String threadId, final String userId) {
        post(new Runnable() {
            @Override
            public void run() {
                handleUsersDetailsChange(userId);
            }
        });

        // TODO add option to listen to specific thread and from specific type.
        for (ThreadEventListener te : threadEventList)
            te.onUserAddedToThread(threadId, userId);

        for (Event ae : eventsList)
            ae.onUserAddedToThread(threadId, userId);
        return false;
    }

    @Override
    public boolean onUserDetailsChange(BUser user) {
        for (UserEventListener ue : userEventList)
        {
            // We check to see if the listener specified a specific user that he wants to listen to.
            // If we could find and match the data we ignore it.
            if (!ue.getEntityId().equals(""))
                if (user.getEntityID() != null && !ue.getEntityId().equals("") && user.getEntityID().equals(ue.getEntityId()))
                    return false;

            ue.onUserDetailsChange(user);
        }

        for (Event ae : eventsList)
            ae.onUserDetailsChange(user);


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
                        continue;

            me.onMessageReceived(message);
        }

        for (AppEvents ae : eventsList)
            ae.onMessageReceived(message);

        return false;
    }

    @Override
    public boolean onThreadDetailsChanged(final String threadId) {
        post(new Runnable() {
            @Override
            public void run() {
                // Also listen to the thread users
                // This will allow us to update the users in the database
                handleUsersAddedToThread(threadId);
//               Handle incoming messages
                handleMessages(threadId);
            }
        });

        for (Event ae : eventsList)
            ae.onThreadDetailsChanged(threadId);

        // TODO add option to listen to specific thread and from specific type.
        for (ThreadEventListener te : threadEventList)
            te.onThreadDetailsChanged(threadId);

        return false;
    }

    @Override
    public boolean onThreadIsAdded(String threadId) {
        for (Event ae : eventsList)
            ae.onThreadIsAdded(threadId);

        return false;
    }

    /*##########################################################################################*/
    /*------Assigning app events. ------*/

    public void addEventIfNotExist(Event event){
        if (DEBUG) Log.v(TAG, "addEventIfNotExist, Tag: " + event.getTag());

         if (isEventTagExist(event.getTag()))
             return;

        if (event instanceof ThreadEventListener)
            threadEventList.add((ThreadEventListener) event);

        else if (event instanceof MessageEventListener)
            messageEventList.add((MessageEventListener) event);

        else if (event instanceof UserEventListener)
            userEventList.add((UserEventListener) event);

        else eventsList.add(event);

        if (DEBUG) Log.d(TAG, "addEventIfNotExist, Added.");
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

    /** Removes an app event by tag.*/
    public boolean removeEventByTag(String tag){

        if (DEBUG) Log.v(TAG, "removeEventByTag, Tag: " + tag);

        tags.remove(tag);

        for (Event ae : eventsList)
            if (ae.getTag().equals(tag))
            {
                eventsList.remove(ae);
                return true;
            }

        for (MessageEventListener me : messageEventList)
            if (me.getTag().equals(tag))
            {
                messageEventList.remove(me);
                return true;
            }

        for (ThreadEventListener te : threadEventList)
            if (te.getTag().equals(tag))
            {
                threadEventList.remove(te);
                return true;
            }

        for (UserEventListener ue : userEventList)
            if (ue.getTag().equals(tag))
            {
                userEventList.remove(ue);
                return true;
            }

        if (DEBUG) Log.d(TAG, "Event was not found.");
        return false;
    }

    /** Check if there is a AppEvent listener with the currnt tag, Could be AppEvent or one of his child(MessageEventListener, ThreadEventListener, UserEventListener).
     * @return true if found.*/
    public boolean isEventTagExist(String tag){
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

    /*##########################################################################################*/
    /*------Assigning listeners to Firebase refs. ------*/
    /**Set listener to thread details change.*/
    private void handleThreadDetails(final String threadId, FirebasePaths threadRef){
        // Add an observer to the thread details so we get
        // updated when the thread details change
        FirebasePaths detailsRef = threadRef.appendPathComponent(BFirebaseDefines.Path.BDetailsPath);

        FirebaseEventCombo combo = getCombo(threadId, detailsRef.toString(), new ThreadDetailsChangeListener(threadId, handler));

        detailsRef.addValueEventListener(combo.getListener());
    }

    /** Set listener to users that are added to thread.*/
    private void handleUsersAddedToThread(final String threadId){
        // Check if handled.
        if (handledAddedUsersToThreadIDs.contains(threadId))
            return;

        handledAddedUsersToThreadIDs.add(threadId);

        // Also listen to the thread users
        // This will allow us to update the users in the database
        Firebase threadUsers = FirebasePaths.threadRef(threadId).child(BFirebaseDefines.Path.BUsersPath);

        UserAddedToThreadListener userAddedToThreadListener= UserAddedToThreadListener.getNewInstance(threadId, handler);

        FirebaseEventCombo combo = getCombo(USER_PREFIX + threadId, threadUsers.toString(), userAddedToThreadListener);

        threadUsers.addChildEventListener(combo.getListener());
    }

    /** Handle user details change.*/
    public void handleUsersDetailsChange(String userID){
        if (DEBUG) Log.v(TAG, "handleUsersDetailsChange, Entered. " + userID);

        if (userID.equals(getCurrentUserId()))
        {
            if (DEBUG) Log.v(TAG, "handleUsersDetailsChange, Current User." + userID);
            return;
        }

        if (usersIds.contains(userID))
        {
            if (DEBUG) Log.v(TAG, "handleUsersDetailsChange, Listening." + userID);
            return;
        }

        usersIds.add(userID);

        final FirebasePaths userRef = FirebasePaths.userRef(userID);

        if (DEBUG) Log.v(TAG, "handleUsersDetailsChange, User Ref." + userRef.getRef().toString());

        UserDetailsChangeListener userDetailsChangeListener = new UserDetailsChangeListener(userID, handler);

        FirebaseEventCombo combo = getCombo(USER_PREFIX + userID, userRef.toString(), userDetailsChangeListener);

        userRef.addValueEventListener(combo.getListener());
    }

    /** Handle incoming messages for thread.*/
    private void handleMessages(String threadId){
        // Check if handled.
        if (handledMessagesThreadsID.contains(threadId))
            return;

        handledMessagesThreadsID.add(threadId);

        FirebasePaths threadRef = FirebasePaths.threadRef(threadId);
        Query messagesQuery = threadRef.appendPathComponent(BFirebaseDefines.Path.BMessagesPath);

        BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadId);

        List<BMessage> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC);

        Date startDate = null;

        // If the message exists we only listen for newer messages
        if (messages.size() > 0)
        {
            startDate = messages.get(0).getDate();
            if (DEBUG) Log.d(TAG, "Fetching messages, Starting at: " + startDate.getTime() + ", Msg Text: " + messages.get(0).getText());

            // The plus 1 is needed so we wont receive the last message again.
            messagesQuery = messagesQuery.startAt(startDate.getTime() + 1).limit(BDefines.MAX_MESSAGES_TO_PULL);
        }
        else
        {
            if (DEBUG) Log.d(TAG, "No Messages");
            startDate = new Date((long) (thread.lastMessageAdded().getTime() - BDefines.Time.BDays * 7));


            // The plus 1 is needed so we wont receive the last message again.
            messagesQuery = messagesQuery.limit(BDefines.MAX_MESSAGES_TO_PULL);

            /*startDate = [thread.lastMessageAdded dateByAddingTimeInterval:-bDays * 7];
            // TODO: Remove this
            startDate = [[NSDate date] dateByAddingTimeInterval:-bHours];*/
        }

        IncomingMessagesListener incomingMessagesListener = new IncomingMessagesListener(handler);
        FirebaseEventCombo combo = getCombo(MSG_PREFIX + thread.getEntityID(), messagesQuery.getRef().toString(), incomingMessagesListener);

        messagesQuery.addChildEventListener(combo.getListener());
    }

    /** Hnadle the thread by given id, If thread is not handled already a listener
     * to thread details change will be assigned. After details received the messages and added users listeners will be assign.*/
    public void handleThread(final String threadID){

        if (threadID == null)
        {
            if (DEBUG) Log.e(TAG, "observeThread, ThreadId is null, ID: " + threadID);
            return;
        }

        if (DEBUG) Log.v(TAG, "observeThread, ThreadID: " + threadID);


        if (!isListeningToThread(threadID))
        {
            threadsIds.add(threadID);

            final FirebasePaths threadRef = FirebasePaths.threadRef(threadID);

            // Add an observer to the thread details so we get
            // updated when the thread details change
            // When a thread details change a listener for added users is assign to the thread(If not assigned already).
            // For each added user a listener will be assign for his details change.
            post(new Runnable() {
                @Override
                public void run() {
                    handleThreadDetails(threadID, threadRef);
                }
            });

        }
        else if (DEBUG) Log.e(TAG, "Thread is already handled..");
    }

    public void observeUser(BUser user){
        FirebasePaths.userRef(user.getEntityID())
                .appendPathComponent(BFirebaseDefines.Path.BThreadPath)
                .addChildEventListener(threadAddedListener);

        FirebasePaths.publicThreadsRef().addChildEventListener(threadAddedListener);

        for (BUser contact : user.getContacts())
            handleUsersDetailsChange(contact.getEntityID());
    }

    private ChildEventListener threadAddedListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot snapshot, String s) {
            if (DEBUG) Log.i(TAG, "Thread is added. SnapShot Ref: " + snapshot.getRef().toString());

            String threadFirebaseID;
            BPath path = BPath.pathWithPath(snapshot.getRef().toString());
            if (path.isEqualToComponent(BFirebaseDefines.Path.BPublicThreadPath))
                threadFirebaseID = path.idForIndex(0);
            else threadFirebaseID = path.idForIndex(1);

            if (DEBUG) Log.i(TAG, "Thread is added, Thread EntityID: " + threadFirebaseID);

            if (!isListeningToThread(threadFirebaseID))
            {
                // Load the thread from firebase only if he is not exist.
                // There is no reason to load if exist because the event manager will collect all the thread data.
                if (threadFirebaseID != null && DaoCore.fetchEntityWithProperty(BThread.class, BThreadDao.Properties.EntityID, threadFirebaseID) == null)
                    BFirebaseInterface.objectFromSnapshot(snapshot);

                handleThread(threadFirebaseID);

                onThreadIsAdded(threadFirebaseID);
            }
        }

        //region Not used.
        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError error) {

        }
        //endregion
    };

    /** Check to see if the given thread id is already handled by this class.
     * @return true if handled.*/
    public boolean isListeningToThread(String entityID){
        return threadsIds.contains(entityID);
    }

    public boolean isListeningToIcomingMessages(String entityID){
        return handledMessagesThreadsID.contains(entityID);
    }

    public boolean isListeningToUserDetailsChanged(String entityID){
        return usersIds.contains(entityID);
    }

    /** Remove listeners from thread id. The listener's are The thread details, messages and added users.*/
    public void stopListeningToThread(String threadID){
        if (DEBUG) Log.v(TAG, "stopListeningToThread, ThreadID: "  + threadID);

        if (listenerAndRefs.containsKey(threadID))
            listenerAndRefs.get(threadID).breakCombo();

        if (listenerAndRefs.containsKey(MSG_PREFIX + threadID))
            listenerAndRefs.get(MSG_PREFIX  + threadID).breakCombo();

        if (listenerAndRefs.containsKey(USER_PREFIX + threadID))
            listenerAndRefs.get(USER_PREFIX  + threadID).breakCombo();

        // Removing the combo's from the Map.
        listenerAndRefs.remove(threadID);
        listenerAndRefs.remove(MSG_PREFIX  + threadID);
        listenerAndRefs.remove(USER_PREFIX  + threadID);
    }

    /** Get a combo object for given index, ref and listener.
     *  The combo is used to keep the firebase ref path and their listeners so we can remove the listener when needed.*/
    private FirebaseEventCombo getCombo(String index, String ref, FirebaseGeneralEvent listener){
        FirebaseEventCombo combo = FirebaseEventCombo.getNewInstance(listener, ref);
        saveCombo(index, combo);
        return combo;
    }

    /** Save the combo to the combo map.*/
    private void saveCombo(String index, FirebaseEventCombo combo){
        listenerAndRefs.put(index, combo);
    }

    /*##########################################################################################*/
    /*------Clearing all the data from class. ------*/

    /** Remove all firebase listeners and all app events listeners. After removing all class list will be cleared.*/
    public void removeAll(){

        FirebasePaths.userRef(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID())
                .appendPathComponent(BFirebaseDefines.Path.BThreadPath)
                .removeEventListener(threadAddedListener);

        FirebasePaths.publicThreadsRef().removeEventListener(threadAddedListener);

        Set<String> Keys = listenerAndRefs.keySet();

        FirebaseEventCombo combo;

        for (String key : Keys)
        {
            if (DEBUG) Log.d(TAG, "Removing listener, Key: " + key);

            combo = listenerAndRefs.get(key);
            combo.breakCombo();
        }

        clearLists();
    }

    /** Clearing all the lists.*/
    private void clearLists(){
        listenerAndRefs.clear();

        eventsList.clear();
        userEventList.clear();
        messageEventList.clear();
        threadEventList.clear();

        tags.clear();
        threadsIds.clear();
        usersIds.clear();
        handledMessagesThreadsID.clear();
        handledAddedUsersToThreadIDs.clear();
    }

    /*##########################################################################################*/

    /** get the current user entity so we know not to listen to his details and so on.*/
    public static String getCurrentUserId() {
        return BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID();
    }

    /** Print save data of this class. Id's List and listener and refs. Used for debugging.*/
    public void printDataReport(){
        for (String s : threadsIds)
            Log.i(TAG, "Listening to thread ID: "  + s);

        for (String u: usersIds)
            Log.i(TAG, "handled users details, user ID: "  + u);

        for (Event e : messageEventList)
            Log.i(TAG, "Msg Event, Tag: " + e.getTag());

        for (String s : handledAddedUsersToThreadIDs)
            Log.i(TAG, "handled added users, Thread ID: " + s);

        for (String s : handledMessagesThreadsID)
            Log.i(TAG, "handled messages, Thread ID: " + s);
    }

    private void post(Runnable runnable){
        handler.postDelayed(runnable, 0);
    }

    public static class Executor {

        // Sets the amount of time an idle thread waits before terminating
        private static final int KEEP_ALIVE_TIME = 1;
        // Sets the Time Unit to seconds
        private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        private LinkedBlockingQueue<Runnable>  workQueue = new LinkedBlockingQueue<Runnable>();
        /*
         * Gets the number of available cores
         * (not always the same as the maximum number of cores)
         */
        private static int NUMBER_OF_CORES =
                Runtime.getRuntime().availableProcessors();

        private ThreadPoolExecutor threadPool;

        private static Executor instance;

        public static Executor getInstance() {
            if (instance == null)
                instance = new Executor();
            return instance;
        }

        private Executor(){
            // Creates a thread pool manager
            threadPool = new ThreadPoolExecutor(
                    NUMBER_OF_CORES,       // Initial pool size
                    NUMBER_OF_CORES,       // Max pool size
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    workQueue);
        }

        public void execute(Runnable runnable){
            threadPool.execute(runnable);
        }
    }
}

