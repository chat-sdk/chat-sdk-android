package firefly.sdk.chat;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import firefly.sdk.chat.events.ConnectionEvent;
import firefly.sdk.chat.namespace.Fl;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import firefly.sdk.chat.chat.AbstractChat;
import firefly.sdk.chat.chat.Chat;
import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.events.ChatEvent;
import firefly.sdk.chat.events.EventType;
import firefly.sdk.chat.events.UserEvent;
import firefly.sdk.chat.filter.MessageStreamFilter;
import firefly.sdk.chat.firebase.firestore.FirestoreService;
import firefly.sdk.chat.firebase.realtime.RealtimeService;
import firefly.sdk.chat.firebase.service.Paths;
import firefly.sdk.chat.firebase.service.FirebaseService;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.message.DeliveryReceipt;
import firefly.sdk.chat.message.Invitation;
import firefly.sdk.chat.message.Message;
import firefly.sdk.chat.message.Presence;
import firefly.sdk.chat.message.Sendable;
import firefly.sdk.chat.message.TextMessage;
import firefly.sdk.chat.message.TypingState;
import firefly.sdk.chat.firebase.rx.MultiQueueSubject;
import firefly.sdk.chat.types.ContactType;
import firefly.sdk.chat.types.DeliveryReceiptType;
import firefly.sdk.chat.types.InvitationType;
import firefly.sdk.chat.types.PresenceType;
import firefly.sdk.chat.types.SendableType;
import firefly.sdk.chat.types.TypingStateType;
import io.reactivex.subjects.BehaviorSubject;

public class Firefly extends AbstractChat {

    public static final Firefly instance = new Firefly();

    protected FirebaseUser user;

    protected ArrayList<User> contacts = new ArrayList<>();
    protected ArrayList<User> blocked = new ArrayList<>();

    protected MultiQueueSubject<ChatEvent> chatEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<UserEvent> contactEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<UserEvent> blockedEvents = MultiQueueSubject.create();

    protected BehaviorSubject<ConnectionEvent> connectionEvents = BehaviorSubject.create();

    protected FirebaseService firebaseService = null;
    protected WeakReference<Context> context;

    public static Firefly shared () {
        return instance;
    }

    protected ArrayList<Chat> chats = new ArrayList<>();

    public Firefly() {

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

    public void initialize(Context context, @Nullable Config config) {
        this.context = new WeakReference<>(context);
        if (!isInitialized()) {
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

    public boolean isInitialized() {
        return config != null;
    }

    public void initialize(Context context) {
        initialize(context, null);
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
        Observable<Sendable> stream = getEvents().getSendables().pastAndNewEvents();
        if (!config.deleteMessagesOnReceipt) {
            stream = stream.filter(MessageStreamFilter.bySendableType(SendableType.typingState(), SendableType.presence()));
        }
        // If deletion is enabled, we don't filter so we delete all the message types
        dm.add(stream.flatMapCompletable(this::deleteSendable).subscribe());

        // DELIVERY RECEIPTS

        dm.add(getEvents().getMessages().pastAndNewEvents().flatMapCompletable(message -> {
            ArrayList<Completable> completables = new ArrayList<>();

            // If delivery receipts are enabled, send the delivery receipt
            if (config.deliveryReceiptsEnabled) {
                completables.add(markReceived(message));
            }
            // If message deletion is disabled, instead mark the message as received. This means
            // that when we add a childListener, we only get new messages
            if (!config.deleteMessagesOnReceipt) {
                completables.add(sendDeliveryReceipt(currentUserId(), DeliveryReceiptType.received(), message.id));
            }

            return Completable.merge(completables);
        }).doOnError(this).subscribe());

        // INVITATIONS

        dm.add(getEvents().getInvitations().pastAndNewEvents().flatMapCompletable(invitation -> {
            if (config.autoAcceptChatInvite) {
                return invitation.accept();
            }
            return Completable.complete();
        }).doOnError(this).subscribe());

        // BLOCKED USERS

        dm.add(listChangeOn(Paths.blockedPath()).subscribe(listEvent -> {
            UserEvent ue = UserEvent.from(listEvent);
            if (ue.type == EventType.Added) {
                blocked.add(ue.user);
            }
            if (ue.type == EventType.Removed) {
                blocked.remove(ue.user);
            }
            blockedEvents.onNext(ue);
        }));

        // CONTACTS

        dm.add(listChangeOn(Paths.contactsPath()).subscribe(listEvent -> {
            UserEvent ue = UserEvent.from(listEvent);
            if (ue.type == EventType.Added) {
                contacts.add(ue.user);
            }
            else if (ue.type == EventType.Removed) {
                contacts.remove(ue.user);
            }
            contactEvents.onNext(ue);
        }));

        // CONNECT TO EXISTING GROUP CHATS

        dm.add(listChangeOn(Paths.userGroupChatsPath()).subscribe(listEvent -> {
            ChatEvent chatEvent = ChatEvent.from(listEvent);
            Chat chat = chatEvent.chat;
            if (chatEvent.type == EventType.Added) {
                chat.connect();
                chats.add(chat);
                chatEvents.onNext(chatEvent);
            }
            else if (chatEvent.type == EventType.Removed) {
                dm.add(chat.leave().subscribe(() -> {
                    chats.remove(chat);
                    chatEvents.onNext(chatEvent);
                }, this));
            } else {
                chatEvents.onNext(chatEvent);
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

    public String currentUserId() {
        return user.getUid();
    }

    //
    // Messages
    //

    public Completable deleteSendable (Sendable sendable) {
        return deleteSendable(Paths.messagePath(sendable.id));
    }

    public Completable sendPresence(String userId, PresenceType type) {
        return sendPresence(userId, type, null);
    }

    public Completable sendPresence(String userId, PresenceType type, @Nullable Consumer<String> newId) {
        return send(userId, new Presence(type), newId);
    }


    public Completable sendInvitation(String userId, InvitationType type, String id) {
        return sendInvitation(userId, type, id, null);
    }
    public Completable sendInvitation(String userId, InvitationType type, String groupId, @Nullable Consumer<String> newId) {
        return send(userId, new Invitation(type, groupId), newId);
    }

    public Completable send(String toUserId, Sendable sendable) {
        return send(toUserId, sendable, null);
    }
    public Completable send(String toUserId, Sendable sendable, @Nullable Consumer<String> newId) {
        return send(Paths.messagesPath(toUserId), sendable, newId);
    }

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a message is delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the message
     * @param userId - the recipient user id
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId) {
        return sendDeliveryReceipt(userId, type, messageId, null);
    }
    public Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId) {
        return send(userId, new DeliveryReceipt(type, messageId), newId);
    }

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param userId - the recipient user id
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Completable sendTypingIndicator(String userId, TypingStateType type) {
        return sendTypingIndicator(userId, type, null);
    }
    public Completable sendTypingIndicator(String userId, TypingStateType type, @Nullable Consumer<String> newId) {
        return send(userId, new TypingState(type), newId);
    }


    public Completable sendMessageWithText(String userId, String text) {
        return sendMessageWithText(userId, text, null);
    }
    public Completable sendMessageWithText(String userId, String text, @Nullable Consumer<String> newId) {
        return send(userId, new TextMessage(text), newId);
    }

    public Completable sendMessageWithBody(String userId, HashMap<String, Object> body) {
        return sendMessageWithBody(userId, body, null);
    }
    public Completable sendMessageWithBody(String userId, HashMap<String, Object> body, @Nullable Consumer<String> newId) {
        return send(userId, new Message(body), newId);
    }

    //
    // Blocking
    //

    public Completable block(User user) {
        return addUser(Paths.blockedPath(), User.dateDataProvider(), user);
    }

    public Completable unblock(User user) {
        return removeUser(Paths.blockedPath(), user);
    }

    public ArrayList<User> getBlocked() {
        return blocked;
    }

    public boolean isBlocked(User user) {
        return blocked.contains(user);
    }

    //
    // Contacts
    //

    public Completable addContact(User user, ContactType type) {
        user.contactType = type;
        return addUser(Paths.contactsPath(), User.contactTypeDataProvider(), user);
    }

    public Completable removeContact(User user) {
        return removeUser(Paths.contactsPath(), user);
    }

    public ArrayList<User> getContacts() {
        return contacts;
    }

    //
    // Chats
    //

    public Single<Chat> createChat(String name, String avatarURL, User... users) {
        return createChat(name, avatarURL, Arrays.asList(users));
    }

    public Single<Chat> createChat(String name, String avatarURL, List<User> users) {
        return Chat.create(name, avatarURL, users).flatMap(groupChat -> {
            return joinChat(groupChat.getId()).toSingle(() -> groupChat);
        });
    }

    public Chat getChat(String chatId) {
        for (Chat chat : chats) {
            if (chat.getId().equals(chatId)) {
                return chat;
            }
        }
        return null;
    }

    public Completable leaveChat(String chatId) {
        return getFirebaseService().chat
                .leaveChat(chatId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable joinChat(String chatId) {
        return getFirebaseService().chat
                .joinChat(chatId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public List<Chat> getChats() {
        return chats;
    }

    /**
     * Send a read receipt
     * @return completion
     */
    public Completable markRead(Message message) {
        return Fl.y.sendDeliveryReceipt(message.from, DeliveryReceiptType.read(), message.id);
    }

    /**
     * Send a received receipt
     * @return completion
     */
    public Completable markReceived(Message message) {
        return Fl.y.sendDeliveryReceipt(message.from, DeliveryReceiptType.received(), message.id);
    }

    //
    // Events
    //

    public MultiQueueSubject<ChatEvent> getChatEvents() {
        return chatEvents;
    }

    public MultiQueueSubject<UserEvent> getBlockedEvents() {
        return blockedEvents;
    }

    public MultiQueueSubject<UserEvent> getContactEvents() {
        return contactEvents;
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

    public Observable<ConnectionEvent> getConnectionEvents() {
        return connectionEvents.hide();
    }
}
