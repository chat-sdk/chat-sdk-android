/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BThreadWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.InMessagesListener;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.ThreadUpdateChangeListener;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.UserAddedListener;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.UserMetaChangeListener;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.FollowerLink;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BThreadEntity;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.BPath;
import com.braunster.chatsdk.network.events.AbstractEventManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

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

import timber.log.Timber;

public class FirebaseEventsManager extends AbstractEventManager implements AppEvents {

    private static final boolean DEBUG = Debug.EventManager;

    public static final String THREAD_ID = "threadID";
    public static final String USER_ID = "userID";


    private static FirebaseEventsManager instance;

    private static final String MSG_PREFIX = "msg_";
    private static final String USER_PREFIX = "user_";
    private static final String USER_META_PREFIX = "user_meta_";

    private ConcurrentHashMap<String, Event> events = new ConcurrentHashMap<String, Event>();

    private List<String> threadsIds = new ArrayList<String>();
    private List<String> handledAddedUsersToThreadIDs = new ArrayList<String>();
    private List<String> handledMessagesThreadsID = new ArrayList<String>();
    private List<String> usersIds = new ArrayList<String>();
    private List<String> handledUsersMetaIds = new ArrayList<String>();
    private List<String> handleFollowDataChangeUsersId = new ArrayList<String>();

    public ConcurrentHashMap<String, FirebaseEventCombo> listenerAndRefs = new ConcurrentHashMap<String, FirebaseEventCombo>();

    public static FirebaseEventsManager getInstance(){
        if (instance == null)
            instance = new FirebaseEventsManager();

        return instance;
    }

    private final EventHandler handlerThread = new EventHandler(this);
    private final EventHandler handlerMessages = new EventHandler(this);
    private final EventHandler handlerUserDetails = new EventHandler(this);
    private final EventHandler handlerUserAdded = new EventHandler(this);

    private String observedUserEntityID = "";
    
    private FirebaseEventsManager(){
        threadsIds = Collections.synchronizedList(threadsIds);
        handledAddedUsersToThreadIDs = Collections.synchronizedList(handledAddedUsersToThreadIDs);;
        handledMessagesThreadsID = Collections.synchronizedList(handledMessagesThreadsID);
        usersIds = Collections.synchronizedList(usersIds);
        handledUsersMetaIds = Collections.synchronizedList(handledUsersMetaIds);
    }

    static class EventHandler extends Handler{
        WeakReference<FirebaseEventsManager> manager;

        public EventHandler(FirebaseEventsManager manager){
            super(Looper.getMainLooper());
            this.manager = new WeakReference<>(manager);
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
                        manager.get().onFollowerAdded((FollowerLink) msg.obj);
                    break;

                case AppEvents.FOLLOWER_REMOVED:
                    if (notNull())
                        manager.get().onFollowerRemoved();
                    break;

                case AppEvents.USER_TO_FOLLOW_ADDED:
                    if (notNull())
                        manager.get().onUserToFollowAdded((FollowerLink) msg.obj);
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
        if (DEBUG) Timber.i("onUserAddedToThread");

        for (Event e : events.values())
        {
            if (e == null)
                continue;

            if (StringUtils.isNotEmpty(e.getEntityId())  && StringUtils.isNotEmpty(threadId)
                    &&  !e.getEntityId().equals(threadId) )
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.ThreadEvent, threadId);

            e.onUserAddedToThread(threadId, userId);
        }

        return false;
    }

    @Override
    public boolean onFollowerAdded(final FollowerLink follower) {

        if (follower!=null)
            for (Event  e : events.values())
            {
                if (e == null)
                    continue;

                if(e instanceof BatchedEvent)
                    ((BatchedEvent) e).add(Event.Type.FollwerEvent, follower.getBUser().getEntityID());

                e.onFollowerAdded(follower);
            }
        return false;
    }

    @Override
    public boolean onFollowerRemoved() {
        for ( Event  e : events.values())
        {
            if (e == null )
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.FollwerEvent);

            e.onFollowerRemoved();
        }
        return false;
    }

    @Override
    public boolean onUserToFollowAdded(final FollowerLink follower) {

        if (follower!=null)
            for (Event e : events.values())
            {
                if (e == null)
                    continue;

                if(e instanceof BatchedEvent)
                    ((BatchedEvent) e).add(Event.Type.FollwerEvent, follower.getBUser().getEntityID());

                e.onUserToFollowAdded(follower);
            }
        return false;
    }

    @Override
    public boolean onUserToFollowRemoved() {
        for (Event  e : events.values())
        {
            if (e == null )
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.FollwerEvent);

            e.onUserToFollowRemoved();
        }
        return false;
    }

    @Override
    public boolean onUserDetailsChange(BUser user) {
        if (DEBUG) Timber.i("onUserDetailsChange");
        if (user == null)
            return false;

        for ( Event e : events.values())
        {
            if (e == null)
                continue;

            // We check to see if the listener specified a specific user that he wants to listen to.
            // If we could find and match the data we ignore it.
            if (StringUtils.isNotEmpty(e.getEntityId())  && StringUtils.isNotEmpty(user.getEntityID())
                    &&  !e.getEntityId().equals(user.getEntityID()) )
                continue;


            if(e instanceof BatchedEvent)
                ((BatchedEvent) e ).add(Event.Type.UserDetailsEvent, user.getEntityID());

            e.onUserDetailsChange(user);
        }

        return false;
    }

    @Override
    public boolean onMessageReceived(BMessage message) {
        if (DEBUG) Timber.i("onMessageReceived");
        for (Event e : events.values())
        {
            if (e == null)
                continue;

            // We check to see if the listener specified a specific thread that he wants to listen to.
            // If we could find and match the data we ignore it.
            if (StringUtils.isNotEmpty(e.getEntityId()) && message.getThread() != null
                    && message.getThread().getEntityID() != null
                    && !message.getThread().getEntityID().equals(e.getEntityId()))
                    continue;


            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.MessageEvent, message.getEntityID());

            e.onMessageReceived(message);
        }

        return false;
    }

    @Override
    public boolean onThreadDetailsChanged(final String threadId) {
        if (DEBUG) Timber.i("onThreadDetailsChanged");

        for (Event e : events.values())
        {
            if (e  == null)
                continue;

            
            if (StringUtils.isNotEmpty(e.getEntityId()) && !threadId.equals(e.getEntityId()))
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.ThreadEvent, threadId);

            e.onThreadDetailsChanged(threadId);
        }

         return false;
    }

    @Override
    public boolean onThreadIsAdded(String threadId) {
        if (DEBUG) Timber.i("onThreadIsAdded");
        for (Event e : events.values())
        {

            if (e == null)
                continue;
            
            if (StringUtils.isNotEmpty(e.getEntityId()) && !threadId.equals(e.getEntityId()))
                continue;

            if(e instanceof BatchedEvent)
                ((BatchedEvent) e).add(Event.Type.ThreadEvent, threadId);

            e.onThreadIsAdded(threadId);
        }

        return false;
    }


    /*##########################################################################################*/

    @Override
    public void userOn(final BUser user){

        if (DEBUG) Timber.v("userOn, EntityID: %s", user.getEntityID());
        
        observedUserEntityID = user.getEntityID();

        DatabaseReference userRef = FirebasePaths.userRef(observedUserEntityID);

        userRef.child(BFirebaseDefines.Path.BThreadPath).addChildEventListener(threadAddedListener);

        userRef.child(BFirebaseDefines.Path.FollowerLinks).addChildEventListener(followerEventListener);
        userRef.child(BFirebaseDefines.Path.BFollows).addChildEventListener(followsEventListener);

        FirebasePaths.publicThreadsRef().addChildEventListener(threadAddedListener);

        post(new Runnable() {
            @Override
            public void run() {
                for (BUser contact : user.getContacts())
                    BUserWrapper.initWithModel(contact).metaOn();
            }
        });

    }

    @Override
    public void userOff(final BUser user){
        if (DEBUG) Timber.v("userOff, EntityID: $s", user.getEntityID());
        
        BThreadWrapper wrapper;
        for (BThread thread : user.getThreads())
        {
            wrapper = new BThreadWrapper(thread);
            
            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }

        post(new Runnable() {
            @Override
            public void run() {
                for (BUser contact : user.getContacts())
                    BUserWrapper.initWithModel(contact).metaOff();
            }
        });

        removeAll();
    }

    /**
     * Handle user meta change.
     **/
    public void userMetaOn(String userID, Deferred<Void, Void, Void> promise){

        if (userID.equals(getCurrentUserId()))
        {
            if (DEBUG) Timber.d("handleUsersDetailsChange, Current User: %s", userID);
            return;
        }

        if (handledUsersMetaIds.contains(userID))
        {
            if (DEBUG) Timber.d("handleUsersDetailsChange, Listening.");
            return;
        }

        handledUsersMetaIds.add(userID);

        final DatabaseReference userRef = FirebasePaths.userMetaRef(userID);

        if (DEBUG) Timber.v("handleUsersDetailsChange, User Ref: %s", userRef.getRef().toString());

        UserMetaChangeListener userMetaChangeListener = new UserMetaChangeListener(userID, promise, handlerUserDetails);

        FirebaseEventCombo combo = getCombo(USER_META_PREFIX  + userID, userRef.toString(), userMetaChangeListener);

        userRef.addValueEventListener(combo.getListener());
    }

    /**
     * Stop handling user meta change.
     **/
    public void userMetaOff(String userID){
        if (DEBUG) Timber.v("userMetaOff, UserId: %s", userID);

        FirebaseEventCombo c = listenerAndRefs.get(USER_META_PREFIX  + userID);
        
        if (c != null)
            c.breakCombo();
        
        listenerAndRefs.remove(USER_META_PREFIX  + userID);

        handledUsersMetaIds.remove(userID);
    }
    
    public void threadUsersAddedOn(String threadId){
        // Check if handled.
        if (handledAddedUsersToThreadIDs.contains(threadId))
            return;

        handledAddedUsersToThreadIDs.add(threadId);

        // Also listen to the thread users
        // This will allow us to update the users in the database
        DatabaseReference threadUsers = FirebasePaths.threadRef(threadId).child(BFirebaseDefines.Path.BUsersPath);

        UserAddedListener userAddedToThreadListener= UserAddedListener.getNewInstance(observedUserEntityID, threadId, handlerUserAdded);

        FirebaseEventCombo combo = getCombo(USER_PREFIX + threadId, threadUsers.toString(), userAddedToThreadListener);

        threadUsers.addChildEventListener(combo.getListener());
    }
    
    public void threadUsersAddedOff(String threadId){
        if (DEBUG) Timber.v("handleUsersDetailsChange, EntityId: %s", threadId);

        FirebaseEventCombo c = listenerAndRefs.get(USER_PREFIX + threadId);

        if (c != null)
            c.breakCombo();

        listenerAndRefs.remove(USER_PREFIX + threadId);
        
        handledAddedUsersToThreadIDs.remove(threadId);
    }
    
    public void messagesOn(String threadId, Deferred<BThread, Void , Void> deferred){
        if (DEBUG) Timber.v("messagesOn, EntityID: %s", threadId);
        
        // Check if handled.
        if (handledMessagesThreadsID.contains(threadId))
            return;

        handledMessagesThreadsID.add(threadId);

        final DatabaseReference threadRef = FirebasePaths.threadRef(threadId);
        Query messagesQuery = threadRef.child(BFirebaseDefines.Path.BMessagesPath);

        final BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadId);

        final List<BMessage> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC);

        final InMessagesListener incomingMessagesListener = new InMessagesListener(handlerMessages, threadId, deferred);

        /**
         * If the thread was deleted or has no message we first check for his deletion date.
         * If has deletion date we listen to message from this day on, Else we will get the last messages.
         *
         *
         * Limiting the messages here can cause a problems,
         * If we reach the limit with new messages the new one wont be triggered and he user wont see them.
         * (He would see his if he kill the chat because they are saved locally).
         *
         * */
        if (thread.isDeleted() || messages.size() == 0)
        {
            if (DEBUG) Timber.v("Thread is Deleted, ID: %s", threadId);
            
            BThreadWrapper wrapper = new BThreadWrapper(thread);
            
            wrapper.threadDeletedDate().done(new DoneCallback<Long>() {
                @Override
                public void onDone(Long aLong) {
                    Query query = threadRef.child(BFirebaseDefines.Path.BMessagesPath);

                    // Not deleted
                    if (aLong==null)
                    {
                        query = query.limitToLast(BDefines.MAX_MESSAGES_TO_PULL);
                    }
                    // Deleted.
                    else
                    {
                        if (DEBUG) Timber.d("Thread Deleted Value: %s", aLong);

                        // The plus 1 is needed so we wont receive the last message again.
                        query = query.startAt(aLong);
                    }

                    FirebaseEventCombo combo = getCombo(MSG_PREFIX + thread.getEntityID(), query.getRef().toString(), incomingMessagesListener);

                    query.addChildEventListener(combo.getListener());
                }
            })
            .fail(new FailCallback<DatabaseError>() {
                @Override
                public void onFail(DatabaseError firebaseError) {
                    // Default behavior if failed.
                    Query query = threadRef.child(BFirebaseDefines.Path.BMessagesPath);
                    query = query.limitToLast(BDefines.MAX_MESSAGES_TO_PULL);

                    FirebaseEventCombo combo = getCombo(MSG_PREFIX + thread.getEntityID(), query.getRef().toString(), incomingMessagesListener);

                    query.addChildEventListener(combo.getListener());
                }
            });

            return;
        }
        else if (messages.size() > 0)
        {
            if (DEBUG) Timber.d("messagesOn, Messages size:  %s, LastMessage: %s", messages.size(), messages.get(0).getText());
            
            // The plus 1 is needed so we wont receive the last message again.
            messagesQuery = messagesQuery.startAt(messages.get(0).getDate().getTime() + 1);

            // Set any message that received as new.
            incomingMessagesListener.setNew(true);
        }
        else
        {
            messagesQuery = messagesQuery.limitToLast(BDefines.MAX_MESSAGES_TO_PULL);
        }

        FirebaseEventCombo combo = getCombo(MSG_PREFIX + thread.getEntityID(), messagesQuery.getRef().toString(), incomingMessagesListener);

        messagesQuery.addChildEventListener(combo.getListener());
    }
    
    public void messagesOff(String threadId){
        if (DEBUG) Timber.v("messagesOff, EntityID: %s", threadId);
        
        if (DEBUG) Timber.v("handleUsersDetailsChange, EntityId: %s", threadId);

        FirebaseEventCombo c = listenerAndRefs.get(MSG_PREFIX + threadId);

        if (c != null)
            c.breakCombo();

        listenerAndRefs.remove(MSG_PREFIX + threadId);
        
        handledMessagesThreadsID.remove(threadId);
    }
    
    public void threadOn(String threadId, Deferred<BThread, Void, Void> deferred){
        if (DEBUG) Timber.v("threadOn, EntityID: %s",threadId);
        if (!isListeningToThread(threadId))
        {
            threadsIds.add(threadId);

            final DatabaseReference threadRef = FirebasePaths.threadRef(threadId);

            // Add an observer to the thread details so we get
            // updated when the thread details change
            DatabaseReference detailsRef = threadRef.child(BFirebaseDefines.Path.BDetailsPath);

            FirebaseEventCombo combo = getCombo(threadId, detailsRef.toString(), new ThreadUpdateChangeListener(threadId, handlerThread, deferred));

            detailsRef.addValueEventListener(combo.getListener());
        }
        else if (DEBUG) Timber.e("Thread is already handled..");

    }
    
    public void threadOff(String threadId){
        if (DEBUG) Timber.v("threadOff, EntityID: %s", threadId);
        
        FirebaseEventCombo c = listenerAndRefs.get(threadId);

        if (c != null)
            c.breakCombo();

        listenerAndRefs.remove(threadId);

        threadsIds.remove(threadId);
    }
    
    
    private ChildEventListener threadAddedListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot snapshot, String s) {
            post(new Runnable() {
                @Override
                public void run() {

                    boolean publicThread =false;
                    String threadFirebaseID;
                    BPath path = BPath.pathWithPath(snapshot.getRef().toString());
                    if (path.isEqualToComponent(BFirebaseDefines.Path.BPublicThreadPath))
                    {
                        threadFirebaseID = path.idForIndex(0);
                        publicThread = true;
                    }
                    else threadFirebaseID = path.idForIndex(1);

                    if (DEBUG) Timber.i("Thread is added, Thread EntityID: %s, Listening: %s", threadFirebaseID, isListeningToThread(threadFirebaseID));


                    if (!isListeningToThread(threadFirebaseID))
                    {
                        BThreadWrapper wrapper = new BThreadWrapper(threadFirebaseID);
                        
                        // Starting to listen to thread changes.
                        wrapper.on();
                        wrapper.messagesOn();
                        wrapper.usersOn();

                        BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel();

                        // Add the current user to the thread if needed. Only if not public.
                        if (!publicThread &&
                                !wrapper.getModel().hasUser(currentUser))
                        {
                            wrapper.addUser(BUserWrapper.initWithModel(currentUser));
                            BThread thread = wrapper.getModel();
                            thread.setType(BThreadEntity.Type.Private);
                            DaoCore.createEntity(thread);
                            DaoCore.connectUserAndThread(currentUser, thread);
                        }
                        
                        // Triggering thread added events.
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
        public void onCancelled(DatabaseError error) {

        }
        //endregion
    };

    private ChildEventListener followerEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot snapshot, String s) {
            post(new Runnable() {
                @Override
                public void run() {
                    FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);

                    onFollowerAdded(follower);
                    BUserWrapper wrapper = BUserWrapper.initWithModel(follower.getBUser());
                    wrapper.once();
                    wrapper.metaOn();
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
            DaoCore.deleteEntity(follower);
            onFollowerRemoved();
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {

        }
    };

    /** Check to see if the given thread id is already handled by this class.
     * @return true if handled.*/
    @Override
    public boolean isListeningToThread(String entityID){
        return threadsIds.contains(entityID);
    }

    
    
    
    
    
    
    
    
    

    private ChildEventListener followsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot snapshot, String s) {
            post(new Runnable() {
                @Override
                public void run() {
                    FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);

                    BUserWrapper wrapper = BUserWrapper.initWithModel(follower.getBUser());
                    wrapper.once();
                    wrapper.metaOn();
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
            DaoCore.deleteEntity(follower);
            onUserToFollowRemoved();
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {

        }
    };

    /** Remove listeners from thread id. The listener's are The thread details, messages and added users.*/
    public void stopListeningToThread(String threadID){
        if (DEBUG) Timber.v("stopListeningToThread, ThreadID: %s", threadID);

        if (listenerAndRefs.containsKey(threadID) && listenerAndRefs.get(threadID) != null)
            listenerAndRefs.get(threadID).breakCombo();

        if (listenerAndRefs.containsKey(MSG_PREFIX + threadID) && listenerAndRefs.get(MSG_PREFIX  + threadID) != null)
            listenerAndRefs.get(MSG_PREFIX  + threadID).breakCombo();

        if (listenerAndRefs.containsKey(USER_PREFIX + threadID) && listenerAndRefs.get(USER_PREFIX + threadID) != null)
            listenerAndRefs.get(USER_PREFIX  + threadID).breakCombo();

        // Removing the combo's from the Map.
        listenerAndRefs.remove(threadID);
        listenerAndRefs.remove(MSG_PREFIX  + threadID);
        listenerAndRefs.remove(USER_PREFIX  + threadID);

        threadsIds.remove(threadID);
        handledMessagesThreadsID.remove(threadID);
        handledAddedUsersToThreadIDs.remove(threadID);
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

    
    
    
    
    
    
    
    
    
    @Override
    public void addEvent(Event appEvents){
        Event e = events.put(appEvents.getTag(), appEvents);
        
        if (e != null )
            e.kill();
    }

    /** Removes an app event by tag.*/
    @Override
    public boolean removeEventByTag(String tag){

        if (DEBUG) Timber.v("removeEventByTag, Tag: %s", tag);

        if (StringUtils.isEmpty(tag)){
            return false;
        }

        Event e = events.remove(tag);
        
        if (e != null)
        {
            if (DEBUG) Timber.i("killing event, Tag: %s", e.getTag());
            e.kill();
        }
        
        return e != null;
    }

    /** Check if there is a AppEvent listener with the currnt tag, Could be AppEvent or one of his child(MessageEventListener, ThreadEventListener, UserEventListener).
     * @return true if found.*/
    @Override
    public boolean isEventTagExist(String tag){
        return events.containsKey(tag);
    }
    
    /** 
     * Remove all firebase listeners and all app events listeners. 
     * After removing all class list will be cleared.
     **/
    public void removeAll(){

        DatabaseReference userRef = FirebasePaths.userRef(observedUserEntityID);

        userRef.child(BFirebaseDefines.Path.BThreadPath).removeEventListener(threadAddedListener);

        userRef.child(BFirebaseDefines.Path.FollowerLinks).removeEventListener(followerEventListener);
        userRef.child(BFirebaseDefines.Path.BFollows).removeEventListener(followsEventListener);

        observedUserEntityID = "";

        FirebasePaths.publicThreadsRef().removeEventListener(threadAddedListener);

        Set<String> Keys = listenerAndRefs.keySet();

        FirebaseEventCombo combo;

        Iterator<String> iter = Keys.iterator();
        String key;
        while (iter.hasNext())
        {
            key = iter.next();
            if (DEBUG) Timber.d("Removing listener, Key: %s",  key);

            combo = listenerAndRefs.get(key);
            
            if (combo != null)
                combo.breakCombo();
        }

        Executor.getInstance().restart();

        clearLists();
    }

    /**
     *Clearing all the lists.
     **/
    private void clearLists(){
        listenerAndRefs.clear();

        // Killing all events
        for (Event e : events.values())
        {
            if (e != null)
                e.kill();
        }
        
        events.clear();

        threadsIds.clear();
        usersIds.clear();
        handledUsersMetaIds.clear();
        handledMessagesThreadsID.clear();
        handledAddedUsersToThreadIDs.clear();
        handleFollowDataChangeUsersId.clear();
    }
    
    /** get the current user entity so we know not to listen to his details and so on.*/
    public static String getCurrentUserId() {
        return BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getEntityID();
    }

    /** Print save data of this class. Id's List and listener and refs. Used for debugging.*/
    public void printDataReport(){
        for (String s : threadsIds)
            Timber.i("Listening to thread ID: "  + s);

        for (String u: usersIds)
            Timber.i("handled users details, user ID: %s", u);

        for (String s : handledAddedUsersToThreadIDs)
            Timber.i("handled added users, Thread ID: %s", s);

        for (String s : handledMessagesThreadsID)
            Timber.i("handled messages, Thread ID: %s", s);
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
            
            if (NUMBER_OF_CORES <= 0)
                NUMBER_OF_CORES = 2;
            
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

        private void restart(){
            threadPool.shutdownNow();
            instance = new Executor();
        }
    }
}

