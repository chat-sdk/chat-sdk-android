/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.BMessageDao;
import co.chatsdk.core.dao.sorter.MessageSorter;
import co.chatsdk.firebase.FirebasePaths;


import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoDefines;
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

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
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
    
    public static final boolean DEBUG = Debug.BThread;

    private BThread model;

    public ThreadWrapper(BThread thread){
        this.model = thread;
    }
    
    public ThreadWrapper(String entityId){
        this(StorageManager.shared().fetchOrCreateEntityWithEntityID(BThread.class, entityId));
    }

    public BThread getModel(){
        return model;
    }

    /**
     * Start listening to thread details changes.
     **/
    public Observable<BThread> on() {
        return Observable.create(new ObservableOnSubscribe<BThread>() {
            @Override
            public void subscribe(final ObservableEmitter<BThread> e) throws Exception {

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
    public Observable<BMessage> messagesOn(){
        return Observable.create(new ObservableOnSubscribe<BMessage>() {
            @Override
            public void subscribe(final ObservableEmitter<BMessage> e) throws Exception {

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

                        final List<BMessage> messages = model.getMessagesWithOrder(DaoCore.ORDER_DESC);

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
                                    boolean newMessage = message.getModel().getDelivered() == BMessage.Delivered.No;

                                    message.setDelivered(BMessage.Delivered.Yes);

                                    // Update the thread
                                    DaoCore.updateEntity(model);

                                    // Update the message.
                                    message.getModel().setThread(model);
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
    public Observable<BUser> usersOn() {
        return Observable.create(new ObservableOnSubscribe<BUser>() {
            @Override
            public void subscribe(final ObservableEmitter<BUser> e) throws Exception {

                final DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());

                if(FirebaseReferenceManager.shared().isOn(ref)) {
                    e.onComplete();
                    return;
                }

                ChildEventListener listener = ref.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
                    @Override
                    public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                        final UserWrapper user = new UserWrapper(snapshot);
                        if (!model.hasUser(user.getModel())) {
                            DaoCore.connectUserAndThread(user.getModel(), model);
                            DaoCore.updateEntity(model);
                        }
                        user.metaOn().subscribe(new Consumer<BUser>() {
                            @Override
                            public void accept(BUser user) throws Exception {
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
                BUser user = NM.currentUser();

                DatabaseReference currentThreadUser = FirebasePaths.threadRef(model.getEntityID())
                        .child(FirebasePaths.UsersPath)
                        .child(user.getEntityID())
                        .child(DaoDefines.Keys.Deleted);;

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
                    List<BMessage> messages = model.getMessages();

                    for (BMessage m : messages) {
                        DaoCore.deleteEntity(m);
                    }

                    DaoCore.updateEntity(model);

                    final BUser currentUser = NM.currentUser();

                    DatabaseReference currentThreadUser = FirebasePaths.threadUsersRef(model.getEntityID())
                            .child(currentUser.getEntityID());

                    if(model.typeIs(ThreadType.Private) && model.getUsers().size() == 2) {
                        HashMap<String, Object> value = new HashMap<>();
                        value.put(DaoDefines.Keys.Name, currentUser.getName());
                        value.put(DaoDefines.Keys.Deleted, ServerValue.TIMESTAMP);

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
                        removeUser(currentUser).doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                e.onComplete();
                            }
                        }).doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                e.onError(throwable);
                            }
                        }).subscribe();
                    }

                }
            }
        });
    }

    public Single<List<BMessage>> loadMoreMessages(BMessage fromMessage){
        return loadMoreMessages(fromMessage, Defines.MAX_MESSAGES_TO_PULL);
    }

    public Single<List<BMessage>> loadMoreMessages(final BMessage fromMessage, final Integer numberOfMessages){
        return Single.create(new SingleOnSubscribe<List<BMessage>>() {
            @Override
            public void subscribe(final SingleEmitter<List<BMessage>> e) throws Exception {

                // First try to load the messages from the local database
                Date messageDate = fromMessage != null ? fromMessage.getDate().toDate() : new Date();

                QueryBuilder<BMessage> qb = DaoCore.daoSession.queryBuilder(BMessage.class);
                qb.where(BMessageDao.Properties.ThreadId.eq(model.getId()));

                // Making sure no null messages infected the sort.
                qb.where(BMessageDao.Properties.Date.isNotNull());

                qb.where(BMessageDao.Properties.Date.lt(messageDate.getTime()));

                qb.limit(numberOfMessages + 1);
                qb.orderDesc(BMessageDao.Properties.Date);

                List<BMessage> list = qb.list();

                if(!list.isEmpty()) {
                    Collections.sort(list, new MessageSorter(DaoCore.ORDER_DESC));
                    e.onSuccess(list);
                    return;
                }
                else {

                    Date endDate;

                    if(fromMessage != null) {
                        endDate = fromMessage.getDate().toDate();
                    }
                    else {
                        endDate = new Date();
                    }

                    DatabaseReference messageRef = FirebasePaths.threadMessagesRef(model.getEntityID());

                    Query query = messageRef.orderByPriority().endAt(endDate.getTime() - 1).limitToLast(numberOfMessages + 1);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.getValue() != null)
                            {
                                if (DEBUG) Timber.d("MessagesSnapShot: %s", snapshot.getValue().toString());

                                List<BMessage> messages = new ArrayList<BMessage>();

                                MessageWrapper message;
                                for (String key : ((Map<String, Object>) snapshot.getValue()).keySet())
                                {
                                    message = new MessageWrapper(snapshot.child(key));
                                    message.getModel().setThread(model);
                                    message.setDelivered(BMessage.Delivered.Yes);

                                    DaoCore.updateEntity(message.getModel());
                                    messages.add(message.getModel());
                                }
                                e.onSuccess(messages);
                            }
                            else
                            {
                                e.onSuccess(new ArrayList<BMessage>());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            e.onError(firebaseError.toException());
                        }
                    });                }
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
            nestedMap.put(DaoDefines.Keys.CreationDate, ServerValue.TIMESTAMP);
        }
        else {
            nestedMap.put(DaoDefines.Keys.CreationDate, model.getCreationDate().getTime());
        }

        nestedMap.put(DaoDefines.Keys.Name, model.getName());

        // This is the legacy type
        int type = model.typeIs(ThreadType.Public) ? 1 : 0;

        nestedMap.put(DaoDefines.Keys.Type, type);
        nestedMap.put(DaoDefines.Keys.Type_v4, model.getType());

        if (model.getLastMessageAdded() != null)
            nestedMap.put(DaoDefines.Keys.LastMessageAdded, model.getLastMessageAdded().getTime());

        nestedMap.put(DaoDefines.Keys.CreatorEntityId, this.model.getCreatorEntityId());

        nestedMap.put(DaoDefines.Keys.ImageUrl, this.model.getImageUrl());

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

        if (value.containsKey(DaoDefines.Keys.CreationDate))
        {
            if (value.get(DaoDefines.Keys.CreationDate) instanceof Long)
            {
                Long data = (Long) value.get(DaoDefines.Keys.CreationDate);
                if (data != null && data > 0) {
                    this.model.setCreationDate(new Date(data));
                }
            }
            else if (value.get(DaoDefines.Keys.CreationDate) instanceof Double)
            {
                Double data = (Double) value.get(DaoDefines.Keys.CreationDate);
                if (data != null && data > 0) {
                    this.model.setCreationDate(new Date(data.longValue()));
                }
            }
        }

        String creatorEntityID = (String) value.get(DaoDefines.Keys.CreatorEntityId);
        if (creatorEntityID != null) {
            this.model.setCreatorEntityId(creatorEntityID);
        }

        long type = ThreadType.PrivateGroup;
        // First check to see if the new type value exists
        if(value.containsKey(DaoDefines.Keys.Type_v4)) {
            type = (Long) value.get(DaoDefines.Keys.Type_v4);
        }
        // Handle the legacy value
        else if (value.containsKey(DaoDefines.Keys.Type)) {
            type = ((Long) value.get(DaoDefines.Keys.Type));
            type = (type == ThreadType.PrivateV3) ? ThreadType.PrivateGroup : ThreadType.PublicGroup;
        }
        model.setType((int)type);

        if (value.containsKey(DaoDefines.Keys.Name) && !value.get(DaoDefines.Keys.Name).equals("")) {
            this.model.setName((String) value.get(DaoDefines.Keys.Name));
        }

        Long lastMessageAdded = 0L;
        Object o = value.get(DaoDefines.Keys.LastMessageAdded);

        if (o instanceof Long) {
            lastMessageAdded = (Long) o;
        }

        else if (o instanceof Double) {
            lastMessageAdded = ((Double) o).longValue();
        }

        if (lastMessageAdded != null && lastMessageAdded > 0)
        {
            Date date = new Date(lastMessageAdded);
            if (this.model.getLastMessageAdded() == null || date.getTime() > this.model.getLastMessageAdded() .getTime()) {
                this.model.setLastMessageAdded(date);
            }
        }

        this.model.setImageURL((String) value.get(DaoDefines.Keys.ImageUrl));
        this.model.setCreatorEntityId((String) value.get(DaoDefines.Keys.CreatorEntityId));
        
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
    private Completable removeUser(final BUser user){
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
