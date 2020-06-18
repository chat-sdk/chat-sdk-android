package firestream.chat;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import firestream.chat.chat.AbstractChat;
import firestream.chat.chat.Chat;
import firestream.chat.chat.User;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.filter.Filter;
import firestream.chat.firebase.rx.MultiQueueSubject;
import firestream.chat.firebase.service.FirebaseService;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Path;
import firestream.chat.firebase.service.Paths;
import firestream.chat.interfaces.IChat;
import firestream.chat.interfaces.IFireStream;
import firestream.chat.message.Body;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Invitation;
import firestream.chat.message.Message;
import firestream.chat.message.Presence;
import firestream.chat.message.Sendable;
import firestream.chat.message.TextMessage;
import firestream.chat.message.TypingState;
import firestream.chat.namespace.Fire;
import firestream.chat.types.ContactType;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.InvitationType;
import firestream.chat.types.PresenceType;
import firestream.chat.types.SendableType;
import firestream.chat.types.TypingStateType;
import firestream.chat.util.Typing;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.BehaviorSubject;
import sdk.guru.common.Event;
import sdk.guru.common.RX;

public class FireStream extends AbstractChat implements IFireStream {

    public static final FireStream instance = new FireStream();

    protected FirebaseUser user;

    protected List<User> contacts = new ArrayList<>();
    protected List<User> blocked = new ArrayList<>();
    protected Map<String, Date> muted = new HashMap<>();

    protected MultiQueueSubject<Event<Chat>> chatEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<Event<User>> contactEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<Event<User>> blockedEvents = MultiQueueSubject.create();

    protected BehaviorSubject<ConnectionEvent> connectionEvents = BehaviorSubject.create();

    protected FirebaseService firebaseService = null;
    protected WeakReference<Context> context;

    protected Predicate<Event<? extends Sendable>> markReceivedFilter = message -> {
        return getConfig().deliveryReceiptsEnabled && getConfig().autoMarkReceived;
    };

    /**
     * Current configuration
     */
    protected FirestreamConfig config;

    public static FireStream shared () {
        return instance;
    }

    protected List<IChat> chats = new ArrayList<>();

    public FireStream() {

        RxJavaPlugins.setErrorHandler(Fire.stream());

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            // We are connecting for the first time
            if (this.user == null && firebaseAuth.getCurrentUser() != null) {
                this.user = firebaseAuth.getCurrentUser();
                try {
                    connect();
                } catch (Exception e) {
                    events.publishThrowable().onNext(e);
                }
            }
            if(this.user != null && firebaseAuth.getCurrentUser() == null) {
                this.user = null;
                disconnect();
            }
        });
    }

    @Override
    public void initialize(Context context, @Nullable FirestreamConfig config, FirebaseService service) {
        this.context = new WeakReference<>(context);
        if (config == null) {
            config = new FirestreamConfig<>(this);
        }
        this.config = config;
        firebaseService = service;
    }

    @Override
    public void initialize(Context context, FirebaseService service) {
        initialize(context, null, service);
    }

    @Override
    public boolean isInitialized() {
        return config != null;
    }

    public void connect () throws Exception {

        if (this.config == null) {
            throw new Exception(context().getString(R.string.error_initialize_not_run));
        }
        if (this.user == null) {
            throw new Exception(context().getString(R.string.error_no_authenticated_user));
        }

        connectionEvents.onNext(ConnectionEvent.willConnect());

        // MESSAGE DELETION

        // We always delete delivery receipt and presence messages
        Observable<Event<Sendable>> stream = getSendableEvents().getSendables().pastAndNewEvents();
        if (!config.deleteMessagesOnReceipt) {
            List<SendableType> typesToDelete = new ArrayList<>();
            typesToDelete.add(SendableType.presence());
            if (config.deleteDeliveryReceiptsOnReceipt) {
                typesToDelete.add(SendableType.deliveryReceipt());
            }
            stream = stream.filter(Filter.eventBySendableType(typesToDelete));
        }
        // If deletion is enabled, we don't filter so we delete all the message types
        stream.filter(Event::isAdded)
                .map(Event::get)
                .flatMapCompletable(this::deleteSendable).subscribe(this);

        // DELIVERY RECEIPTS

        getSendableEvents()
                .getMessages()
                .pastAndNewEvents()
                .filter(deliveryReceiptFilter())
                .flatMapCompletable(event -> markReceived(event.get()))
                .subscribe(this);

        // If message deletion is disabled, send a received receipt to our-self for each message. This means
        // that when we add a childListener, we only get new messages
//        if (!config.deleteMessagesOnReceipt && config.startListeningFromLastSentMessageDate) {
//            getSendableEvents()
//                    .getMessages()
//                    .pastAndNewEvents()
//                    .filter(Filter.notFromMe())
//                    .flatMapCompletable(event -> {
//
//                    return sendDeliveryReceipt(currentUserId(), DeliveryReceiptType.received(), event.get().getId());
//
//            }).subscribe(this);
//        }

        // INVITATIONS

        getSendableEvents().getInvitations().pastAndNewEvents().flatMapCompletable(event -> {
            if (config.autoAcceptChatInvite) {
                return event.get().accept();
            }
            return Completable.complete();
        }).subscribe(this);

        // BLOCKED USERS

        dm.add(listChangeOn(Paths.blockedPath()).subscribe(listEvent -> {
            Event<User> ue = listEvent.to(User.from(listEvent));
            if (ue.isAdded()) {
                blocked.add(ue.get());
            }
            if (ue.isRemoved()) {
                blocked.remove(ue.get());
            }
            blockedEvents.onNext(ue);
        }));

        // CONTACTS

        dm.add(listChangeOn(Paths.contactsPath()).subscribe(listEvent -> {
            Event<User> ue = listEvent.to(User.from(listEvent));
            if (ue.isAdded()) {
                contacts.add(ue.get());
            }
            else if (ue.isRemoved()) {
                contacts.remove(ue.get());
            }
            contactEvents.onNext(ue);
        }));

        // CONNECT TO EXISTING GROUP CHATS

        dm.add(listChangeOn(Paths.userChatsPath()).subscribe(listEvent -> {
            Event<Chat> chatEvent = listEvent.to(Chat.from(listEvent));
            IChat chat = chatEvent.get();
            if (chatEvent.isAdded()) {
                chat.connect();
                chats.add(chat);
                chatEvents.onNext(chatEvent);
            }
            else if (chatEvent.isRemoved()) {
                dm.add(chat.leave().subscribe(() -> {
                    chats.remove(chat);
                    chatEvents.onNext(chatEvent);
                }, this));
            } else {
                chatEvents.onNext(chatEvent);
            }
        }));

        dm.add(listChangeOn(Paths.userMutedPath()).subscribe(listDataEvent -> {
            String id = listDataEvent.get().getId();
            if (listDataEvent.isRemoved()) {
                muted.remove(id);
            } else {
                Object date = listDataEvent.get().getData().get(Keys.Date);
                if (date instanceof Long) {
                    muted.put(id, new Date((Long) date));
                }
            }
        }));

        // Connect to the message events AFTER we have added our events listeners
        super.connect();

        connectionEvents.onNext(ConnectionEvent.didConnect());
    }

    @Override
    public void disconnect() {
        connectionEvents.onNext(ConnectionEvent.willDisconnect());
        super.disconnect();
        connectionEvents.onNext(ConnectionEvent.didDisconnect());
    }

    @Override
    public String currentUserId() {
        return user.getUid();
    }

    //
    // Messages
    //

    @Override
    public Completable deleteSendable (Sendable sendable) {
        return deleteSendable(sendable.getId());
    }

    @Override
    public Completable deleteSendable (String toUserId, Sendable sendable) {
        return deleteSendable(toUserId, sendable.getId());
    }

    @Override
    public Completable deleteSendable (String sendableId) {
        return deleteSendable(Paths.messagePath(sendableId));
    }

    @Override
    public Completable deleteSendable (String toUserId, String sendableId) {
        return deleteSendable(Paths.messagePath(toUserId, sendableId));
    }

    @Override
    public Completable sendPresence(String userId, PresenceType type) {
        return sendPresence(userId, type, null);
    }

    @Override
    public Completable sendPresence(String userId, PresenceType type, @Nullable Consumer<String> newId) {
        return send(userId, new Presence(type), newId);
    }


    @Override
    public Completable sendInvitation(String userId, InvitationType type, String id) {
        return sendInvitation(userId, type, id, null);
    }

    @Override
    public Completable sendInvitation(String userId, InvitationType type, String groupId, @Nullable Consumer<String> newId) {
        return send(userId, new Invitation(type, groupId), newId);
    }

    @Override
    public Completable send(String toUserId, Sendable sendable) {
        return send(toUserId, sendable, null);
    }

    @Override
    public Completable send(String toUserId, Sendable sendable, @Nullable Consumer<String> newId) {
        return send(Paths.messagesPath(toUserId), sendable, newId);
    }

    @Override
    public Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId) {
        return sendDeliveryReceipt(userId, type, messageId, null);
    }

    @Override
    public Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId) {
        return send(userId, new DeliveryReceipt(type, messageId), newId);
    }

    @Override
    public Completable startTyping(String userId) {
        return Completable.defer(() -> {
            final Typing typing = typingMap.get(userId);
            if (!typing.isTyping) {
                typing.isTyping = true;
                return send(userId, new TypingState(TypingStateType.typing()), s -> {
                    typing.sendableId = s;
                });
            }
            return Completable.complete();
        });
    }

    @Override
    public Completable stopTyping(String userId) {
        return Completable.defer(() -> {
            final Typing typing = typingMap.get(userId);
            if (typing.isTyping) {
                return deleteSendable(userId, typing.sendableId).doOnComplete(() -> {
                    typing.isTyping = false;
                    typing.sendableId = null;
                });
            }
            return Completable.complete();
        });
    }

    @Override
    public Completable sendMessageWithText(String userId, String text) {
        return sendMessageWithText(userId, text, null);
    }

    @Override
    public Completable sendMessageWithText(String userId, String text, @Nullable Consumer<String> newId) {
        return send(userId, new TextMessage(text), newId);
    }

    @Override
    public Completable sendMessageWithBody(String userId, Body body) {
        return sendMessageWithBody(userId, body, null);
    }

    @Override
    public Completable sendMessageWithBody(String userId, Body body, @Nullable Consumer<String> newId) {
        return send(userId, new Message(body), newId);
    }

    //
    // Blocking
    //

    @Override
    public Completable block(User user) {
        return addUser(Paths.blockedPath(), User.dateDataProvider(), user);
    }

    @Override
    public Completable unblock(User user) {
        return removeUser(Paths.blockedPath(), user);
    }

    @Override
    public List<User> getBlocked() {
        return blocked;
    }

    @Override
    public boolean isBlocked(User user) {
        return blocked.contains(user);
    }

    //
    // Contacts
    //

    @Override
    public Completable addContact(User user, ContactType type) {
        user.setContactType(type);
        return addUser(Paths.contactsPath(), User.contactTypeDataProvider(), user);
    }

    @Override
    public Completable removeContact(User user) {
        return removeUser(Paths.contactsPath(), user);
    }

    @Override
    public List<User> getContacts() {
        return contacts;
    }

    //
    // Chats
    //

    @Override
    public Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, User... users) {
        return createChat(name, imageURL, null, Arrays.asList(users));
    }

    @Override
    public Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, @Nullable Map<String, Object> customData, User... users) {
        return createChat(name, imageURL, customData, Arrays.asList(users));
    }

    @Override
    public Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, List<? extends User> users) {
        return createChat(name, imageURL, null, users);
    }

    @Override
    public Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, @Nullable Map<String, Object> customData, List<? extends User> users) {
        return Chat.create(name, imageURL, customData, users).flatMap(chat -> {
            return joinChat(chat).toSingle(() -> chat);
        });
    }

    @Override
    public IChat getChat(String chatId) {
        for (IChat chat : chats) {
            if (chat.getId().equals(chatId)) {
                return chat;
            }
        }
        return null;
    }

    @Override
    public Completable leaveChat(IChat chat) {
        // We remove the chat from our list of chats, when that completes,
        // we will remove our self from the chat roster
        return getFirebaseService().chat.leaveChat(chat.getId())
                .observeOn(RX.main());
    }

    @Override
    public Completable joinChat(IChat chat) {
        return getFirebaseService().chat
                .joinChat(chat.getId())
                .observeOn(RX.main());
    }

    @Override
    public List<IChat> getChats() {
        return chats;
    }

    /**
     * Send a read receipt
     * @return completion
     */
    @Override
    public Completable markRead(Sendable sendable) {
        return markRead(sendable.getFrom(), sendable.getId());
    }

    @Override
    public Completable markRead(String fromUserId, String sendableId) {
        return Fire.stream().sendDeliveryReceipt(fromUserId, DeliveryReceiptType.read(), sendableId);
    }

    /**
     * Send a received receipt
     * @return completion
     */
    @Override
    public Completable markReceived(Sendable sendable) {
        return markReceived(sendable.getFrom(), sendable.getId());
    }

    @Override
    public Completable markReceived(String fromUserId, String sendableId) {
        return Fire.stream().sendDeliveryReceipt(fromUserId, DeliveryReceiptType.received(), sendableId);
    }

    //
    // Events
    //

    @Override
    public MultiQueueSubject<Event<Chat>> getChatEvents() {
        return chatEvents;
    }

    @Override
    public MultiQueueSubject<Event<User>> getBlockedEvents() {
        return blockedEvents;
    }

    @Override
    public MultiQueueSubject<Event<User>> getContactEvents() {
        return contactEvents;
    }

    @Override
    public Observable<ConnectionEvent> getConnectionEvents() {
        return connectionEvents.hide();
    }

    //
    // Utility
    //

    @Override
    public User currentUser() {
        return new User(currentUserId());
    }

    @Override
    protected Path messagesPath() {
        return Paths.messagesPath();
    }

    public FirestreamConfig getConfig() {
        return config;
    }

    public FirebaseService getFirebaseService() {
        return firebaseService;
    }

    public Context context() {
        return context.get();
    }

    public Throwable getError(@StringRes int resId) {
        return new Throwable(context().getString(resId));
    }

    @Override
    public void setMarkReceivedFilter(Predicate<Event<? extends Sendable>> filter) {
        this.markReceivedFilter = filter;
    }

    public Predicate<Event<? extends Sendable>> getMarkReceivedFilter() {
        return markReceivedFilter;
    }

    @Override
    public Completable mute(User user) {
        return mute(user, null);
    }

    @Override
    public Completable mute(User user, @Nullable Date until) {
        return mute(user.getId(), until);
    }

    @Override
    public Completable unmute(User user) {
        return mute(user, null);
    }

    @Override
    public Date mutedUntil(User user) {
        return mutedUntil(user.getId());
    }

    @Override
    public boolean muted(User user) {
        return muted(user.getId());
    }

    // Internal mute methods

    public Date mutedUntil(String id) {
        return muted.get(id);
    }

    public boolean muted(String id) {
        return mutedUntil(id) != null;
    }

    public Completable mute(String id) {
        return mute(id, null);
    }

    public Completable mute(String id, @Nullable Date until) {
        return getFirebaseService().core.mute(Paths.userMutedPath().child(id), new HashMap<String, Object>() {{
            put(Keys.Date, until != null ? until.getTime() : Long.MAX_VALUE);
        }});
    }

    public Completable unmute(String id) {
        return getFirebaseService().core.unmute(Paths.userMutedPath().child(id));
    }

}
