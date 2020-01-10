package firestream.chat;

import android.content.Context;

import androidx.annotation.IntegerRes;
import androidx.annotation.StringRes;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import firefly.sdk.chat.R;
import firestream.chat.events.SendableEvent;
import firestream.chat.interfaces.IChat;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.interfaces.IFireStream;
import firestream.chat.namespace.Fire;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import firestream.chat.chat.AbstractChat;
import firestream.chat.chat.Chat;
import firestream.chat.chat.User;
import firestream.chat.events.ChatEvent;
import firestream.chat.events.EventType;
import firestream.chat.events.UserEvent;
import firestream.chat.filter.MessageStreamFilter;
import firestream.chat.firebase.firestore.FirestoreService;
import firestream.chat.firebase.realtime.RealtimeService;
import firestream.chat.firebase.service.Paths;
import firestream.chat.firebase.service.FirebaseService;
import firestream.chat.firebase.service.Path;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Invitation;
import firestream.chat.message.Message;
import firestream.chat.message.Presence;
import firestream.chat.message.Sendable;
import firestream.chat.message.TextMessage;
import firestream.chat.message.TypingState;
import firestream.chat.firebase.rx.MultiQueueSubject;
import firestream.chat.types.ContactType;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.InvitationType;
import firestream.chat.types.PresenceType;
import firestream.chat.types.SendableType;
import firestream.chat.types.TypingStateType;
import io.reactivex.subjects.BehaviorSubject;

public class FireStream extends AbstractChat implements IFireStream {

    public static final FireStream instance = new FireStream();

    protected FirebaseUser user;

    protected ArrayList<User> contacts = new ArrayList<>();
    protected ArrayList<User> blocked = new ArrayList<>();

    protected MultiQueueSubject<ChatEvent> chatEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<UserEvent> contactEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<UserEvent> blockedEvents = MultiQueueSubject.create();

    protected BehaviorSubject<ConnectionEvent> connectionEvents = BehaviorSubject.create();

    protected FirebaseService firebaseService = null;
    protected WeakReference<Context> context;

    /**
     * Current configuration
     */
    protected Config config;

    public static FireStream shared () {
        return instance;
    }

    protected ArrayList<IChat> chats = new ArrayList<>();

    public FireStream() {

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
    public void initialize(Context context, @Nullable Config config) {
        this.context = new WeakReference<>(context);
        if (config == null) {
            config = new Config();
        }
        this.config = config;

        if (config.database == Config.DatabaseType.Firestore) {
            firebaseService = new FirestoreService();
        }
        if (config.database == Config.DatabaseType.Realtime) {
            firebaseService = new RealtimeService();
        }
    }

    @Override
    public void initialize(Context context) {
        initialize(context, null);
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

        // We always delete typing state and presence messages
        Observable<SendableEvent> stream = getSendableEvents().getSendables().pastAndNewEvents();
        if (!config.deleteMessagesOnReceipt) {
            stream = stream.filter(MessageStreamFilter.eventBySendableType(SendableType.typingState(), SendableType.presence()));
        }
        // If deletion isType enabled, we don't filter so we delete all the errorMessage types
        dm.add(stream.map(SendableEvent::getSendable).flatMapCompletable(this::deleteSendable).subscribe());

        // DELIVERY RECEIPTS

        dm.add(getSendableEvents().getMessages().pastAndNewEvents().flatMapCompletable(message -> {
            ArrayList<Completable> completables = new ArrayList<>();

            // If delivery receipts are enabled, send the delivery receipt
            if (config.deliveryReceiptsEnabled) {
                completables.add(markReceived(message));
            }
            // If errorMessage deletion isType disabled, instead mark the errorMessage as received. This means
            // that when we add a childListener, we only get new messages
            if (!config.deleteMessagesOnReceipt) {
                completables.add(sendDeliveryReceipt(currentUserId(), DeliveryReceiptType.received(), message.getId()));
            }

            return Completable.merge(completables);
        }).doOnError(this).subscribe());

        // INVITATIONS

        dm.add(getSendableEvents().getInvitations().pastAndNewEvents().flatMapCompletable(invitation -> {
            if (config.autoAcceptChatInvite) {
                return invitation.accept();
            }
            return Completable.complete();
        }).doOnError(this).subscribe());

        // BLOCKED USERS

        dm.add(listChangeOn(Paths.blockedPath()).subscribe(listEvent -> {
            UserEvent ue = UserEvent.from(listEvent);
            if (ue.typeIs(EventType.Added)) {
                blocked.add(ue.user);
            }
            if (ue.typeIs(EventType.Removed)) {
                blocked.remove(ue.user);
            }
            blockedEvents.onNext(ue);
        }));

        // CONTACTS

        dm.add(listChangeOn(Paths.contactsPath()).subscribe(listEvent -> {
            UserEvent ue = UserEvent.from(listEvent);
            if (ue.typeIs(EventType.Added)) {
                contacts.add(ue.user);
            }
            else if (ue.typeIs(EventType.Removed)) {
                contacts.remove(ue.user);
            }
            contactEvents.onNext(ue);
        }));

        // CONNECT TO EXISTING GROUP CHATS

        dm.add(listChangeOn(Paths.userChatsPath()).subscribe(listEvent -> {
            ChatEvent chatEvent = ChatEvent.from(listEvent);
            IChat chat = chatEvent.getChat();
            if (chatEvent.typeIs(EventType.Added)) {
                chat.connect();
                chats.add(chat);
                chatEvents.onNext(chatEvent);
            }
            else if (chatEvent.typeIs(EventType.Removed)) {
                dm.add(chat.leave().subscribe(() -> {
                    chats.remove(chat);
                    chatEvents.onNext(chatEvent);
                }, this));
            } else {
                chatEvents.onNext(chatEvent);
            }
        }));

        // Connect to the errorMessage events AFTER we have added our events listeners
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
        return deleteSendable(Paths.messagePath(sendable.getId()));
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
    public Completable sendTypingIndicator(String userId, TypingStateType type) {
        return sendTypingIndicator(userId, type, null);
    }

    @Override
    public Completable sendTypingIndicator(String userId, TypingStateType type, @Nullable Consumer<String> newId) {
        return send(userId, new TypingState(type), newId);
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
    public Completable sendMessageWithBody(String userId, HashMap<String, Object> body) {
        return sendMessageWithBody(userId, body, null);
    }

    @Override
    public Completable sendMessageWithBody(String userId, HashMap<String, Object> body, @Nullable Consumer<String> newId) {
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
    public ArrayList<User> getBlocked() {
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
        user.contactType = type;
        return addUser(Paths.contactsPath(), User.contactTypeDataProvider(), user);
    }

    @Override
    public Completable removeContact(User user) {
        return removeUser(Paths.contactsPath(), user);
    }

    @Override
    public ArrayList<User> getContacts() {
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
    public Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, @Nullable HashMap<String, Object> customData, User... users) {
        return createChat(name, imageURL, customData, Arrays.asList(users));
    }

    @Override
    public Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, List<? extends User> users) {
        return createChat(name, imageURL, null, users);
    }

    @Override
    public Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, @Nullable HashMap<String, Object> customData, List<? extends User> users) {
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
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable joinChat(IChat chat) {
        return getFirebaseService().chat
                .joinChat(chat.getId())
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
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
    public Completable markRead(Message message) {
        return Fire.Stream.sendDeliveryReceipt(message.getFrom(), DeliveryReceiptType.read(), message.getId());
    }

    /**
     * Send a received receipt
     * @return completion
     */
    @Override
    public Completable markReceived(Message message) {
        return Fire.Stream.sendDeliveryReceipt(message.getFrom(), DeliveryReceiptType.received(), message.getId());
    }

    //
    // Events
    //

    @Override
    public MultiQueueSubject<ChatEvent> getChatEvents() {
        return chatEvents;
    }

    @Override
    public MultiQueueSubject<UserEvent> getBlockedEvents() {
        return blockedEvents;
    }

    @Override
    public MultiQueueSubject<UserEvent> getContactEvents() {
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
    protected Single<Date> dateOfLastDeliveryReceipt() {
        if (config.deleteMessagesOnReceipt) {
            return Single.just(new Date(0));
        } else {
            return super.dateOfLastDeliveryReceipt();
        }
    }

    @Override
    public User currentUser() {
        return new User(currentUserId());
    }

    @Override
    protected Path messagesPath() {
        return Paths.messagesPath();
    }

    public Config getConfig() {
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

}
