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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseReferenceManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class ThreadWrapper  {

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

                updateReadReceipts();

                e.onNext(model);
            }));

            FirebaseReferenceManager.shared().addRef(detailsRef, listener);

            if(NM.typingIndicator() != null) {
                NM.typingIndicator().typingOn(model);
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

    // When we remove the listener it seems to remove the general message listener too
    // This would be better implemented with a cloud function

//    public Completable updateLastMessage () {
//        return Completable.create(e -> {
//            DatabaseReference ref = messagesRef();
//            Query queryByDate = ref.orderByChild(Keys.Date).limitToLast(1);
//
//            queryByDate.addChildEventListener(new ChildEventListener() {
//                @Override
//                public void onChildAdded(DataSnapshot snapshot, String s) {
//                    if (snapshot.getValue() != null && snapshot.getKey() != null) {
//                        Message m = StorageManager.shared().fetchOrCreateEntityWithEntityID(Message.class, snapshot.getKey());
//                        HashMap<String, Object> messageData = new MessageWrapper(m).lastMessageData();
//                        pushLastMessage(messageData).subscribe(e::onComplete, e::onError);
//                    }
//                    else {
//                        e.onError(new Throwable("No messages exist in thread"));
//                    }
//                    //ref.removeEventListener(this);
//                }
//
//                @Override
//                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                    ref.removeEventListener(this);
//                    e.onComplete();
//                }
//
//                @Override
//                public void onChildRemoved(DataSnapshot dataSnapshot) {
//                    ref.removeEventListener(this);
//                    e.onComplete();
//                }
//
//                @Override
//                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                    ref.removeEventListener(this);
//                    e.onComplete();
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    e.onComplete();
//                }
//            });
//
//        }).observeOn(Schedulers.single());
//    }

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
        if(NM.typingIndicator() != null) {
            NM.typingIndicator().typingOff(model);
        }
    }

    public Observable<Message> messageRemovedOn() {
        return Observable.create(e -> {
            final DatabaseReference ref = FirebasePaths.threadMessagesRef(model.getEntityID());
            ChildEventListener removedListener = ref.addChildEventListener(new FirebaseEventListener().onChildRemoved((snapshot, hasValue) -> {
                if(hasValue) {
                    MessageWrapper message = new MessageWrapper(snapshot);
                    this.model.removeMessage(message.getModel());
//                    updateLastMessage().subscribe(new CrashReportingCompletableObserver());
                    e.onNext(message.getModel());
                }
            }));
            FirebaseReferenceManager.shared().addRef(ref, removedListener);
        });
    }

    /**
     * Start listening to incoming messages.
     **/
    public Observable<Message> messagesOn() {
        return Observable.create((ObservableOnSubscribe<Message>) e -> {

            updateReadReceipts();

            final DatabaseReference ref = FirebasePaths.threadMessagesRef(model.getEntityID());

//            if(FirebaseReferenceManager.shared().isOn(ref)) {
//                e.onComplete();
//                return;
//            }

            // Add the delete listener


            threadDeletedDate()
                    .subscribeOn(Schedulers.single())
                    .subscribe(deletedTimestamp -> {

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

                        query = query.orderByPriority().limitToLast(ChatSDK.config().maxMessagesToLoad);

                        ChildEventListener listener = query.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {
                            if (hasValue) {

                                Object value = snapshot.getValue();
                                if (value instanceof HashMap) {
                                    HashMap<String, Object> hashValue = (HashMap) snapshot.getValue();
                                    Object userIDObject = hashValue.get(Keys.UserFirebaseId);
                                    if (userIDObject instanceof String) {
                                        String userID = (String) userIDObject;
                                        if (NM.blocking() != null && NM.blocking().isBlocked(userID)) {
                                            return;
                                        }
                                    }
                                }

                                model.setDeleted(false);

                                MessageWrapper message = new MessageWrapper(snapshot);

                                boolean newMessage = message.getModel().getMessageStatus() == MessageSendStatus.None;

                                if(NM.hook() != null) {
                                    HashMap<String, Object> data = new HashMap<>();
                                    data.put(BaseHookHandler.MessageReceived_Message, message);
                                    NM.hook().executeHook(BaseHookHandler.MessageReceived, data);
                                }

                                message.getModel().setMessageStatus(MessageSendStatus.Delivered);

                                model.addMessage(message.getModel());
                                model.setLastMessage(message.getModel());

                                // Update the message and thread
                                message.getModel().update();
                                model.update();

                                if (newMessage) {
                                    e.onNext(message.getModel());
                                }
                                updateReadReceipts();
                            }
                        }));
                        FirebaseReferenceManager.shared().addRef(ref, listener);
                    });
        }).subscribeOn(Schedulers.single());

    }


    /**
     * Stop listening to incoming messages.
     **/
    public void messagesOff() {
        DatabaseReference ref = FirebasePaths.threadMessagesRef(model.getEntityID());
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

            ref.setValue(meta, ((databaseError, databaseReference) -> {
                if (databaseError == null) {
                    e.onComplete();
                }
                else {
                    e.onError(databaseError.toException());
                }
            }));

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
                        NM.core().userOn(user.getModel()).subscribe(() -> e.onNext(user.getModel()), e::onError);

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
            User user = NM.currentUser();

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
        return Completable.create(e -> {

            // TODO: Check this
            if (model.typeIs(ThreadType.Public)) {
                e.onComplete();
            }
            else {
                List<Message> messages = model.getMessages();

                for (Message m : messages) {
                    DaoCore.deleteEntity(m);
                }

                model.update();

                final User currentUser = NM.currentUser();

                DatabaseReference currentThreadUser = FirebasePaths.threadUsersRef(model.getEntityID())
                        .child(currentUser.getEntityID());

                if(model.typeIs(ThreadType.Private) && model.getUsers().size() == 2) {

                    model.setDeleted(true);
                    model.update();

                    HashMap<String, Object> value = new HashMap<>();
                    value.put(Keys.Name, currentUser.getName());
                    value.put(Keys.Deleted, ServerValue.TIMESTAMP);

                    currentThreadUser.setValue(value, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            e.onError(databaseError.toException());
                        }
                        else {
                            e.onComplete();
                        }
                    });
                }
                else {

                    NM.thread().removeUsersFromThread(model, currentUser).subscribe(e::onComplete, e::onError);
                }
            }
        }).subscribeOn(Schedulers.single());
    }

    public Single<List<Message>> loadMoreMessages(final Message fromMessage, final Integer numberOfMessages){
        return Single.create((SingleOnSubscribe<List<Message>>) e -> {

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

                query.addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                    if(hasValue) {
                        List<Message> messages = new ArrayList<Message>();

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
                        e.onSuccess(messages);
                    }
                    else {
                        e.onSuccess(new ArrayList<>());
                    }
                }));
            }
        }).subscribeOn(Schedulers.single());
    }

    /**
     * Converting the thread details to a map object.
     **/
    private Map<String, Object> serialize() {

        Map<String , Object> value = new HashMap<String, Object>();
        Map<String , Object> nestedMap = new HashMap<String, Object>();

        nestedMap.put(Keys.CreationDate, ServerValue.TIMESTAMP);

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

    public Completable pushName () {
        return Completable.create(e -> {
            DatabaseReference ref = FirebasePaths.threadRef(model.getEntityID()).child(FirebasePaths.DetailsPath);
            HashMap<String, Object> map = new HashMap<>();
            map.put(Keys.Name, model.getName());
            ref.updateChildren(map, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    FirebaseEntity.pushThreadDetailsUpdated(model.getEntityID()).subscribe(new CrashReportingCompletableObserver());
                    e.onComplete();
                }
                else {
                    e.onError(databaseError.toException());
                }
            });
        }).subscribeOn(Schedulers.single());
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

        this.model.setImageUrl((String) value.get(Keys.ImageUrl));
        this.model.setCreatorEntityId((String) value.get(Keys.CreatorEntityId));
        
        DaoCore.updateEntity(this.model);
    }

    /**
     * Push the thread to firebase.
     **/
    public Completable push() {
        return Completable.create(e -> {

            DatabaseReference ref = null;

            if (model.getEntityID() != null && model.getEntityID().length() > 0) {
                ref = FirebasePaths.threadRef(model.getEntityID());
            }
            else {
                ref = FirebasePaths.threadRef().push();
                model.setEntityID(ref.getKey());
                DaoCore.updateEntity(model);
            }

            ref.updateChildren(serialize(), (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    FirebaseEntity.pushThreadDetailsUpdated(model.getEntityID()).subscribe(new CrashReportingCompletableObserver());
                    e.onComplete();
                }
                else {
                    e.onError(databaseError.toException());
                }
            });
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
        if(NM.readReceipts() != null) {
            NM.readReceipts().updateReadReceipts(model);
        }
    }

}
