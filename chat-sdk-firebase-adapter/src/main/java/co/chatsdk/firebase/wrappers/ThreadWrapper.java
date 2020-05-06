/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import co.chatsdk.firebase.R;
import co.chatsdk.firebase.moderation.Permission;
import co.chatsdk.firebase.update.FirebaseUpdate;
import co.chatsdk.firebase.update.FirebaseUpdateWriter;
import io.reactivex.CompletableSource;
import io.reactivex.ObservableSource;
import io.reactivex.SingleSource;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.ThreadMetaValue;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.sorter.MessageSorter;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import co.chatsdk.firebase.FirebaseEntity;
import sdk.chat.core.utils.StringChecker;
import sdk.guru.realtime.DocumentChange;
import sdk.guru.realtime.RealtimeEventListener;
import co.chatsdk.firebase.FirebasePaths;
import sdk.guru.realtime.RealtimeReferenceManager;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.utils.Generic;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import sdk.guru.common.RX;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;
import sdk.guru.realtime.RXRealtime;

public class ThreadWrapper implements RXRealtime.DatabaseErrorListener {

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
    public Completable on() {

        Completable completable = metaOn();
        usersOn();
        permissionsOn();

        messagesOn();
        messageRemovedOn();
        if (ChatSDK.typingIndicator() != null) {
            ChatSDK.typingIndicator().typingOn(model);
        }

        // Update our permission level
        if (model.typeIs(ThreadType.Group)) {
            completable = myPermission().andThen(completable);
        }

        return completable;
    }

    /**
     * Stop listening to thread details change
     **/
    public void off() {
        metaOff();
        messagesOff();
        usersOff();
        permissionsOff();
        if(ChatSDK.typingIndicator() != null) {
            ChatSDK.typingIndicator().typingOff(model);
        }
    }

//    public void updateListenersForPermissions() {
//        if (ChatSDK.thread().roleForUser(model, ChatSDK.currentUser()).equals(Permission.Banned)) {
//            messagesOff();
//            if (ChatSDK.typingIndicator() != null) {
//                ChatSDK.typingIndicator().typingOff(model);
//            }
//        } else {
//            messagesOn();
//            messageRemovedOn();
//            if (ChatSDK.typingIndicator() != null) {
//                ChatSDK.typingIndicator().typingOn(model);
//            }
//        }
//    }

    public void permissionsOff() {
        DatabaseReference ref = FirebasePaths.threadPermissionsRef(model.getEntityID());
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    public void messageRemovedOn() {
        RXRealtime realtime = new RXRealtime(this);

        Query query = FirebasePaths.threadMessagesRef(model.getEntityID());

        if (!RealtimeReferenceManager.shared().isOn(query)) {
            query = query.orderByChild(Keys.Date);

            Date startDate = null;

            // We do it this way because otherwise when we exceed the number of messages,
            // This event is triggered as the messages go out of scope
            List<Message> messages = model.getMessages();
            if (messages.size() > ChatSDK.config().messageDeletionListenerLimit) {
                startDate = messages.get(messages.size() - ChatSDK.config().messageDeletionListenerLimit).getDate();
            } else if (!messages.isEmpty()) {
                startDate = messages.get(0).getDate();
            }

            if (startDate != null) {
                query = query.startAt(startDate.getTime());
            }

            realtime.childOn(query).observeOn(RX.db()).doOnNext(change -> {
                if(change.getSnapshot().exists() && change.getType() == EventType.Removed) {
                    MessageWrapper message = new MessageWrapper(change.getSnapshot().getKey());
                    if (!message.getModel().getSender().isMe() || message.getModel().getMessageStatus() != MessageSendStatus.Failed) {
                        model.removeMessage(message.getModel());
                    }
                }
            }).ignoreElements().subscribe(ChatSDK.events());

            realtime.addToReferenceManager();
        }
    }

    /**
     * Start listening to incoming messages.
     *
     * @return*/
    public void messagesOn() {

        if (RealtimeReferenceManager.shared().isOn(messagesRef())) {
            return;
        }

        threadDeletedDate().flatMapCompletable(deletedTimestamp -> {

            Long startTimestamp = null;

            Date lastMessageAddedDate = model.getLastMessageAddedDate();
            if(lastMessageAddedDate != null) {
                startTimestamp = lastMessageAddedDate.getTime() + 1;
            }

            if(deletedTimestamp > 0) {
                model.setLoadMessagesFrom(new Date(deletedTimestamp));

                // If there were no new messages since the deleted date, then we know the thread is still in the
                // deleted state
                if (startTimestamp == null || deletedTimestamp > startTimestamp) {
                    startTimestamp = deletedTimestamp;
                }

            } else {
                model.setLoadMessagesFrom(null);
            }

            final Long finalStartTimestamp = startTimestamp;

            // If there are already messages in the database, just load messages since the last one
            // otherwise we load the messagesToLoadPerBatch
            Completable loadMessages;
            if (startTimestamp != null) {
                loadMessages = loadMoreMessagesAfter(new Date(startTimestamp), 0).ignoreElement();
            } else {
                loadMessages = loadMoreMessagesBefore(null, ChatSDK.config().messagesToLoadPerBatch).ignoreElement();
            }

            return loadMessages.observeOn(RX.db()).andThen(Completable.defer(() -> {

                Query query = messagesRef();

                Date messageAddedDate = model.getLastMessageAddedDate();

                // After the message load, determine if the thread is still deleted
                if (deletedTimestamp > 0 && messageAddedDate == null) {
                    model.setDeleted(true);
                } else {
                    model.setDeleted(false);
                }

                if (messageAddedDate == null && finalStartTimestamp != null) {
                    messageAddedDate = new Date(finalStartTimestamp);
                }
                if(messageAddedDate != null) {
                    query = query.startAt(messageAddedDate.getTime() + 1, Keys.Date);
                }

                query = query.orderByChild(Keys.Date);

                RXRealtime realtime = new RXRealtime(this);
                realtime.childOn(query).observeOn(RX.db()).doOnNext(change -> {
                    if (change.getType() == EventType.Added) {
                        String from = change.getSnapshot().child(Keys.From).getValue(String.class);
                        if (ChatSDK.blocking() == null || !ChatSDK.blocking().isBlocked(from)) {
                            model.setDeleted(false);

                            MessageWrapper message = new MessageWrapper(change.getSnapshot());

                            boolean newMessage = message.getModel().getMessageStatus() == MessageSendStatus.None;

                            message.markAsReceived().subscribe(ChatSDK.events());

                            ChatSDK.hook().executeHook(HookEvent.MessageReceived, new HashMap<String, Object>() {{
                                put(HookEvent.Message, message.getModel());
                                put(HookEvent.IsNew_Boolean, newMessage);
                            }}).doOnComplete(() -> {
                                model.addMessage(message.getModel(), newMessage);
                            }).subscribe(ChatSDK.events());
                        }
                    }
                }).doOnError(throwable -> {
                    //
                    Logger.debug(throwable);
                }).ignoreElements().subscribe(ChatSDK.events());

                realtime.addToReferenceManager();

                return Completable.complete();
            }).subscribeOn(RX.db()));
        }).subscribe(ChatSDK.events());

    }

    /**
     * Stop listening to incoming messages.
     **/
    public void messagesOff() {
        DatabaseReference ref = messagesRef();
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    public Completable metaOn() {
        return Completable.defer((Callable<CompletableSource>) () -> {
            DatabaseReference ref = FirebasePaths.threadMetaRef(model.getEntityID());
            if (!RealtimeReferenceManager.shared().isOn(ref)) {
                RXRealtime realtime = new RXRealtime(this);

                Completable completable = realtime.on(ref).doOnNext(change -> {
                    if (change.getSnapshot().exists()) {
                        deserialize(change.getSnapshot());
                        Map<String, Object> map = change.getSnapshot().getValue(Generic.mapStringObject());
                        if (map != null) {
                            model.setMetaValues(map);
                        }
                    }
                }).firstElement().ignoreElement();

                realtime.addToReferenceManager();

                return completable;
            }
            return Completable.complete();
        });
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

        }).subscribeOn(RX.io());
    }

    public void metaOff () {
        DatabaseReference ref = FirebasePaths.threadMetaRef(model.getEntityID());
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    //Note the old listener that was used to process the thread bundle is still in use.
    /**
     * Start listening to users added to this thread.
     **/
    public void usersOn() {

        final DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());

        if(!RealtimeReferenceManager.shared().isOn(ref)) {
            RXRealtime realtime = new RXRealtime(this);

            realtime.childOn(ref).map(change -> {
                final UserWrapper user = new UserWrapper(change.getSnapshot().getKey());

                if (change.getType() == EventType.Added) {
                    model.addUser(user.getModel());
                }
                // We don't remove the current user. If we leave the thread, we still
                // want to be a member so the thread is still associated with us
                if (change.getType() == EventType.Removed) {
                    if (!user.getModel().isMe()) {
                        model.removeUser(user.getModel());
                    } else {
                        model.setPermission(user.getModel().getEntityID(), Permission.None, false, false);
                    }
//                    updateListenersForPermissions();
                }

                if (change.getType() == EventType.Modified) {
                    if (user.getModel().isMe()) {
                        Boolean muted = change.getSnapshot().child(Keys.Mute).getValue(Boolean.class);
                        if (muted != null) {
                            model.setMuted(muted);
                        }
                        Long deleted = change.getSnapshot().child(Keys.Deleted).getValue(Long.class);
                        if (deleted != null) {
                            model.setLoadMessagesFrom(new Date(deleted));
                        }
                    }
                }

                return new Event<>(user.getModel(), change.getType());

            }).flatMapCompletable(userEvent -> {
                if (userEvent.typeIs(EventType.Added)) {
                    return ChatSDK.core().userOn(userEvent.get());
                }
                return Completable.complete();
            }).subscribe(ChatSDK.events());

            realtime.addToReferenceManager();
        }
    }

    /**
     * Stop listening to users added to this thread.
     **/
    public void usersOff(){
        DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());
        RealtimeReferenceManager.shared().removeListeners(ref);
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

            currentThreadUser.addListenerForSingleValueEvent(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    e.onSuccess((Long) snapshot.getValue());
                }
                else {
                    e.onSuccess(Long.valueOf(-1));
                }
            }));

        }).subscribeOn(RX.io());
    }

    //Note - Maybe should treat group thread and one on one thread the same
    /**
     * Deleting a thread, CoreThread isn't always actually deleted from the db.
     * We mark the thread as deleted and mark the user in the thread users ref as deleted.
     **/
    public Completable deleteThread() {
        return new ThreadDeleter(model).execute();
    }

    public Single<List<Message>> loadMoreMessagesBefore(@Nullable final Date before, final Integer numberOfMessages) {
        return loadMoreMessages(null, before, numberOfMessages);
    }

    public Single<List<Message>> loadMoreMessagesAfter(@Nullable final Date after, final Integer numberOfMessages) {
        return loadMoreMessages(after, null, numberOfMessages);
    }

    public Single<List<Message>> loadMoreMessagesBetween(@Nullable final Date before, @Nullable final Date after) {
        return loadMoreMessages(after, before, 0);
    }

    protected Single<List<Message>> loadMoreMessages(@Nullable final Date after, @Nullable final Date before, final Integer numberOfMessages){
        return Single.create((SingleOnSubscribe<List<Message>>) e -> {

            DatabaseReference messageRef = FirebasePaths.threadMessagesRef(model.getEntityID());

            Query query = messageRef.orderByChild(Keys.Date);

            if (numberOfMessages > 0) {
                query = query.limitToLast(numberOfMessages + 1);
            }

            if (before != null) {
                query = query.endAt(before.getTime() - 1, Keys.Date);
            }

            if (after != null) {
                query = query.startAt(after.getTime() + 1, Keys.Date);
            }

            query.addListenerForSingleValueEvent(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    List<Message> messages = new ArrayList<>();

                    Map<String, Object> hashData = snapshot.getValue(Generic.mapStringObject());

                    MessageWrapper message;
                    for (String key : hashData.keySet())
                    {
                        message = new MessageWrapper(snapshot.child(key));
                        model.addMessage(message.getModel(), false);

                        messages.add(message.getModel());
                    }

                    // Sort the messages
                    // We need to do this because the data comes as a hash map that's not sorted
                    Collections.sort(messages, new MessageSorter());

                    ChatSDK.events().source().onNext(NetworkEvent.messageAdded(messages.get(0)));

                    e.onSuccess(messages);
                }
                else {
                    e.onSuccess(new ArrayList<>());
                }
            }));
        }).subscribeOn(RX.io());
    }

    /**
     * Converting the thread details to a map object.
     **/
    protected Map<String, Object> serialize() {
        return new HashMap<String, Object>() {{
            put(Keys.CreationDate, ServerValue.TIMESTAMP);
            if (!StringChecker.isNullOrEmpty(model.getName())) {
                put(Keys.Name, model.getName());
            }
            put(Keys.Type, model.getType());
            put(Keys.Creator, model.getCreator().getEntityID());
            put(Keys.ImageUrl, model.getImageUrl());
            if (FirebaseModule.config().enableCompatibilityWithV4) {
                put("creator-entity-id", model.getCreator().getEntityID());
                put("type_v4", model.getType());
            }
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
        String creatorEntityID = null;
        if (snapshot.hasChild(Keys.Creator)) {
            creatorEntityID = snapshot.child(Keys.Creator).getValue(String.class);
        }
        if (creatorEntityID == null && FirebaseModule.config().enableCompatibilityWithV4) {
            if (snapshot.hasChild("creator-entity-id")) {
                creatorEntityID = snapshot.child("creator-entity-id").getValue(String.class);
            }
        }
        if (creatorEntityID != null) {
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

        }).doOnComplete(() -> {
            if (FirebaseModule.config().enableCompatibilityWithV4) {
                DatabaseReference ref = FirebasePaths.threadRef(model.getEntityID());
                ref.updateChildren(new HashMap<String, Object>() {{
                    put("details", serialize());
                }});
            }
        }).subscribeOn(RX.io());
    }

    public Completable setPermission(String userEntityID, String permission) {
        return Completable.defer(() -> new RXRealtime().set(FirebasePaths.threadUserPermissionRef(model.getEntityID(), userEntityID), permission));
    }

    public Completable setPermissions(Map<String, String> userEntityIDPermissionMap) {
        return Completable.defer(() -> new RXRealtime().set(FirebasePaths.threadPermissionsRef(model.getEntityID()), userEntityIDPermissionMap));
    }

    public void permissionsOn() {
        DatabaseReference ref = FirebasePaths.threadPermissionsRef(model.getEntityID());
        if (!RealtimeReferenceManager.shared().isOn(ref)) {
            RXRealtime realtime = new RXRealtime(this);

            realtime.childOn(ref).map(change -> {
                if (change.getSnapshot().exists()) {
                    model.setPermission(change.getSnapshot().getKey(), change.getSnapshot().getValue(String.class));
//                    updateListenersForPermissions();
                } else {
                    // If no permission is set, we set it to member
                    model.setPermission(change.getSnapshot().getKey(), Permission.Member);
                }
                return model;
            }).ignoreElements().subscribe(ChatSDK.events());

            realtime.addToReferenceManager();
        }
    }

    /**
     * When we first open the thread get our permission level to decide which listeners to add
     * @return
     */
    public Completable myPermission() {
        return Completable.defer(() -> {
            String currentEntityID = ChatSDK.currentUserID();
            DatabaseReference ref = FirebasePaths.threadPermissionsRef(model.getEntityID()).child(currentEntityID);
            RXRealtime realtime = new RXRealtime(this);
            return realtime.get(ref).flatMapCompletable(change -> {
                if (!change.isEmpty()) {
                    model.setPermission(currentEntityID, change.get().getValue(String.class));
                } else {
                    // If no permission is set, we set it to member
                    model.setPermission(currentEntityID, Permission.Member);
                }
                return Completable.complete();
            });
        });
    }

    public Completable leave() {
        return removeUsers(Arrays.asList(ChatSDK.currentUser()));
    }

    public Completable addUsers(final List<User> users) {
        return addUsers(users, null);
    }

    public Completable addUsers(final List<User> users, List<String> permissions) {
        return setUserThreadLinkValue(users, false, permissions);
    }

    public Completable removeUsers(final List<User> users) {
        return setUserThreadLinkValue(users, true, null);
    }

    /**
     * This function is a convenience function to add or remove batches of users
     * from threads. If the value is defined, it will populate the thread/users
     * path with the user IDs. And add the thread ID to the user/threads path for
     * private threads. If value is null, the users will be removed from the thread/users
     * path and the thread will be removed from the user/threads path
     *
     * @param users
     * @return
     */
    public Completable setUserThreadLinkValue(final List<User> users, boolean remove, @Nullable List<String> permissions) {
        return Single.create((SingleOnSubscribe<FirebaseUpdateWriter>) emitter -> {
            FirebaseUpdateWriter updateWriter = new FirebaseUpdateWriter(FirebaseUpdateWriter.Type.Update);

            User u;
            String p = null;
            for (int i = 0; i < users.size(); i++) {
                u = users.get(i);
                if (permissions != null && permissions.size() > i) {
                    p = permissions.get(i);
                }

                DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(model.getEntityID()).child(u.getEntityID()).child(Keys.Status);
                DatabaseReference userThreadsRef = FirebasePaths.userThreadsRef(u.getEntityID()).child(model.getEntityID()).child(Keys.InvitedBy);

                if (!remove) {

                    if (p != null) {
                        updateWriter.add(new FirebaseUpdate(FirebasePaths.threadUserPermissionRef(model.getEntityID(), u.getEntityID()), p));
                    }

                    updateWriter.add(new FirebaseUpdate(threadUsersRef, u.equalsEntity(model.getCreator()) ? Keys.Owner : Keys.Member));

                    // Public threads aren't added to the user path
                    if (!model.typeIs(ThreadType.Public)) {
                        updateWriter.add(new FirebaseUpdate(userThreadsRef, ChatSDK.currentUserID()));
                    } else if (u.isMe() && !ChatSDK.config().publicChatAutoSubscriptionEnabled) {
                        threadUsersRef.onDisconnect().removeValue();
                    }

                } else {
                    updateWriter.add(new FirebaseUpdate(threadUsersRef, null));
                    updateWriter.add(new FirebaseUpdate(userThreadsRef, null));
                }
            }
            emitter.onSuccess(updateWriter);
        }).subscribeOn(RX.db()).flatMap((Function<FirebaseUpdateWriter, SingleSource<?>>) FirebaseUpdateWriter::execute)
                .ignoreElement()
                .doOnComplete(() -> {
                    if (FirebaseModule.config().enableWebCompatibility) {
                        FirebaseEntity.pushThreadUsersUpdated(model.getEntityID()).subscribe(ChatSDK.events());
                        for (User u : users) {
                            FirebaseEntity.pushUserThreadsUpdated(u.getEntityID()).subscribe(ChatSDK.events());
                        }
                    }
                });
    }

    public DatabaseReference messagesRef () {
        return FirebasePaths.threadMessagesRef(model.getEntityID());
    }

    @Override
    public void onError(Query ref, DatabaseError error) {
        // Handle the error
        if (error.getCode() == -3) {
            RealtimeReferenceManager.shared().removeListeners(ref);
        }
    }
}
