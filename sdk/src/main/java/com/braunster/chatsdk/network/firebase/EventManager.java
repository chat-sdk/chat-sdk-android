package com.braunster.chatsdk.network.firebase;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BFollower;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
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

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    private ConcurrentHashMap<String, Event> events = new ConcurrentHashMap<String, Event>();

    public List<String> threadsIds = new ArrayList<String>();
    public List<String> handledAddedUsersToThreadIDs = new ArrayList<String>();
    public List<String> handledMessagesThreadsID = new ArrayList<String>();
    public List<String> usersIds = new ArrayList<String>();
    public List<String> handleFollowDataChangeUsersId = new ArrayList<String>();

    public ConcurrentHashMap<String, FirebaseEventCombo> listenerAndRefs = new ConcurrentHashMap<String, FirebaseEventCombo>();

    public static EventManager getInstance(){
        if (instance == null)
            instance = new EventManager();

        return instance;
    }
    private final EventHandler handler = new EventHandler(this);

    private EventManager(){
        threadsIds = Collections.synchronizedList(threadsIds);
        handledAddedUsersToThreadIDs = Collections.synchronizedList(handledAddedUsersToThreadIDs);;
        handledMessagesThreadsID = Collections.synchronizedList(handledMessagesThreadsID);
        usersIds = Collections.synchronizedList(usersIds);
    }

    /*TODO Events first triggered the the specific event listeners saved in the list's.
    *  If one of those events return true then the event wont trigger any AppEventListener. */

    static class EventHandler extends Handler{
        WeakReference<EventManager> manager;

        public EventHandler(EventManager manager){
            super(Looper.getMainLooper());
            this.manager = new WeakReference<EventManager>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case AppEvents.USER_DETAILS_CHANGED:
                    if (notNull())
                        manager.get().onUserDetailsChange((BUser) msg.obj);
                    break;

                case AppEvents.MESSAGE_RECEIVED:
                    if (notNull())
                        manager.get().onMessageReceived((BMessage) msg.obj);
                    break;

                case AppEvents.THREAD_DETAILS_CHANGED:
                    if (notNull())
                        manager.get().onThreadDetailsChanged((String) msg.obj);
                    break;

                case AppEvents.USER_ADDED_TO_THREAD:
                    if (notNull())
                        manager.get().onUserAddedToThread(msg.getData().getString(THREAD_ID), msg.getData().getString(USER_ID));
                    break;

                case AppEvents.FOLLOWER_ADDED:
                    if (notNull())
                        manager.get().onFollowerAdded((BFollower) msg.obj);
                    break;

                case AppEvents.FOLLOWER_REMOVED:
                    if (notNull())
                        manager.get().onFollowerRemoved();
                    break;

                case AppEvents.USER_TO_FOLLOW_ADDED:
                    if (notNull())
                        manager.get().onUserToFollowAdded((BFollower) msg.obj);
                    break;

                case AppEvents.USER_TO_FOLLOW_REMOVED:
                    if (notNull())
                        manager.get().onUserToFollowRemoved();
                    break;
            }
        }

        private boolean notNull(){
            return manager.get() != null;
        }
    }

    @Override
    public boolean onUserAddedToThread(String threadId, final String userId) {
        if (DEBUG) Log.i(TAG, "onUserAddedToThread");
        post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                handleUsersDetailsChange(userId);
            }
        });


        for (Event e : events.values())
        {
            if (e == null)
                continue;

            if (StringUtils.isNotEmpty(e.getEntityId())  && StringUtils.isNotEmpty(threadId) &&  !e.getEntityId().equals(threadId) )
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.ThreadEvent, threadId);

            e.onUserAddedToThread(threadId, userId);
        }

        return false;
    }

    @Override
    public boolean onFollowerAdded(final BFollower follower) {
        post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                handleUsersDetailsChange(follower.getUser().getEntityID());
            }
        });

        if (follower!=null)
            for (Event e : events.values())
            {
                if (e == null)
                    continue;

                if(e instanceof BatchedEvent)
                    ((BatchedEvent) e).add(Event.Type.FollwerEvent, follower.getUser().getEntityID());

                e.onFollowerAdded(follower);
            }
        return false;
    }

    @Override
    public boolean onFollowerRemoved() {
        for (Event e : events.values())
        {
            if (e == null)
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.FollwerEvent);

            e.onFollowerRemoved();
        }
        return false;
    }

    @Override
    public boolean onUserToFollowAdded(final BFollower follower) {
        post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                handleUsersDetailsChange(follower.getUser().getEntityID());
            }
        });

        if (follower!=null)
            for (Event e : events.values())
            {
                if (e == null)
                    continue;

                if(e instanceof BatchedEvent)
                    ((BatchedEvent) e).add(Event.Type.FollwerEvent, follower.getUser().getEntityID());

                e.onUserToFollowAdded(follower);
            }
        return false;
    }

    @Override
    public boolean onUserToFollowRemoved() {
        for (Event e : events.values())
        {
            if (e == null)
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.FollwerEvent);

            e.onUserToFollowRemoved();
        }
        return false;
    }

    @Override
    public boolean onUserDetailsChange(BUser user) {
        if (DEBUG) Log.i(TAG, "onUserDetailsChange");
        if (user == null)
            return false;

        for (Event e : events.values())
        {
            if (e == null)
                continue;

            // We check to see if the listener specified a specific user that he wants to listen to.
            // If we could find and match the data we ignore it.
            if (StringUtils.isNotEmpty(e.getEntityId())  && StringUtils.isNotEmpty(user.getEntityID()) &&  !e.getEntityId().equals(user.getEntityID()) )
                continue;


            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.UserDetailsEvent, user.getEntityID());

            e.onUserDetailsChange(user);
        }

        return false;
    }

    @Override
    public boolean onMessageReceived(BMessage message) {
        if (DEBUG) Log.i(TAG, "onMessageReceived");
        for (Event e : events.values())
        {
            if (e == null)
                continue;

            // We check to see if the listener specified a specific thread that he wants to listen to.
            // If we could find and match the data we ignore it.
            if (StringUtils.isNotEmpty(e.getEntityId()) && message.getBThreadOwner() != null && message.getBThreadOwner().getEntityID() != null
                    && !message.getBThreadOwner().getEntityID().equals(e.getEntityId()))
                    continue;


            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.MessageEvent, message.getEntityID());

            e.onMessageReceived(message);
        }

        return false;
    }

    @Override
    public boolean onThreadDetailsChanged(final String threadId) {
        if (DEBUG) Log.i(TAG, "onThreadDetailsChanged");
        post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                // Also listen to the thread users
                // This will allow us to update the users in the database
                handleUsersAddedToThread(threadId);
//               Handle incoming messages
                handleMessages(threadId);
            }
        });

        for (Event e : events.values())
        {
            if (StringUtils.isNotEmpty(e.getEntityId()) && !threadId.equals(e.getEntityId()))
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.ThreadEvent, threadId);

            e.onThreadDetailsChanged(threadId);
        }

        // TODO add option to listen to specific thread and from specific type.

        return false;
    }

    @Override
    public boolean onThreadIsAdded(String threadId) {
        if (DEBUG) Log.i(TAG, "onThreadIsAdded");
        for (Event e : events.values())
        {
            if (StringUtils.isNotEmpty(e.getEntityId()) && !threadId.equals(e.getEntityId()))
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.ThreadEvent, threadId);

            e.onThreadIsAdded(threadId);
        }

        return false;
    }

    /*##########################################################################################*/
    /*------Assigning app events. ------*/
    public void addAppEvent(Event appEvents){
        events.put(appEvents.getTag(), appEvents);
    }

    public void addThreadEvent(ThreadEventListener threadEvent){
        events.put(threadEvent.getTag(), threadEvent);
    }

    public void addMessageEvent(MessageEventListener messgaeEvent){
        events.put(messgaeEvent.getTag(), messgaeEvent);
    }

    public void addUserEvent(UserEventListener userEvent){
        events.put(userEvent.getTag(), userEvent);
    }

    /** Removes an app event by tag.*/
    public boolean removeEventByTag(String tag){

        if (DEBUG) Log.v(TAG, "removeEventByTag, Tag: " + tag);

        if (StringUtils.isEmpty(tag)){
            return false;
        }

        boolean removed = events.remove(tag) != null;

        if (DEBUG && !removed) Log.d(TAG, "Event was not found.");

        return removed;
    }

    /** Check if there is a AppEvent listener with the currnt tag, Could be AppEvent or one of his child(MessageEventListener, ThreadEventListener, UserEventListener).
     * @return true if found.*/
    public boolean isEventTagExist(String tag){
        return events.containsKey(tag);
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

        UserAddedToThreadListener userAddedToThreadListener= UserAddedToThreadListener.getNewInstance(observedUserEntityID, threadId, handler);

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

    private void handleUserFollowDataChange(String userID){
        if (DEBUG) Log.v(TAG, "handleUserFollowDataChange, Entered. " + userID);


        if (handleFollowDataChangeUsersId.contains(userID))
        {
            if (DEBUG) Log.v(TAG, "handleUserFollowDataChange, Listening." + userID);
            return;
        }

        handleFollowDataChangeUsersId.add(userID);

        final FirebasePaths userRef = FirebasePaths.userRef(userID);


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


        IncomingMessagesListener incomingMessagesListener = new IncomingMessagesListener(handler);

        // If the message exists we only listen for newer messages
        if (messages.size() > 0)
        {
            // The plus 1 is needed so we wont receive the last message again.
            messagesQuery = messagesQuery.startAt(messages.get(0).getDate().getTime() + 1).limit(BDefines.MAX_MESSAGES_TO_PULL);

            // Set any message that received as new.
            incomingMessagesListener.setNew(true);
        }
        else
        {
            if (DEBUG) Log.d(TAG, "No Messages");
            messagesQuery = messagesQuery.limit(BDefines.MAX_MESSAGES_TO_PULL);
        }

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
            handleThreadDetails(threadID, threadRef);
        }
        else if (DEBUG) Log.e(TAG, "Thread is already handled..");
    }

    private String observedUserEntityID = "";
    public void observeUser(final BUser user){

        observedUserEntityID = user.getEntityID();
/*        post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            }
        });*/

        FirebasePaths.userRef(observedUserEntityID)
                .appendPathComponent(BFirebaseDefines.Path.BThreadPath)
                .addChildEventListener(threadAddedListener);

        FirebasePaths.userRef(observedUserEntityID).appendPathComponent(BFirebaseDefines.Path.BFollowers).addChildEventListener(followerEventListener);
        FirebasePaths.userRef(observedUserEntityID).appendPathComponent(BFirebaseDefines.Path.BFollows).addChildEventListener(followsEventListener);

        FirebasePaths.publicThreadsRef().addChildEventListener(threadAddedListener);

        post(new Runnable() {
            @Override
            public void run() {
                for (BUser contact : user.getContacts())
                    handleUsersDetailsChange(contact.getEntityID());
            }
        });

    }

    private ChildEventListener threadAddedListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot snapshot, String s) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.i(TAG, "Thread is added. SnapShot Ref: " + snapshot.getRef().toString());
                    /*android.os.Process.getThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);*/

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
            });
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

    private ChildEventListener followerEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot snapshot, String s) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.i(TAG, "Follower is added. SnapShot Ref: " + snapshot.getRef().toString());
                    BFollower follower = (BFollower) BFirebaseInterface.objectFromSnapshot(snapshot);

                    handleUsersDetailsChange(follower.getUser().getEntityID());
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            if (DEBUG) Log.i(TAG, "Follower is removed. SnapShot Ref: " + snapshot.getRef().toString());
            BFollower follower = (BFollower) BFirebaseInterface.objectFromSnapshot(snapshot);
            DaoCore.deleteEntity(follower);
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    private ChildEventListener followsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot snapshot, String s) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.i(TAG, "Follower is added. SnapShot Ref: " + snapshot.getRef().toString());
                    BFollower follower = (BFollower) BFirebaseInterface.objectFromSnapshot(snapshot);

                    handleUsersDetailsChange(follower.getUser().getEntityID());
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            if (DEBUG) Log.i(TAG, "Follower is removed. SnapShot Ref: " + snapshot.getRef().toString());
            BFollower follower = (BFollower) BFirebaseInterface.objectFromSnapshot(snapshot);
            if (DEBUG) Log.i(TAG, "Follower is removed. UserID: " + follower.getUser().getEntityID() + ", OwnerID: " + follower.getOwner().getEntityID());
            DaoCore.deleteEntity(follower);
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
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

        FirebasePaths.userRef(observedUserEntityID)
                .appendPathComponent(BFirebaseDefines.Path.BThreadPath)
                .removeEventListener(threadAddedListener);

        observedUserEntityID = "";

        FirebasePaths.publicThreadsRef().removeEventListener(threadAddedListener);

        Set<String> Keys = listenerAndRefs.keySet();

        FirebaseEventCombo combo;

        Iterator<String> iter = Keys.iterator();
        String key;
        while (iter.hasNext())
        {
            key = iter.next();
            if (DEBUG) Log.d(TAG, "Removing listener, Key: " + key);

            combo = listenerAndRefs.get(key);
            combo.breakCombo();
        }

        Executor.getInstance().restart();

        clearLists();
    }

    /** Clearing all the lists.*/
    private void clearLists(){
        listenerAndRefs.clear();

        events.clear();

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

 /*       for (Event e : messageEventList)
            Log.i(TAG, "Msg Event, Tag: " + e.getTag());*/

        for (String s : handledAddedUsersToThreadIDs)
            Log.i(TAG, "handled added users, Thread ID: " + s);

        for (String s : handledMessagesThreadsID)
            Log.i(TAG, "handled messages, Thread ID: " + s);
    }

    private void post(Runnable runnable){
        Executor.getInstance().execute(runnable);
    }

    public static class Executor {
        // Sets the amount of time an idle thread waits before terminating
        private static final int KEEP_ALIVE_TIME = 20;
        // Sets the Time Unit to seconds
        private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        private LinkedBlockingQueue<Runnable>  workQueue = new LinkedBlockingQueue<Runnable>();
        /*
         * Gets the number of available cores
         * (not always the same as the maximum number of cores)
         */
        private static int NUMBER_OF_CORES =
                Runtime.getRuntime().availableProcessors();

        private static int MAX_THREADS = 15;

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
                    MAX_THREADS,       // Max pool size
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    workQueue);
        }

        public void execute(Runnable runnable){
            threadPool.execute(runnable);
        }

        private void restart(){
            threadPool.shutdownNow();
            instance = new Executor();
        }
    }
}

