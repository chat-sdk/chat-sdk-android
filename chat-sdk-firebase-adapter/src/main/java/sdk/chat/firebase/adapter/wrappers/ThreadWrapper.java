/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package sdk.chat.firebase.adapter.wrappers;

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

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.dao.sorter.MessageSorter;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.firebase.adapter.FirebaseEntity;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.moderation.Permission;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.update.FirebaseUpdate;
import sdk.chat.firebase.adapter.update.FirebaseUpdateWriter;
import sdk.chat.firebase.adapter.utils.Generic;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;
import sdk.guru.common.RX;
import sdk.guru.realtime.RXRealtime;
import sdk.guru.realtime.RealtimeEventListener;
import sdk.guru.realtime.RealtimeReferenceManager;

public class ThreadWrapper implements RXRealtime.DatabaseErrorListener {

    protected Thread model;

    public ThreadWrapper(Thread thread){
        this.model = thread;
    }
    
    public ThreadWrapper(String entityId) {
        this(ChatSDK.db().fetchOrCreateThreadWithEntityID(entityId));
    }

    public Thread getModel(){
        return model;
    }

    /**
     * Start listening to thread details changes.
     **/
    public Completable on() {
        return Completable.defer(() -> {

//            Completable metaOnCompletable = metaOn().doOnComplete(() -> {
//                permissionsOn();
//                updateListenersForPermissions();
//            });

            return myPermission().doOnNext(s -> {
                updateListenersForPermissions();
            }).ignoreElements();

            // TODO: Thread
//            return myPermission().andThen(metaOnCompletable);
        });
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

    public void updateListenersForPermissions() {
        Logger.debug("updateListeners for thread: " + model.getEntityID());
        if (ChatSDK.thread().roleForUser(model, ChatSDK.currentUser()).equals(Permission.Banned)) {
            metaOff();
            messagesOff();
            usersOff();
            if (ChatSDK.typingIndicator() != null) {
                ChatSDK.typingIndicator().typingOff(model);
            }
            model.getUserThreadLink(ChatSDK.currentUser().getId()).setIsBanned(true);
        } else {
            metaOn().subscribe(ChatSDK.events());
            messagesOn();
            usersOn();
            if (ChatSDK.typingIndicator() != null) {
                ChatSDK.typingIndicator().typingOn(model);
            }
            model.getUserThreadLink(ChatSDK.currentUser().getId()).setIsBanned(false);
        }
    }

    public void permissionsOff() {
        DatabaseReference ref = FirebasePaths.threadPermissionsRef(model.getEntityID());
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    protected void messageRemovedOn() {
        RXRealtime realtime = new RXRealtime(this);

        Query query = messagesRef();

//        if (!RealtimeReferenceManager.shared().isOn(query)) {

            if (ChatSDK.config().messageDeletionListenerLimit < 0) {
                model.setCanDeleteMessagesFrom(new Date(0));
            } else {
                query = query.orderByChild(Keys.Date);

                Date startDate = null;

                // We do it this way because otherwise when we exceed the number of messages,
                // This event is triggered as the messages go out of scope
                int indexOfFirstDeletableMessage = model.indexOfFirstDeletableMessage();
                if (indexOfFirstDeletableMessage >=0 ) {
                    startDate = model.getMessages().get(indexOfFirstDeletableMessage).getDate();
                }

                if (startDate != null) {
                    query = query.startAt(startDate.getTime() - 1000);
                    model.setCanDeleteMessagesFrom(startDate);
                } else {
                    model.setCanDeleteMessagesFrom(new Date());
                }
            }

            realtime.childOn(query).observeOn(RX.db()).doOnNext(change -> {
                if(change.getSnapshot().exists() && change.getType() == EventType.Removed) {

                    Message message = ChatSDK.db().fetchMessageWithEntityID(change.getSnapshot().getKey());
                    if (message != null) {
                        // TODO: Check this - was will send
                        if (message.getMessageStatus() == MessageSendStatus.Initial) {
                            return;
                        }
                    }

                    // If the message send fails for example if there is a permission error
                    if (message != null && (message.getSender() == null || !message.getSender().isMe() || message.getMessageStatus() != MessageSendStatus.Failed)) {
                        removeMessage(message);
                    }
                }
            }).ignoreElements().doOnComplete(() -> {
                Logger.debug("Wrapper isOn: " + RealtimeReferenceManager.shared().isOn(messagesRef()));
                Logger.debug("");
            }).subscribe(ChatSDK.events());
//        }
    }

    public void messagesOn() {

        if (RealtimeReferenceManager.shared().isOn(messagesRef())) {
            return;
        }

        // We call messages added on first because it is asynchronous. So it takes some
        // Time for the listener to be added. To prevent a double listener, we call
        // Messages removed directly afterwards. That will add a message listener
        // So if messages on is called again, the ref will already be on
        messagesAddedOn();
        messageRemovedOn();

    }

    /**
     * Start listening to incoming messages.
     *
     * @return*/
    protected void messagesAddedOn() {

//        if (RealtimeReferenceManager.shared().isOn(messagesRef())) {
//            return;
//        }

        // Disable local notifications during setup
        boolean localPushEnabled = ChatSDK.config().showLocalNotifications;
        ChatSDK.config().setShowLocalNotifications(false);

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

//                if (RealtimeReferenceManager.shared().isOn(messagesRef())) {
//                    Logger.warn("Messages ref already on " + messagesRef());
//                    return Completable.complete();
//                }

                Query query = messagesRef();

                Date messageAddedDate = model.getLastMessageAddedDate();

                // After the message load, determine if the thread is still deleted
                if (deletedTimestamp > 0 && messageAddedDate == null) {
                    model.setDeleted(true);
                } else {
                    model.setDeleted(false, model.getDeleted() != null);
                }

                if (messageAddedDate == null && finalStartTimestamp != null) {
                    messageAddedDate = new Date(finalStartTimestamp);
                }
                if(messageAddedDate != null) {
                    query = query.startAt(messageAddedDate.getTime() + 1, Keys.Date);
                }

                query = query.orderByChild(Keys.Date);

                Logger.debug("Listen for messages: " + getModel().getEntityID());

                RXRealtime realtime = new RXRealtime(this);
                realtime.childOn(query).observeOn(RX.db()).doOnNext(change -> {
                    if (change.getType() == EventType.Added) {

                        String from = change.getSnapshot().child(Keys.From).getValue(String.class);
                        if (ChatSDK.blocking() == null || !ChatSDK.blocking().isBlocked(from)) {
                            model.setDeleted(false);

                            MessageWrapper message = FirebaseModule.config().provider.messageWrapper(change.getSnapshot());

                            Logger.debug("MessageAdded: " + message.getModel().getText());

                            // Temporarily set this because it's needed later on
                            message.getModel().setThread(model);

                            boolean newMessage = message.getModel().getMessageStatus() == MessageSendStatus.None;

                            ChatSDK.hook().executeHook(HookEvent.MessageReceived, new HashMap<String, Object>() {{
                                put(HookEvent.Message, message.getModel());
                                put(HookEvent.Thread, model);
                                put(HookEvent.IsNew_Boolean, newMessage);
                            }}).doOnComplete(() -> {
                                message.markAsReceived().subscribe(ChatSDK.events());
                                model.addMessage(message.getModel(), newMessage);
                            }).subscribe(ChatSDK.events());
                        }
                    }
                }).doOnError(throwable -> {
                    //
                    Logger.debug(throwable);
                }).ignoreElements().subscribe(ChatSDK.events());

//                realtime.addToReferenceManager();

                return Completable.complete();
            }).subscribeOn(RX.db()));
        }).doFinally(() -> {
            ChatSDK.config().setShowLocalNotifications(localPushEnabled);
        }).subscribe(ChatSDK.events());

    }

    public void removeMessage(Message message) {
        model.removeMessage(message);
    }

    /**
     * Stop listening to incoming messages.android
     **/
    public void messagesOff() {
        DatabaseReference ref = messagesRef();
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    public Completable metaOn() {
        return Completable.create(emitter -> {
            DatabaseReference ref = FirebasePaths.threadMetaRef(model.getEntityID());
            if (!RealtimeReferenceManager.shared().isOn(ref)) {
                RXRealtime realtime = new RXRealtime(this);

                realtime.on(ref).doOnNext(change -> {
                    if (change.getSnapshot().exists()) {
                        deserialize(change.getSnapshot());
//                        Map<String, Object> map = change.getSnapshot().getValue(Generic.mapStringObject());
//                        if (map != null) {
//                            model.setMetaValues(map);
//                        }
                    }
                    emitter.onComplete();
                }).ignoreElements().subscribe(ChatSDK.events());

//                realtime.addToReferenceManager();
            }
        }).subscribeOn(RX.io());
    }

    public Completable pushMeta() {
        return Completable.defer(() -> {
            Map<String, Object> meta = model.metaMap();
            if (meta.keySet().size() > 0) {
                RXRealtime realtime = new RXRealtime();
                DatabaseReference ref = FirebasePaths.threadMetaRef(model.getEntityID());
                return realtime.update(ref, meta);
            }
            return Completable.complete();
        }).subscribeOn(RX.io());
    }

    public void metaOff() {
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

            // Why is this needed?
            // TODO: Performance
//        realtime.on(ref).doOnNext(document -> {
//            if (document.hasValue()) {
//                Map<String, Object> map = document.getSnapshot().getValue(Generic.mapStringObject());
//                for (String key: map.keySet()) {
//                    final UserWrapper user = FirebaseModule.config().provider.userWrapper(key);
//                    user.on().subscribe();
//                    model.addUser(user.getModel());
//                }
//            }
//        }).subscribe();

            // TODO: Thread
//            ref.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.getValue() != null) {
//                        Map<String, Object> map = snapshot.getValue(Generic.mapStringObject());
//                        for (String key: map.keySet()) {
//                            final UserWrapper user = FirebaseModule.config().provider.userWrapper(key);
//
//                            user.on().subscribe();
//                            model.addUser(user.getModel());
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });

            realtime.childOn(ref).map(change -> {
                final UserWrapper user = FirebaseModule.config().provider.userWrapper(change.getSnapshot().getKey());
                user.on().subscribe();

                if (change.getType() == EventType.Added) {
                    Logger.debug("XX Add User:  " + user.model.getName() + ", to thread: " + model.getEntityID());

                    if (model.addUser(user.getModel())) {
                        Logger.debug("XX Success");
                    } else {
                        Logger.debug("XX Failure");
                    }
//                    Logger.warn("User link count:" + model.getUserThreadLinks().size());
//                    for (User u: model.getUsers()) {
//                        Logger.warn("--- User " + u.getName());
//                    }
                    for (UserThreadLink u: model.getUserThreadLinks()) {
                        Logger.debug("XX --- User " + u.getUser().getName());
                    }
                }
                // We don't remove the current user. If we leave the thread, we still
                // want to be a member so the thread is still associated with us
                if (change.getType() == EventType.Removed) {
                    // A user leaves the public group when leave the ChatActivity
                    // If we remove the user, we then delete their permission information
                    // So for public rooms, we don't remove the user. So we show a list of
                    // All users who have been in the room when we were
                    if (model.typeIs(ThreadType.Private)) {
                        if (!user.getModel().isMe()) {
                            model.removeUser(user.getModel());
                        } else {
                            model.setPermission(user.getModel().getEntityID(), Permission.None, true, false);
                        }
                    } else {
                        if(model.getUserThreadLink(user.getModel().getId()).setHasLeft(true)) {
                            ChatSDK.events().source().accept(NetworkEvent.threadUserRemoved(model, user.getModel()));
                        }
                    }
//                    updateListenersForPermissions();
                }

                if (change.getType() == EventType.Modified) {
                    if (user.getModel().isMe()) {
                        Boolean muted = change.getSnapshot().child(Keys.Mute).getValue(Boolean.class);
                        if (muted != null) {
                            model.setMuted(muted);
                        } else {
                            model.setMuted(false);
                        }
                        Long deleted = change.getSnapshot().child(Keys.Deleted).getValue(Long.class);
                        if (deleted != null) {
                            model.setLoadMessagesFrom(new Date(deleted));
                        }
                    }
                }

                return new Event<>(user.getModel(), change.getType());

            }).subscribe();

//                    .flatMapCompletable(userEvent -> {
//                if (userEvent.isAdded()) {
//                    return ChatSDK.core().userOn(userEvent.get());
//                }
//                return Completable.complete();
//            }).subscribe(ChatSDK.events());

//            realtime.addToReferenceManager();
        }
    }

    /**
     * Stop listening to users added to this thread.
     **/
    public void usersOff() {
        DatabaseReference ref = FirebasePaths.threadUsersRef(model.getEntityID());
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    //Note - Maybe should reject when cant find value in the user deleted path.
    /**
     * Get the date when the thread was deleted
     * @return Single On success return the date or -1 if the thread hasn't been deleted
     **/
    protected Single<Long> threadDeletedDate() {

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
//                ChatSDK.db().getDaoCore().getDaoSession().runInTx(() -> {
                    List<Message> messages = new ArrayList<>();

                    if(hasValue) {

                        Map<String, Object> hashData = snapshot.getValue(Generic.mapStringObject());

                        MessageWrapper message;
                        for (String key : hashData.keySet()) {
                            message = FirebaseModule.config().provider.messageWrapper(snapshot.child(key));
                            messages.add(message.getModel());
                        }

                        // Sort the messages
                        // We need to do this because the data comes as a hash map that's not sorted
                        Collections.sort(messages, new MessageSorter(DaoCore.ORDER_ASC));

                        for (Message m: messages) {
                            model.addMessage(m, false, false, true);
                        }

                        // Sort the messages
                        // We need to do this because the data comes as a hash map that's not sorted
                        model.sortMessages(true);

                        ChatSDK.db().update(model);


                        ChatSDK.events().source().accept(NetworkEvent.threadMessagesUpdated(getModel()));
//                        ChatSDK.events().source().accept(NetworkEvent.messageAdded(model.lastMessage()));
                    }

                    e.onSuccess(messages);
//                });
            }));
        }).subscribeOn(RX.io());
    }

    /**
     * Converting the thread details to a map object.
     **/
    protected Map<String, Object> serialize() {
        Map<String, Object> items = new HashMap<String, Object>() {{
            put(Keys.CreationDate, ServerValue.TIMESTAMP);
            put(Keys.Type, model.getType());
            put(Keys.Creator, model.getCreator().getEntityID());
        }};
        return items;
    }

    /**
     * Updating thread details from given map
     **/
    @SuppressWarnings("all") // To remove setType warning.
    protected void deserialize(DataSnapshot snapshot) {

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
        if (creatorEntityID != null) {
            model.setCreator(ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, creatorEntityID));
        }

        long type = ThreadType.PrivateGroup;
        if (snapshot.hasChild(Keys.Type)) {
            type = snapshot.child(Keys.Type).getValue(Long.class);
        }
        model.setType((int)type);

        Map<String, Object> meta = snapshot.getValue(Generic.mapStringObject());

        // When we add the data to Firebase we add thread "details" and "meta" data to the
        // same map. But when we recover them, we should not duplciate so any data that
        // is contained in details is not added to meta
        Map<String, Object> details = serialize();

        for (String key: details.keySet()) {
            meta.remove(key);
        }
        model.setMetaValues(meta, false);

        ChatSDK.db().update(model);
        ChatSDK.events().source().accept(NetworkEvent.threadMetaUpdated(model));
    }

    /**
     * Push the thread to firebase.
     **/
    public Completable push() {
        return Completable.defer(() -> {

            final Map<String, Object> data = serialize();

            // Also add the meta data
            data.putAll(model.metaMap());

            return Completable.create(e ->
            {

                // If the thread ID is null, create a new random ID
                if (model.getEntityID() == null || model.getEntityID().length() == 0) {
                    model.setEntityID(FirebasePaths.threadRef().push().getKey());
                    ChatSDK.db().update(model);
                }

                DatabaseReference metaRef = FirebasePaths.threadMetaRef(model.getEntityID());

                metaRef.updateChildren(data, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        FirebaseEntity.pushThreadUpdated(model.getEntityID()).subscribe(ChatSDK.events());
                        e.onComplete();
                    }
                    else {
                        e.onError(databaseError.toException());
                    }
                });

            });
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
                String userEntityID = change.getSnapshot().getKey();
                if (!ChatSDK.currentUserID().equals(userEntityID)) {
                    if (change.getSnapshot().exists()) {
                        model.setPermission(userEntityID, change.getSnapshot().getValue(String.class));
                    }
                    else {
                        // TODO: Thread
                        if (model.getCreator() != null && model.getCreator().isMe()) {
                            model.setPermission(change.getSnapshot().getKey(), Permission.Owner);
                        } else {
                            model.setPermission(change.getSnapshot().getKey(), Permission.Member);
                        }
                    }
                }
                return model;
            }).ignoreElements().subscribe(ChatSDK.events());

//            realtime.addToReferenceManager();
        }
    }

    /**
     * When we first open the thread get our permission level to decide which listeners to add
     * @return
     */
    @Nullable
    public Observable<String> myPermission() {
//        return Completable.defer(() -> {

        String currentEntityID = ChatSDK.currentUserID();
        DatabaseReference ref = FirebasePaths.threadPermissionsRef(model.getEntityID()).child(currentEntityID);

        if (RealtimeReferenceManager.shared().isOn(ref)) {
            return Observable.error(new Throwable("Permission is already on for user"));
        }

        RXRealtime realtime = new RXRealtime(this);

        Observable<String> observable = realtime.on(ref).map(change -> {
            String permission;
            if (change.hasValue()) {
                permission = change.getSnapshot().getValue(String.class);
            } else {
                // If no permission is set, we set it to member
                permission = Permission.Member;
            }
            model.setPermission(ChatSDK.currentUser(), permission, true, false);
            return permission;
        });

//        realtime.addToReferenceManager();

        return observable;

//            return realtime.get(ref).flatMapCompletable(change -> {
//                if (!change.isEmpty()) {
//                    model.setPermission(ChatSDK.currentUser(), change.get().getValue(String.class), true, false);
//                } else {
//                    // If no permission is set, we set it to member
//                    model.setPermission(ChatSDK.currentUser(), Permission.Member, true, false);
//                }
//                return Completable.complete();
//            });
//        });
    }

    public Completable leave() {
        return removeUsers(Arrays.asList(ChatSDK.currentUser()));
    }

    public Completable addUsers(final List<User> users) {
        return setUserThreadLinkValue(users, false);
    }

    public Completable removeUsers(final List<User> users) {
        return setUserThreadLinkValue(users, true);
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
    public Completable setUserThreadLinkValue(final List<User> users, boolean remove) {
        return Completable.defer(() -> {

            FirebaseUpdateWriter updateWriter = new FirebaseUpdateWriter();

            User u;
            for (int i = 0; i < users.size(); i++) {
                u = users.get(i);

                DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(model.getEntityID()).child(u.getEntityID()).child(Keys.Status);
                DatabaseReference userThreadsRef = FirebasePaths.userThreadsRef(u.getEntityID()).child(model.getEntityID()).child(Keys.InvitedBy);

                if (!remove) {

                    updateWriter.add(new FirebaseUpdate(threadUsersRef, u.equalsEntity(model.getCreator()) ? Keys.Owner : Keys.Member));

                    // Public threads aren't added to the user path
                    if (!model.typeIs(ThreadType.Public)) {
                        updateWriter.add(new FirebaseUpdate(userThreadsRef, ChatSDK.currentUserID()));
                    } else if (u.isMe() && !ChatSDK.config().publicChatAutoSubscriptionEnabled) {
                        threadUsersRef.onDisconnect().removeValue();
                    }

                } else {
                    if (!model.typeIs(ThreadType.Public)) {
                        updateWriter.add(new FirebaseUpdate(userThreadsRef, null));
                    }
                    updateWriter.add(new FirebaseUpdate(threadUsersRef, null));
                }
            }

            return updateWriter.execute();
        }).subscribeOn(RX.db()).doOnComplete(() -> {
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
