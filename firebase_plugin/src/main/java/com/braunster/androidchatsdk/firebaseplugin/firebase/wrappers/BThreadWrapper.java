/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebasePaths;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.sorter.MessageSorter;
import com.braunster.chatsdk.dao.UserThreadLink;
import com.braunster.chatsdk.dao.UserThreadLinkDao;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BThreadEntity;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.QueryBuilder;
import jdeferred.android.AndroidDeferredObject;
import jdeferred.android.AndroidExecutionScope;
import timber.log.Timber;

public class BThreadWrapper extends EntityWrapper<BThread> {
    
    public static final boolean DEBUG = Debug.BThread;
    
    public BThreadWrapper(BThread thread){
        this.model = thread;
        this.entityId = thread.getEntityID();
    }
    
    public BThreadWrapper(String entityId){
        this(DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, entityId));
    }

    @Override
    public BThread getModel(){
        return DaoCore.fetchEntityWithEntityID(BThread.class, entityId);
    }

    /**
     * Start listening to thread details changes.
     **/
    public Promise<BThread, Void , Void>  on(){

        if (DEBUG) Timber.v("on");
        
        Deferred<BThread, Void , Void> deferred = new DeferredObject<>();

        AndroidDeferredObject<BThread, Void, Void> androidDeferredObject = new AndroidDeferredObject<BThread, Void, Void>(deferred.promise(), AndroidExecutionScope.UI);

        getNetworkAdapter().getEventManager().threadOn(entityId, deferred);
        
        return androidDeferredObject.promise();
    }

    /**
     * Stop listening to thread details change
     **/
    public void off(){
        if (DEBUG) Timber.v("off");
        getNetworkAdapter().getEventManager().threadOff(entityId);
    }

    /**
     * Start listening to incoming messages.
     **/
    public Promise<BThread, Void, Void> messagesOn(){

        if (DEBUG) Timber.v("messagesOn");
        Deferred<BThread, Void, Void> deferred = new DeferredObject<>();

        AndroidDeferredObject<BThread, Void, Void> androidDeferredObject = new AndroidDeferredObject<BThread, Void, Void>(deferred.promise(), AndroidExecutionScope.UI);

        getNetworkAdapter().getEventManager().messagesOn(entityId, deferred);
        
        return androidDeferredObject.promise();
    }

    /**
     * Stop Lisetenig to incoming messages.
     **/
    public void messagesOff(){

        if (DEBUG) Timber.v("messagesOff");
        getNetworkAdapter().getEventManager().messagesOff(entityId);
    }

    //Note the old listener that was used to process the thread data is still in use.
    /**
     * Start listening to users added to this thread.
     **/
    public void usersOn(){
        if (DEBUG) Timber.v("usersOn");
        getNetworkAdapter().getEventManager().threadUsersAddedOn(entityId);
    }

    /**
     * Stop listening to users added to this thread.
     **/
    public void usersOff(){

        if (DEBUG) Timber.v("usersOff");
        getNetworkAdapter().getEventManager().threadUsersAddedOff(entityId);
    }

    //Note - Maybe should reject when cant find value in the user deleted path.
    /**
     * Get the date when the thread was deleted
     * @return Promise On success return the date or Nil if the thread hasn't been deleted
     **/
    public Promise<Long, DatabaseError, Void> threadDeletedDate(){
        final Deferred<Long, DatabaseError, Void> deferred = new DeferredObject<>();

        BUser user = getNetworkAdapter().currentUserModel();
        
        DatabaseReference currentThreadUser = FirebasePaths.threadRef(entityId)
                .child(BFirebaseDefines.Path.BUsersPath)
                .child(user.getEntityID())
                .child(BDefines.Keys.BDeleted);;
        
        currentThreadUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                {
                    deferred.resolve((Long) snapshot.getValue());
                }
                else deferred.resolve(null);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                deferred.reject(firebaseError);
            }
        });
        
        return deferred.promise();
    }

    //Note - Maybe should treat group thread and one on one thread the same
    /**
     * Deleting a thread, Thread isn't always actually deleted from the db.
     * We mark the thread as deleted and mark the user in the thread users ref as deleted.
     **/
    public Promise<Void, BError, Void>  deleteThread(){

        if (DEBUG) Timber.v("deleteThread");
        
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();
        
        BUser user = getNetworkAdapter().currentUserModel();

        if (model.getTypeSafely() != BThreadEntity.Type.Private) return deferred.promise();

        List<BMessage> messages = DaoCore.fetchEntitiesWithProperty(BMessage.class, BMessageDao.Properties.ThreadDaoId, model.getId());

        for (BMessage m : messages) {
            DaoCore.deleteEntity(m);
        }

        DaoCore.updateEntity(model);

        if (model.getUsers().size() > 2)
        {
            // Removing the thread from the current user thread ref.
            DatabaseReference userThreadRef = FirebasePaths.firebaseRef()
                .child(user.getBPath().getPath())
                .child(model.getBPath().getPath());

            // Stop listening to thread events, Details change, User added and incoming messages.
            off();
            messagesOff();
            usersOff();

            // Removing the thread from the user threads list.
            userThreadRef.removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference firebase) {
                    // Delete the thread if no error occurred when deleting from firebase.
                    if (error == null)
                    {
                        // Adding a leave value to the user on the thread path so other users will know this user has left.
                        DatabaseReference threadUserRef = FirebasePaths.threadRef(entityId)
                            .child(BFirebaseDefines.Path.BUsersPath)
                            .child(getNetworkAdapter().currentUserModel().getEntityID())
                            .child(BDefines.Keys.BLeaved);

                        threadUserRef.setValue(true);

                        List<UserThreadLink> list =  DaoCore.fetchEntitiesWithProperty(UserThreadLink.class, UserThreadLinkDao.Properties.BThreadDaoId, model.getId());

                        DaoCore.deleteEntity(model);

                        // Deleting all data relevant to the thread from the db.
                        for (UserThreadLink d : list)
                            DaoCore.deleteEntity(d);

                        if (DEBUG)
                        {
                            BThread deletedThread = DaoCore.fetchEntityWithEntityID(BThread.class, entityId);
                            if (deletedThread == null)
                                Timber.d("Thread deleted successfully.");
                            else Timber.d("Thread was not deleted.");
                        }

                        deferred.resolve(null);

                    } else
                    {
                        deferred.reject(getFirebaseError(error));
                    }
                }
            });
        }
        else
        {
            DatabaseReference threadUserRef = FirebasePaths.threadRef(entityId)
                .child(BFirebaseDefines.Path.BUsersPath)
                .child(getNetworkAdapter().currentUserModel().getEntityID())
                .child(BDefines.Keys.BDeleted);

            // Set the deleted value in firebase
            threadUserRef.setValue(ServerValue.TIMESTAMP);

            model.setDeleted(true);

            DaoCore.updateEntity(model);
            DaoCore.deleteEntity(model);

            deferred.resolve(null);
        }
        
        return deferred.promise();
    }

    public Promise<BThread, BError, Void> recoverPrivateThread(){

        if (DEBUG) Timber.v("recoverPrivateThread");
        final Deferred<BThread, BError, Void> deferred = new DeferredObject<>();
        // Removing the deleted value from firebase.
        DatabaseReference threadUserRef = FirebasePaths.threadRef(entityId)
            .child(BFirebaseDefines.Path.BUsersPath)
            .child(BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getEntityID())
            .child(BDefines.Keys.BDeleted);

        threadUserRef.removeValue();

        this.getModel().setDeleted(false);
        this.getModel().setType(BThread.Type.Private);
        final BThread toBeUpdated = this.getModel();
        this.loadMessages().done(new DoneCallback<List<BMessage>>() {
            @Override
            public void onDone(List<BMessage> bMessages) {
                toBeUpdated.setMessages(bMessages);
                DaoCore.updateEntity(toBeUpdated);
                deferred.resolve(toBeUpdated);
            }
        }).fail(new FailCallback<Void>() {
            @Override
            public void onFail(Void aVoid) {
                deferred.resolve(toBeUpdated);
            }
        });
        DaoCore.updateEntity(this.model);
        
        return deferred.promise();
        
    }

    // TODO: Replace this with the loadMessages method (need to figure out the date ref first)
    public Promise<List<BMessage>, Void, Void> loadMoreMessages(final int numberOfMessages){

        if (DEBUG) Timber.v("loadMoreMessages");
        final Deferred<List<BMessage>, Void, Void> deferred = new DeferredObject<>();
        
        final Date messageDate;

        List<BMessage> messages = model.getMessagesWithOrder(DaoCore.ORDER_ASC);
        BMessage earliestMessage = null;
        if (messages.size() > 0)
            earliestMessage = messages.get(0);
            
        // If we have a message in the database then we use the latest
        if (earliestMessage != null)
        {
            if(DEBUG) Timber.d("Msg: %s", earliestMessage.getText());
            messageDate = earliestMessage.getDate();
        }
        // Otherwise we use todays date
        else messageDate = new Date();

        List<BMessage> list ;

        QueryBuilder<BMessage> qb = DaoCore.daoSession.queryBuilder(BMessage.class);
        qb.where(BMessageDao.Properties.ThreadDaoId.eq(model.getId()));

        // Making sure no null messages infected the sort.
        qb.where(BMessageDao.Properties.Date.isNotNull());

        qb.where(BMessageDao.Properties.Date.lt(messageDate));

        qb.limit(numberOfMessages + 1);
        qb.orderDesc(BMessageDao.Properties.Date);

        list = qb.list();

        // If we have older messages in the db we get them
        if (list.size() > 0){
            if (DEBUG) Timber.d("Loading messages from local db, Size: %s", list.size());
            Collections.sort(list, new MessageSorter(DaoCore.ORDER_DESC));

            deferred.resolve(list);
        }
        else
        {
            if (DEBUG) Timber.d("Loading messages from firebase");

            DatabaseReference messageRef = FirebasePaths.threadRef(model.getEntityID()).child(BFirebaseDefines.Path.BMessagesPath);

            // Get # messages ending at the end date
            // Limit to # defined in BFirebaseDefines
            // We add one becase we'll also be returning the last message again
            // FIXME not sure if to use limitToFirst or limitToLast
            Query msgQuery;
            if(numberOfMessages == -1){
                msgQuery = messageRef;
            }else {
                msgQuery = messageRef.endAt(messageDate.getTime()).limitToLast(numberOfMessages + 1);
            }

            msgQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null)
                    {
                        if (DEBUG) Timber.d("MessagesSnapShot: %s", snapshot.getValue().toString());
                        
                        List<BMessage> msgs = new ArrayList<BMessage>();
                        
                        BMessageWrapper msg;
                        for (String key : ((Map<String, Object>) snapshot.getValue()).keySet())
                        {
                            msg = new BMessageWrapper(BThreadWrapper.this.getModel(), snapshot.child(key));
                            
                            DaoCore.updateEntity(msg.model);
                            
                            msgs.add(msg.model);
                        }
                        
                        deferred.resolve(msgs);
                    }
                    else
                    {
                        deferred.reject(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    deferred.reject(null);
                }
            });
        }
        
        return deferred.promise();
    }

    public Promise<List<BMessage>, Void, Void> loadMessages(){
        return loadMessages(-1);
    }

     public Promise<List<BMessage>, Void, Void> loadMessages(Integer lastNMessages){
        final Deferred<List<BMessage>, Void, Void> deferred = new DeferredObject<>();

        DatabaseReference messageRef = FirebasePaths.threadRef(model.getEntityID())
                .child(BFirebaseDefines.Path.BMessagesPath);

        Query msgQuery;
        // If number of messages set to retrieve is -1, retrieve all messages
        if(lastNMessages == -1){
            msgQuery = messageRef;
        }else {
            msgQuery = messageRef.limitToLast(lastNMessages);
        }

        msgQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                {
                    if (DEBUG) Timber.d("MessagesSnapShot: %s", snapshot.getValue().toString());

                    List<BMessage> msgs = new ArrayList<BMessage>();

                    BMessageWrapper msg;
                    for (String key : ((Map<String, Object>) snapshot.getValue()).keySet())
                    {
                        msg = new BMessageWrapper(BThreadWrapper.this.getModel() ,snapshot.child(key));

                        DaoCore.updateEntity(msg.model);

                        msgs.add(msg.model);
                    }
                    deferred.resolve(msgs);
                }
                else
                {
                    deferred.reject(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                deferred.reject(null);
            }
        });

        return deferred.promise();
    }



    /**
     * Converting the thread details to a map object.
     **/
    Map<String, Object> serialize(){

        Map<String , Object> value = new HashMap<String, Object>();
        Map<String , Object> nestedMap = new HashMap<String, Object>();

        // If the creation date is null we assume that the thread is now being created so we push the server timestamp with it.
        // Else we will push the saved creation date from the db.
        // No treating this as so can cause problems with firebase security rules.
        if (this.model.getCreationDate() == null)
            nestedMap.put(BDefines.Keys.BCreationDate, BFirebaseDefines.getServerTimestamp());
        else
            nestedMap.put(BDefines.Keys.BCreationDate, this.model.getCreationDate().getTime());

        nestedMap.put(BDefines.Keys.BName, this.model.getName());
        nestedMap.put(BDefines.Keys.BType, this.model.getType());

        if (this.model.getLastMessageAdded() != null)
            nestedMap.put(BDefines.Keys.BLastMessageAdded, this.model.getLastMessageAdded().getTime());

        nestedMap.put(BDefines.Keys.BCreatorEntityId, this.model.getCreatorEntityId());

        nestedMap.put(BDefines.Keys.BImageUrl, this.model.getImageUrl());

        value.put(BFirebaseDefines.Path.BDetailsPath, nestedMap);
                
        return value;
    }

    /**
     * Updating thread details from given map
     **/
    @SuppressWarnings("all")// To remove setType warning.
    void deserialize(Map<String, Object> value){

        if (DEBUG) Timber.d("Update from map. Id: %s", entityId);

        if (value == null) {
            if (DEBUG) Timber.e("Thread update from map is null, Thread ID: %s", entityId);
            return;
        }

        if (value.containsKey(BDefines.Keys.BCreationDate))
        {
            if (value.get(BDefines.Keys.BCreationDate) instanceof Long)
            {
                Long data = (Long) value.get(BDefines.Keys.BCreationDate);
                if (data != null && data > 0)
                    this.model.setCreationDate(new Date(data));
            }
            else if (value.get(BDefines.Keys.BCreationDate) instanceof Double)
            {
                Double data = (Double) value.get(BDefines.Keys.BCreationDate);
                if (data != null && data > 0)
                    this.model.setCreationDate(new Date(data.longValue()));
            }
        }

        long type;
        if (value.containsKey(BDefines.Keys.BType))
        {
            type = (Long) value.get(BDefines.Keys.BType);
            this.model.setType((int) type);
            if (DEBUG) Timber.d("Setting type to: %s, Id: %s", this.model.getType(), entityId);
        }

        if (value.containsKey(BDefines.Keys.BName) && !value.get(BDefines.Keys.BName).equals(""))
            this.model.setName((String) value.get(BDefines.Keys.BName));

        Long lastMessageAdded = 0L;
        Object o = value.get(BDefines.Keys.BLastMessageAdded);

        if (o instanceof Long)
            lastMessageAdded = (Long) o;

        else if (o instanceof Double)
            lastMessageAdded = ((Double) o).longValue();

        if (lastMessageAdded != null && lastMessageAdded > 0)
        {
            Date date = new Date(lastMessageAdded);
            if (this.model.getLastMessageAdded() == null || date.getTime() > this.model.getLastMessageAdded() .getTime())
                this.model.setLastMessageAdded(date);
        }

        this.model.setImageUrl((String) value.get(BDefines.Keys.BImageUrl));
        this.model.setCreatorEntityId((String) value.get(BDefines.Keys.BCreatorEntityId));
        
        DaoCore.updateEntity(this.model);
    }

    /**
     * Push the thread to firebase.
     **/
    public Promise<BThread, BError, Void> push(){

        if (DEBUG) Timber.v("push");
        
        final DeferredObject<BThread, BError, Void> deferred = new DeferredObject<>();
        
        DatabaseReference ref = null;
        if (StringUtils.isNotEmpty(model.getEntityID()))
        {
            ref = FirebasePaths.threadRef(model.getEntityID());
        }
        else
        {
            // Creating a new entry for this thread.
            ref = FirebasePaths.threadRef().push();
            model.setEntityID(ref.getKey());
            
            // Updating the database.
            DaoCore.updateEntity(model);
        }
        
        
        ref.updateChildren(serialize(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null)
                {
                    deferred.reject(getFirebaseError(firebaseError));
                }
                else deferred.resolve(model);
            }
        });
        
        return deferred.promise();
    }

    /**
     * Add the thread from the given user threads ref.
     **/
    public Promise<BThread, BError, Void> addUserWithEntityID(String entityId){

        final Deferred<BThread, BError, Void>  deferred = new DeferredObject<>();
        
        DatabaseReference ref = FirebasePaths.threadRef(this.entityId)
                .child(BFirebaseDefines.Path.BUsersPath)
                .child(entityId);

        BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);
        
        Map<String, Object> values = new HashMap<String, Object>();

        // If metaname is null the data wont be saved so we have to do so.
        values.put(BDefines.Keys.BName, (user.getMetaName() == null ? "no_name" : user.getMetaName()));
        
        ref.setValue(values, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError == null)
                    deferred.resolve(BThreadWrapper.this.model);
                else
                    deferred.reject(getFirebaseError(firebaseError));
            }
        });
        
        return deferred.promise();
    }

    /**
     *Remove the thread from the given user threads ref.
     **/
    public Promise<BThread, BError, Void> removeUserWithEntityID(String entityId){

        final Deferred<BThread, BError, Void>  deferred = new DeferredObject<>();

        BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);

        DatabaseReference ref = FirebasePaths.threadRef(this.entityId).child(BFirebaseDefines.Path.BUsersPath).child(entityId);

        ref.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError == null)
                    deferred.resolve(BThreadWrapper.this.model);
                else
                    deferred.reject(getFirebaseError(firebaseError));
            }
        });
        
        return deferred.promise();
    }

    /**
     * Removing a user from thread.
     * If the thread is private the thread will be removed from the user thread ref.
     **/
    public Promise<BThread, BError, Void> removeUser(final BUserWrapper user){
        final Deferred<BThread, BError, Void>  deferred = new DeferredObject<>();

        removeUserWithEntityID(user.entityId).done(new DoneCallback<BThread>() {
            @Override
            public void onDone(BThread bThreadWrapper) {

                if (model.getType() == BThread.Type.Private) {
                    removeUserWithEntityID(user.entityId).done(new DoneCallback<BThread>() {
                        @Override
                        public void onDone(BThread thread) {
                            deferred.resolve(thread);
                        }
                    }).fail(new FailCallback<BError>() {
                        @Override
                        public void onFail(BError error) {
                            deferred.reject(error);
                        }
                    });
                } else deferred.resolve(BThreadWrapper.this.model);
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError error) {
                deferred.reject(error);
            }
        });


        return deferred.promise();
    }

    /**
     * Adding a user to the thread. 
     * If the thread is private the thread will be added to the user thread ref.
     **/
    public Promise<BThread, BError, Void> addUser(final BUserWrapper user){
        final Deferred<BThread, BError, Void>  deferred = new DeferredObject<>();

        // Adding the user.
        addUserWithEntityID(user.entityId).done(new DoneCallback<BThread>() {
            @Override
            public void onDone(BThread bThreadWrapper) {

                // If the thread is private we are adding the thread to the user.
                if (model.getTypeSafely() == BThread.Type.Private) {
                    user.addThreadWithEntityId(model.getEntityID()).done(new DoneCallback<BUserWrapper>() {
                        @Override
                        public void onDone(BUserWrapper bUserWrapper) {
                            deferred.resolve(BThreadWrapper.this.model);
                        }
                    }).fail(new FailCallback<DatabaseError>() {
                        @Override
                        public void onFail(DatabaseError firebaseError) {
                            deferred.reject(getFirebaseError(firebaseError));
                        }
                    });
                } else deferred.resolve(null);
            }
        })
        .fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError error) {
                deferred.reject(error);
            }
        });
        
        return deferred.promise();
    }
    
    
}
