/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.firebase.FirebasePaths;


import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.defines.Debug;

import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.dao.DaoCore;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebaseReferenceManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class ThreadWrapper  {
    
    public static final boolean DEBUG = Debug.Thread;

    private Thread model;

    public ThreadWrapper(Thread thread){
        this.model = thread;
    }
    
    public ThreadWrapper(String entityId){
        this(StorageManager.shared().fetchOrCreateEntityWithEntityID(Thread.class, entityId));
    }

    public Thread getModel(){
        return model;
    }

    /**
     * Start listening to thread details changes.
     **/
    public Observable<Thread> on() {
        return Observable.create(new ObservableOnSubscribe<Thread>() {
            @Override
            public void subscribe(final ObservableEmitter<Thread> e) throws Exception {

                DatabaseReference detailsRef = FirebasePaths.threadDetailsRef(model.getEntityID());

                if (FirebaseReferenceManager.shared().isOn(detailsRef)) {
                    e.onComplete();
                }

                ValueEventListener listener = detailsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() instanceof Map) {
                            deserialize((Map<String, Object>)dataSnapshot.getValue());
                        }

                        if(NM.readReceipts() != null) {
                            NM.readReceipts().updateReadReceipts(model);
                        }

                        e.onNext(model);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e.onError(databaseError.toException());
                    }
                });

                FirebaseReferenceManager.shared().addRef(detailsRef, listener);

                if(NM.typingIndicator() != null) {
                    NM.typingIndicator().typingOn(model);
                }
            }
        });
    }

    public Completable once () {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                DatabaseReference detailsRef = FirebasePaths.threadDetailsRef(model.getEntityID());

                detailsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() instanceof Map) {
                            deserialize((Map<String, Object>)dataSnapshot.getValue());
                        }

                        if(NM.readReceipts() != null) {
                            NM.readReceipts().updateReadReceipts(model);
                        }

                        e.onComplete();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e.onError(databaseError.toException());
                    }
                });
            }
        });
    }

    /**
     * Stop listening to thread details change
     **/
    public void off(){
        final DatabaseReference ref = FirebasePaths.threadDetailsRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListener(ref);
    }

    /**
     * Start listening to incoming messages.
     **/
    public Observable<Message> messagesOn(){
        return Observable.create(new ObservableOnSubscribe<Message>() {
            @Override
            public void subscribe(final ObservableEmitter<Message> e) throws Exception {

                updateReadReceipts();

                final DatabaseReference ref = FirebasePaths.threadMessagesRef(model.getEntityID());

                if(FirebaseReferenceManager.shared().isOn(ref)) {
                    e.onComplete();
                    return;
                }

                threadDeletedDate().subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long deletedTimestamp) throws Exception {

                        Query query = ref;

                        final List<Message> messages = model.getMessagesWithOrder(DaoCore.ORDER_DESC);

                        Long startTimestamp = null;

                        if(messages.size() > 0) {
                            startTimestamp = messages.get(0).getDate().toDate().getTime() + 1;
                        }

                        if(deletedTimestamp > 0) {
                            startTimestamp = deletedTimestamp;
                        }

                        if(startTimestamp != null) {
                            query = query.startAt(startTimestamp);
                        }

                        query = query.orderByPriority().limitToLast(Defines.MAX_MESSAGES_TO_PULL);

                        ChildEventListener listener = query.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
                            @Override
                            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                                if (hasValue) {

                                    model.setDeleted(false);

                                    MessageWrapper message = new MessageWrapper(snapshot);
                                    boolean newMessage = message.getModel().getDelivered() == Message.Delivered.No;

                                    message.setDelivered(Message.Delivered.Yes);

                                    // Update the thread
                                    DaoCore.updateEntity(model);

                                    // Update the message.
                                    model.addMessage(message.getModel());

                                    DaoCore.updateEntity(message.getModel());

                                    if (newMessage) {

                                        e.onNext(message.getModel());
                                        updateReadReceipts();

                                    }
                                }
                            }
                        }));
                        FirebaseReferenceManager.shared().addRef(ref, listener);
                    }
                });
            }
        });

    }

    /**
     * Stop Lisetenig to incoming messages.
     **/
    public void messagesOff(){

        if (DEBUG) Timber.v("messagesOff");
        DatabaseReference ref = FirebasePaths.threadMessagesRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListener(ref);

    }

    //Note the old listener that was used to process the thread bundle is still in use.
    /**
     * Start listening to users added to this thread.
     **/
    public Observable<User> usersOn() {
        return Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(final ObservableEmitter<User> e) throws Exception {

                final DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());

                if(FirebaseReferenceManager.shared().isOn(ref)) {
                    e.onComplete();
                    return;
                }

                ChildEventListener listener = ref.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
                    @Override
                    public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                        final UserWrapper user = new UserWrapper(snapshot);
                        model.addUser(user.getModel());
                        user.metaOn().subscribe(new Consumer<User>() {
                            @Override
                            public void accept(User user) throws Exception {
                                e.onNext(user);
                            }
                        });
                    }
                }).onChildRemoved(new FirebaseEventListener.Removed() {
                    @Override
                    public void trigger(DataSnapshot snapshot, boolean hasValue) {
                        UserWrapper user = new UserWrapper(snapshot);
                        if (model.hasUser(user.getModel())) {
                            DaoCore.breakUserAndThread(user.getModel(), model);
                            DaoCore.updateEntity(model);
                        }
                        e.onNext(user.getModel());
                    }
                }));

                FirebaseReferenceManager.shared().addRef(ref, listener);
            }
        });
    }

    /**
     * Stop listening to users added to this thread.
     **/
    public void usersOff(){
        DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListener(ref);
    }

    //Note - Maybe should reject when cant find value in the user deleted path.
    /**
     * Get the date when the thread was deleted
     * @return Single On success return the date or -1 if the thread hasn't been deleted
     **/
    private Single<Long> threadDeletedDate() {
        return Single.create(new SingleOnSubscribe<Long>() {
            @Override
            public void subscribe(final SingleEmitter<Long> e) {
                User user = NM.currentUser();

                DatabaseReference currentThreadUser = FirebasePaths.threadRef(model.getEntityID())
                        .child(FirebasePaths.UsersPath)
                        .child(user.getEntityID())
                        .child(Keys.Deleted);;

                currentThreadUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            e.onSuccess((Long) snapshot.getValue());
                        }
                        else {
                            e.onSuccess(Long.valueOf(-1));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        e.onSuccess(null);
                    }
                });
            }
        });

    }

    //Note - Maybe should treat group thread and one on one thread the same
    /**
     * Deleting a thread, CoreThread isn't always actually deleted from the db.
     * We mark the thread as deleted and mark the user in the thread users ref as deleted.
     **/
    public Completable deleteThread() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                if (DEBUG) Timber.v("deleteThread");

                // TODO: Check this
                if (model.typeIs(ThreadType.Public)) {
                    e.onComplete();
                }
                else {
                    List<Message> messages = model.getMessages();

                    for (Message m : messages) {
                        DaoCore.deleteEntity(m);
                    }

                    DaoCore.updateEntity(model);

                    final User currentUser = NM.currentUser();

                    DatabaseReference currentThreadUser = FirebasePaths.threadUsersRef(model.getEntityID())
                            .child(currentUser.getEntityID());

                    if(model.typeIs(ThreadType.Private) && model.getUsers().size() == 2) {
                        HashMap<String, Object> value = new HashMap<>();
                        value.put(Keys.Name, currentUser.getName());
                        value.put(Keys.Deleted, ServerValue.TIMESTAMP);

                        currentThreadUser.setValue(value, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    e.onError(databaseError.toException());
                                }
                                else {
                                    e.onComplete();
                                }
                            }
                        });
                    }
                    else {
                        removeUser(currentUser).doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                e.onError(throwable);
                            }
                        }).subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                e.onComplete();
                            }
                        });
                    }
                }
            }
        });
    }

    public Single<List<Message>> loadMoreMessages(Message fromMessage){
        return loadMoreMessages(fromMessage, Defines.MAX_MESSAGES_TO_PULL);
    }

    public Single<List<Message>> loadMoreMessages(final Message fromMessage, final Integer numberOfMessages){
        return Single.create(new SingleOnSubscribe<List<Message>>() {
            @Override
            public void subscribe(final SingleEmitter<List<Message>> e) throws Exception {

                Date messageDate = fromMessage != null ? fromMessage.getDate().toDate() : new Date();

                // First try to load the messages from the database
                List<Message> list = StorageManager.shared().fetchMessagesForThreadWithID(model.getId(), numberOfMessages + 1, messageDate);

                if(!list.isEmpty()) {
                    e.onSuccess(list);
                }
                else {
                    Date endDate = fromMessage != null ? fromMessage.getDate().toDate() : new Date();

                    DatabaseReference messageRef = FirebasePaths.threadMessagesRef(model.getEntityID());

                    Query query = messageRef.orderByPriority()
                            .endAt(endDate.getTime() - 1)
                            .limitToLast(numberOfMessages + 1);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.getValue() != null)
                            {
                                if (DEBUG) Timber.d("MessagesSnapShot: %s", snapshot.getValue().toString());

                                List<Message> messages = new ArrayList<Message>();

                                MessageWrapper message;
                                for (String key : ((Map<String, Object>) snapshot.getValue()).keySet())
                                {
                                    message = new MessageWrapper(snapshot.child(key));
                                    model.addMessage(message.getModel());

                                    message.setDelivered(Message.Delivered.Yes);

                                    DaoCore.updateEntity(message.getModel());
                                    messages.add(message.getModel());
                                }
                                e.onSuccess(messages);
                            }
                            else
                            {
                                e.onSuccess(new ArrayList<Message>());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            e.onError(firebaseError.toException());
                        }
                    });
                }
            }
        });
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
        if (model.getCreationDate() == null) {
            nestedMap.put(Keys.CreationDate, ServerValue.TIMESTAMP);
        }
        else {
            nestedMap.put(Keys.CreationDate, model.getCreationDate().getTime());
        }

        nestedMap.put(Keys.Name, model.getName());

        // This is the legacy type
        int type = model.typeIs(ThreadType.Public) ? 1 : 0;

        nestedMap.put(Keys.Type, type);
        nestedMap.put(Keys.Type_v4, model.getType());

        nestedMap.put(Keys.CreatorEntityId, this.model.getCreatorEntityId());

        nestedMap.put(Keys.ImageUrl, this.model.getImageUrl());

        value.put(FirebasePaths.DetailsPath, nestedMap);
                
        return value;
    }

    /**
     * Updating thread details from given map
     **/
    @SuppressWarnings("all") // To remove setType warning.
    void deserialize(Map<String, Object> value){

        if (DEBUG) Timber.d("Update from map. Id: %s", model.getEntityID());

        if (value == null) {
            if (DEBUG) Timber.e("CoreThread update from map is null, CoreThread ID: %s", model.getEntityID());
            return;
        }

        if (value.containsKey(Keys.CreationDate))
        {
            if (value.get(Keys.CreationDate) instanceof Long)
            {
                Long data = (Long) value.get(Keys.CreationDate);
                if (data != null && data > 0) {
                    this.model.setCreationDate(new Date(data));
                }
            }
            else if (value.get(Keys.CreationDate) instanceof Double)
            {
                Double data = (Double) value.get(Keys.CreationDate);
                if (data != null && data > 0) {
                    this.model.setCreationDate(new Date(data.longValue()));
                }
            }
        }

        String creatorEntityID = (String) value.get(Keys.CreatorEntityId);
        if (creatorEntityID != null) {
            this.model.setCreatorEntityId(creatorEntityID);
        }

        long type = ThreadType.PrivateGroup;
        // First check to see if the new type value exists
        if(value.containsKey(Keys.Type_v4)) {
            type = (Long) value.get(Keys.Type_v4);
        }
        // Handle the legacy value
        else if (value.containsKey(Keys.Type)) {
            type = ((Long) value.get(Keys.Type));
            type = (type == ThreadType.PrivateV3) ? ThreadType.PrivateGroup : ThreadType.PublicGroup;
        }
        model.setType((int)type);

        if (value.containsKey(Keys.Name) && !value.get(Keys.Name).equals("")) {
            this.model.setName((String) value.get(Keys.Name));
        }

        this.model.setImageURL((String) value.get(Keys.ImageUrl));
        this.model.setCreatorEntityId((String) value.get(Keys.CreatorEntityId));
        
        DaoCore.updateEntity(this.model);
    }

    /**
     * Push the thread to firebase.
     **/
    public Completable push(){
        Completable c = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                DatabaseReference ref = null;

                if (model.getEntityID() != null && model.getEntityID().length() > 0) {
                    ref = FirebasePaths.threadRef(model.getEntityID());
                }
                else
                {
                    ref = FirebasePaths.threadRef().push();
                    model.setEntityID(ref.getKey());
                    DaoCore.updateEntity(model);
                }

                ref.updateChildren(serialize(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            e.onComplete();
                        }
                        else {
                            e.onError(databaseError.toException());
                        }
                    }
                });
            }
        });
        return c;
    }

    /**
     * Removing a user from thread.
     * If the thread is private the thread will be removed from the user thread ref.
     **/
    private Completable removeUser(final User user){
        Completable c = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                DatabaseReference ref = FirebasePaths.firebaseRef();
                final HashMap<String, Object> data = new HashMap<>();
                data.put(FirebasePaths.threadUsersPath(model.getEntityID(), user.getEntityID()).build(), null);

                if(model.typeIs(ThreadType.Private)) {
                    data.put("users/" + user.getEntityID() + "/threads/" + model.getEntityID(), null);
                }
                ref.updateChildren(data, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            e.onComplete();
                        }
                        else {
                            e.onError(databaseError.toException());
                        }
                    }
                });
            }
        });

        c.subscribe();
        return c;
    }

    private void updateReadReceipts() {
        if(NM.readReceipts() != null) {
            NM.readReceipts().updateReadReceipts(model);
        }
    }

}
