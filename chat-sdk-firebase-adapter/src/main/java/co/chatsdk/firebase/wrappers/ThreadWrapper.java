/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.sorter.MessageSorter;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseReferenceManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ThreadWrapper  {

    private Thread model;

    public ThreadWrapper(Thread thread){
        this.model = thread;
    }
    
    public ThreadWrapper(String entityId){
        this(ChatSDK.db().fetchOrCreateEntityWithEntityID(Thread.class, entityId));
    }

    public Thread getModel(){
        return model;
    }

    /**
     * Start listening to thread details changes.
     **/
    public Observable<Thread> on() {
        return Observable.create((ObservableOnSubscribe<Thread>) e -> {

            DatabaseReference detailsRef = FirebasePaths.threadDetailsRef(model.getEntityID());

            if (FirebaseReferenceManager.shared().isOn(detailsRef)) {
                e.onComplete();
                return;
            }

            ValueEventListener listener = detailsRef.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if (hasValue && snapshot.getValue() instanceof Map) {
                    deserialize((Map<String, Object>)snapshot.getValue());
                }

                if (!model.isDeleted()) {
                    updateReadReceipts();
                }

                e.onNext(model);
            }));

            FirebaseReferenceManager.shared().addRef(detailsRef, listener);

            if(ChatSDK.typingIndicator() != null) {
                ChatSDK.typingIndicator().typingOn(model);
            }
        }).subscribeOn(Schedulers.single());
    }

    public Observable<Thread> lastMessageOn () {
        return Observable.create((ObservableOnSubscribe<Thread>) e -> {

            DatabaseReference ref = FirebasePaths.threadLastMessageRef(model.getEntityID());

            if (FirebaseReferenceManager.shared().isOn(ref)) {
                e.onComplete();
                return;
            }

            ValueEventListener listener = ref.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                // We just update the thread. The last message will already have been
                // set by the message listener
                if (hasValue) {
                    e.onNext(model);
                }
            }));

            FirebaseReferenceManager.shared().addRef(ref, listener);

        }).subscribeOn(Schedulers.single());
    }

    public DatabaseReference messagesRef () {
        return FirebasePaths.threadMessagesRef(model.getEntityID());
    }

    public Completable once () {
        return Completable.create(e -> {

            DatabaseReference detailsRef = FirebasePaths.threadDetailsRef(model.getEntityID());

            detailsRef.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue &&  snapshot.getValue() instanceof Map) {
                    deserialize((Map<String, Object>) snapshot.getValue());
                }
                e.onComplete();
            }));

        }).subscribeOn(Schedulers.single());
    }

    /**
     * Stop listening to thread details change
     **/
    public void off() {
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.threadDetailsRef(model.getEntityID()));
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.threadLastMessageRef(model.getEntityID()));
        metaOff();
        if(ChatSDK.typingIndicator() != null) {
            ChatSDK.typingIndicator().typingOff(model);
        }
    }

    public Observable<Message> messageRemovedOn() {
        return Observable.create(e -> {

            Query query = FirebasePaths.threadMessagesRef(model.getEntityID());

            query = query.orderByChild(Keys.Date);
            query = query.limitToLast(ChatSDK.config().messageDeletionListenerLimit);


            ChildEventListener removedListener = query.addChildEventListener(new FirebaseEventListener().onChildRemoved((snapshot, hasValue) -> {
                if(hasValue) {
                    MessageWrapper message = new MessageWrapper(snapshot);
                    this.model.removeMessage(message.getModel());
//                    updateLastMessage().subscribe(new CrashReportingCompletableObserver());
                    e.onNext(message.getModel());
                }
            }));
            FirebaseReferenceManager.shared().addRef(query, removedListener);
        });
    }

    /**
     * Start listening to incoming messages.
     **/
    public Observable<Message> messagesOn() {
        return threadDeletedDate().flatMapObservable((Function<Long, ObservableSource<Message>>) deletedTimestamp -> Observable.create(emitter -> {
            Query query = messagesRef();

            final List<Message> messages = model.getMessagesWithOrder(DaoCore.ORDER_DESC);

            Long startTimestamp = null;

            if(messages.size() > 0) {
                startTimestamp = model.getLastMessageAddedDate().getTime() + 1;
            }

            if(deletedTimestamp > 0) {
                startTimestamp = deletedTimestamp;
                model.setDeleted(true);
            }

            if(startTimestamp != null) {
                query = query.startAt(startTimestamp, Keys.Date);
            }

            query = query.orderByChild(Keys.Date).limitToLast(ChatSDK.config().messageHistoryDownloadLimit);

            ChildEventListener listener = query.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {
                if (hasValue) {

                    Object value = snapshot.getValue();
                    if (value instanceof HashMap) {
                        HashMap<String, Object> hashValue = (HashMap) snapshot.getValue();
                        Object userIDObject = hashValue.get(Keys.UserFirebaseId);
                        if (userIDObject instanceof String) {
                            String userID = (String) userIDObject;
                            if (ChatSDK.blocking() != null && ChatSDK.blocking().isBlocked(userID)) {
                                return;
                            }
                        }
                    }

                    model.setDeleted(false);

                    MessageWrapper message = new MessageWrapper(snapshot);

                    boolean newMessage = message.getModel().getMessageStatus() == MessageSendStatus.None;

                    model.addMessage(message.getModel());
                    message.getModel().setMessageStatus(MessageSendStatus.Delivered);

                    // Update the message and thread
                    message.getModel().update();
                    model.update();


                    if(ChatSDK.hook() != null) {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put(HookEvent.Message, message.getModel());
                        data.put(HookEvent.IsNew_Boolean, newMessage);
                        ChatSDK.hook().executeHook(HookEvent.MessageReceived, data).subscribe(new CrashReportingCompletableObserver());;
                    }

                    // If we remove this, then the thread will update twice for each message.
                    // That can fix a bug if the user's system time is wrong
                    if (newMessage) {
                        emitter.onNext(message.getModel());
                    }

                    message.markAsReceived().subscribe(new CrashReportingCompletableObserver());
                    updateReadReceipts(message.getModel());
                }
            }));
            FirebaseReferenceManager.shared().addRef(messagesRef(), listener);
        })).subscribeOn(Schedulers.single());
    }


    /**
     * Stop listening to incoming messages.
     **/
    public void messagesOff() {
        DatabaseReference ref = messagesRef();
        FirebaseReferenceManager.shared().removeListeners(ref);
    }

    public Observable<Thread> metaOn () {

        return Observable.create((ObservableOnSubscribe<Thread>) e -> {
            DatabaseReference ref = FirebasePaths.threadMetaRef(model.getEntityID());
            FirebaseReferenceManager.shared().addRef(ref, ref.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue &&  snapshot.getValue() instanceof Map) {
                    Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                    for (String key : value.keySet()) {
                        if (value.get(key) instanceof String) {
                            model.setMetaValue(key, (String) value.get(key));
                        }
                    }
                }
                e.onNext(model);
            })));
        }).subscribeOn(Schedulers.single());
    }

    public Completable pushMeta() {
        return Completable.create(e -> {

            DatabaseReference ref = FirebasePaths.threadMetaRef(model.getEntityID());

            HashMap<String, String> meta = new HashMap<>();

            List<ThreadMetaValue> values = model.getMetaValues();
            for(ThreadMetaValue value : values) {
                meta.put(value.getKey(), value.getValue());
            }

            if (meta.keySet().size() > 0) {
                ref.setValue(meta, ((databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        e.onComplete();
                    }
                    else {
                        e.onError(databaseError.toException());
                    }
                }));
            } else {
                e.onComplete();
            }

        }).subscribeOn(Schedulers.single());
    }

    public void metaOff () {
        DatabaseReference ref = FirebasePaths.threadMetaRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListeners(ref);
    }

    //Note the old listener that was used to process the thread bundle is still in use.
    /**
     * Start listening to users added to this thread.
     **/
    public Observable<User> usersOn() {
        return Observable.create((ObservableOnSubscribe<User>) e -> {

            final DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());

            if(FirebaseReferenceManager.shared().isOn(ref)) {
                e.onComplete();
                return;
            }

            ChildEventListener listener = ref.addChildEventListener(new FirebaseEventListener()
                    .onChildAdded((snapshot, s, hasValue) -> {
                        final UserWrapper user = new UserWrapper(snapshot);
                        model.addUser(user.getModel());
                        ChatSDK.core().userOn(user.getModel()).subscribe(() -> e.onNext(user.getModel()), e::onError);

                    }).onChildRemoved((snapshot, hasValue) -> {
                        UserWrapper user = new UserWrapper(snapshot);
                        // We don't call meta off because we may have other therads
                        // with this user
                        model.removeUser(user.getModel());
                        e.onNext(user.getModel());
            }));

            FirebaseReferenceManager.shared().addRef(ref, listener);
        }).subscribeOn(Schedulers.single());
    }

    /**
     * Stop listening to users added to this thread.
     **/
    public void usersOff(){
        DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListeners(ref);
    }

    //Note - Maybe should reject when cant find value in the user deleted path.
    /**
     * Get the date when the thread was deleted
     * @return Single On success return the date or -1 if the thread hasn't been deleted
     **/
    private Single<Long> threadDeletedDate() {
        return Single.create((SingleOnSubscribe<Long>) e -> {
            User user = ChatSDK.currentUser();

            DatabaseReference currentThreadUser = FirebasePaths.threadRef(model.getEntityID())
                    .child(FirebasePaths.UsersPath)
                    .child(user.getEntityID())
                    .child(Keys.Deleted);

            currentThreadUser.addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    e.onSuccess((Long) snapshot.getValue());
                }
                else {
                    e.onSuccess(Long.valueOf(-1));
                }
            }));

        }).subscribeOn(Schedulers.single());
    }

    //Note - Maybe should treat group thread and one on one thread the same
    /**
     * Deleting a thread, CoreThread isn't always actually deleted from the db.
     * We mark the thread as deleted and mark the user in the thread users ref as deleted.
     **/
    public Completable deleteThread() {
        return new ThreadDeleter(model).execute().subscribeOn(Schedulers.single());
    }

    public Single<List<Message>> loadMoreMessages(final Date fromDate, final Integer numberOfMessages){
        return Single.create((SingleOnSubscribe<List<Message>>) e -> {

            DatabaseReference messageRef = FirebasePaths.threadMessagesRef(model.getEntityID());

            Query query = messageRef.orderByChild(Keys.Date).limitToLast(numberOfMessages + 1);

            if (fromDate != null) {
                query = query.endAt(fromDate.getTime() - 1, Keys.Date);
            }

            query.addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    List<Message> messages = new ArrayList<>();

                    MessageWrapper message;
                    for (String key : ((Map<String, Object>) snapshot.getValue()).keySet())
                    {
                        message = new MessageWrapper(snapshot.child(key));
                        model.addMessage(message.getModel());

                        message.getModel().setMessageStatus(MessageSendStatus.Delivered);
                        messages.add(message.getModel());

                        message.getModel().update();
                        model.update();
                    }

                    // Sort the messages
                    Collections.sort(messages, new MessageSorter());

                    e.onSuccess(messages);
                }
                else {
                    e.onSuccess(new ArrayList<>());
                }
            }));
        }).subscribeOn(Schedulers.single());
    }

    /**
     * Converting the thread details to a map object.
     **/
    protected Map<String, Object> serialize() {
        Map<String , Object> map = new HashMap<String, Object>();
        map.put(FirebasePaths.DetailsPath, serializeMeta());
        return map;
    }

    protected Map<String, Object> serializeMeta () {
        Map<String , Object> map = new HashMap<>();

        map.put(Keys.CreationDate, ServerValue.TIMESTAMP);
        map.put(Keys.Name, model.getName());
        // Deprecated in favour of type
        map.put(Keys.Type_v4, model.getType());
        map.put(Keys.Type, model.getType());
        // Deprecated in favour of creator
        map.put(Keys.CreatorEntityId, this.model.getCreatorEntityId());
        map.put(Keys.Creator, this.model.getCreatorEntityId());
        map.put(Keys.ImageUrl, this.model.getImageUrl());

        return map;
    }

    /**
     * Updating thread details from given map
     **/
    @SuppressWarnings("all") // To remove setType warning.
    void deserialize(Map<String, Object> value){

        if (value == null) {
            return;
        }

        if (value.containsKey(Keys.CreationDate))
        {
            if (value.get(Keys.CreationDate) instanceof Long) {
                Long data = (Long) value.get(Keys.CreationDate);
                if (data != null && data > 0) {
                    this.model.setCreationDate(new Date(data));
                }
            }
            else if (value.get(Keys.CreationDate) instanceof Double) {
                Double data = (Double) value.get(Keys.CreationDate);
                if (data != null && data > 0) {
                    this.model.setCreationDate(new Date(data.longValue()));
                }
            }
        }

        String creatorEntityID = (String) value.get(Keys.CreatorEntityId);
        if (creatorEntityID != null) {
            this.model.setCreatorEntityId(creatorEntityID);
            this.model.setCreator(ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, creatorEntityID));
        }

        long type = ThreadType.PrivateGroup;
        // First check to see if the new type value exists
        if(value.containsKey(Keys.Type_v4)) {
            type = (Long) value.get(Keys.Type_v4);
        }
        model.setType((int)type);

        if (value.containsKey(Keys.Name) && !value.get(Keys.Name).equals("")) {
            this.model.setName((String) value.get(Keys.Name));
        }

        this.model.setImageUrl((String) value.get(Keys.ImageUrl));
        this.model.setCreatorEntityId((String) value.get(Keys.CreatorEntityId));
        
        DaoCore.updateEntity(this.model);
    }

    /**
     * Push the thread to firebase.
     **/
    public Completable push() {
        return Completable.create(e -> {

            // If the thread ID is null, create a new random ID
            if (model.getEntityID() == null || model.getEntityID().length() == 0) {
                model.setEntityID(FirebasePaths.threadRef().push().getKey());
                model.update();
            }

            DatabaseReference ref = FirebasePaths.threadRef(model.getEntityID());
            DatabaseReference metaRef = FirebasePaths.threadMetaRef(model.getEntityID());

            ref.updateChildren(serialize(), (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    FirebaseEntity.pushThreadDetailsUpdated(model.getEntityID()).subscribe(new CrashReportingCompletableObserver());
                    e.onComplete();
                }
                else {
                    e.onError(databaseError.toException());
                }
            });

            // Also update the meta ref - we do this for forwards compatibility
            // in the future we will move everything to the meta area
            metaRef.updateChildren(serializeMeta());

        }).subscribeOn(Schedulers.single());
    }

    public Completable pushLastMessage (final HashMap<String, Object> messageData) {
        return Completable.create(e -> FirebasePaths.threadRef(model.getEntityID()).child(FirebasePaths.LastMessagePath).setValue(messageData, (databaseError, databaseReference) -> {
            if(databaseError == null) {
                e.onComplete();
            }
            else {
                e.onError(databaseError.toException());
            }
        })).subscribeOn(Schedulers.single());
    }

    private void updateReadReceipts() {
        if(ChatSDK.readReceipts() != null) {
            ChatSDK.readReceipts().updateReadReceipts(model);
        }
    }

    private void updateReadReceipts(Message message) {
        if(ChatSDK.readReceipts() != null) {
            ChatSDK.readReceipts().updateReadReceipts(message);
        }
    }

}
