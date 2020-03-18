/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
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
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;

import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseReferenceManager;
import co.chatsdk.firebase.utils.Generic;
import io.reactivex.Completable;
import io.reactivex.Observable;
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

            DatabaseReference metaRef = FirebasePaths.threadMetaRef(model.getEntityID());

            if (FirebaseReferenceManager.shared().isOn(metaRef)) {
                e.onComplete();
                return;
            }

            ValueEventListener listener = metaRef.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if (hasValue) {
                    deserialize(snapshot);
                }

                if (!model.isDeleted()) {
                    updateReadReceipts();
                }

                e.onNext(model);
            }));

            FirebaseReferenceManager.shared().addRef(metaRef, listener);

            if(ChatSDK.typingIndicator() != null) {
                ChatSDK.typingIndicator().typingOn(model);
            }
        }).subscribeOn(Schedulers.io());
    }

    public DatabaseReference messagesRef () {
        return FirebasePaths.threadMessagesRef(model.getEntityID());
    }

    public Completable once () {
        return Completable.create(e -> {

            DatabaseReference metaRef = FirebasePaths.threadMetaRef(model.getEntityID());

            metaRef.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    deserialize(snapshot);
                }
                e.onComplete();
            }));

        }).subscribeOn(Schedulers.io());
    }

    /**
     * Stop listening to thread details change
     **/
    public void off() {
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.threadLastMessageRef(model.getEntityID()));
        metaOff();
        if(ChatSDK.typingIndicator() != null) {
            ChatSDK.typingIndicator().typingOff(model);
        }
    }

    public Observable<Message> messageRemovedOn() {
        return Observable.create((ObservableOnSubscribe<Message>) e -> {

            Query query = FirebasePaths.threadMessagesRef(model.getEntityID());

            query = query.orderByChild(Keys.Date);
            query = query.limitToLast(ChatSDK.config().messageDeletionListenerLimit);


            ChildEventListener removedListener = query.addChildEventListener(new FirebaseEventListener().onChildRemoved((snapshot, hasValue) -> {
                if(hasValue) {
                    MessageWrapper message = new MessageWrapper(snapshot);
                    this.model.removeMessage(message.getModel());
//                    updateLastMessage().subscribe(ChatSDK.shared().getCrashReporter());
                    e.onNext(message.getModel());
                }
            }));
            FirebaseReferenceManager.shared().addRef(query, removedListener);
        }).subscribeOn(Schedulers.io());
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
                    String from = snapshot.child(Keys.From).getValue(String.class);
                    if (ChatSDK.blocking() != null && ChatSDK.blocking().isBlocked(from)) {
                        return;
                    }

                    model.setDeleted(false);

                    MessageWrapper message = new MessageWrapper(snapshot);

                    boolean newMessage = message.getModel().getMessageStatus() == MessageSendStatus.None;

                    model.addMessage(message.getModel());

//                    message.getModel().setMessageStatus(MessageSendStatus.Delivered);
//                    ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message.getModel())));

                    // Update the text and thread
                    message.getModel().update();
                    model.update();

                    message.markAsReceived().subscribe(ChatSDK.events());
                    updateReadReceipts(message.getModel());

                    if(ChatSDK.hook() != null) {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put(HookEvent.Message, message.getModel());
                        data.put(HookEvent.IsNew_Boolean, newMessage);
                        ChatSDK.hook().executeHook(HookEvent.MessageReceived, data).subscribe(ChatSDK.events());
                    }

                    // If we remove this, then the thread will update twice for each text.
                    // That can fix a bug if the user's system time is wrong
                    // For encryption, we want to refresh the thread after the message has been successfully delivered
                    // This is because there was a bug whereby the message show show on the thread as "EncryptedMessage" for a second
                    if (newMessage || ChatSDK.encryption() != null) {
                        emitter.onNext(message.getModel());
                    }

                }
            }));
            FirebaseReferenceManager.shared().addRef(messagesRef(), listener);
        })).subscribeOn(Schedulers.io());
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
                HashMap<String, Object> map = snapshot.getValue(Generic.hashMapStringObject());
                if (map != null) {
                    model.setMetaValues(map);
                }
                e.onNext(model);
            })));
        }).subscribeOn(Schedulers.io());
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

        }).subscribeOn(Schedulers.io());
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
            }).onChildChanged((snapshot, s, hasValue) -> {
                Boolean muted = snapshot.child(Keys.Mute).getValue(Boolean.class);
                if (muted != null) {
                    model.setMuted(muted);
                }
            }));

            FirebaseReferenceManager.shared().addRef(ref, listener);
        }).subscribeOn(Schedulers.io());
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

        }).subscribeOn(Schedulers.io());
    }

    //Note - Maybe should treat group thread and one on one thread the same
    /**
     * Deleting a thread, CoreThread isn't always actually deleted from the db.
     * We mark the thread as deleted and mark the user in the thread users ref as deleted.
     **/
    public Completable deleteThread() {
        return new ThreadDeleter(model).execute().subscribeOn(Schedulers.io());
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

//                        message.getModel().setMessageStatus(MessageSendStatus.Delivered);
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
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Converting the thread details to a map object.
     **/
    protected Map<String, Object> serialize() {
        return new HashMap<String, Object>() {{
            put(Keys.CreationDate, ServerValue.TIMESTAMP);
            put(Keys.Name, model.getName());
            put(Keys.Type, model.getType());
            put(Keys.Creator, model.getCreator().getEntityID());
            put(Keys.ImageUrl, model.getImageUrl());
        }};
    }

    /**
     * Updating thread details from given map
     **/
    @SuppressWarnings("all") // To remove setType warning.
    void deserialize(DataSnapshot snapshot){

        if (snapshot.hasChild(Keys.CreationDate)) {
            Long date = snapshot.child(Keys.CreationDate).getValue(Long.class);
            if (date != null && date > 0) {
                model.setCreationDate(new Date(date));
            } else {
                Double date2 = snapshot.child(Keys.CreationDate).getValue(Double.class);
                if (date2 != null && date2 > 0) {
                    model.setCreationDate(new Date(date));
                }
            }
        }
        if (snapshot.hasChild(Keys.Creator)) {
            String creatorEntityID = snapshot.child(Keys.Creator).getValue(String.class);
            model.setCreator(ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, creatorEntityID));
        }

        long type = ThreadType.PrivateGroup;
        if (snapshot.hasChild(Keys.Type)) {
            type = snapshot.child(Keys.Type).getValue(Long.class);
        }
        model.setType((int)type);

        if (snapshot.hasChild(Keys.Name)) {
            String name = snapshot.child(Keys.Name).getValue(String.class);
            if (!name.isEmpty()) {
                model.setName(name, false);
            }
        }

        if (snapshot.hasChild(Keys.ImageUrl)) {
            model.setImageUrl(snapshot.child(Keys.ImageUrl).getValue(String.class), false);
        }

        model.update();
        ChatSDK.events().source().onNext(NetworkEvent.threadDetailsUpdated(model));
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

            DatabaseReference metaRef = FirebasePaths.threadMetaRef(model.getEntityID());

            metaRef.updateChildren(serialize(), (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    FirebaseEntity.pushThreadMetaUpdated(model.getEntityID()).subscribe(ChatSDK.events());
                    e.onComplete();
                }
                else {
                    e.onError(databaseError.toException());
                }
            });

        }).subscribeOn(Schedulers.io());
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
