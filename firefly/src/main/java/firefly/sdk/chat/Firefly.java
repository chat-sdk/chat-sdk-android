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

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
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

public class Firefly extends AbstractChat {

    public static final Firefly instance = new Firefly();

    protected FirebaseUser user;

    protected ArrayList<User> contacts = new ArrayList<>();
    protected ArrayList<User> blocked = new ArrayList<>();

    protected MultiQueueSubject<ChatEvent> chatEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<UserEvent> contactEvents = MultiQueueSubject.create();
    protected MultiQueueSubject<UserEvent> blockedEvents = MultiQueueSubject.create();

    protected FirebaseService firebaseService = null;
    protected WeakReference<Context> context;

    public static Firefly shared () {
        return instance;
    }

    protected ArrayList<Chat> chats = new ArrayList<>();

    public Firefly() {

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            this.user = firebaseAuth.getCurrentUser();
            if (this.user != null) {
                try {
                    connect();
                } catch (Exception e) {
                    events.publishThrowable().onNext(e);
                }
            } else {
                disconnect();
            }
        });
    }

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

    public void initialize(Context context) {
        initialize(context, null);
    }

    public void connect () throws Exception {
        disconnect();

        if (this.config == null) {
            throw new Exception(context().getString(R.string.error_initialize_not_run));
        }
        if (this.user == null) {
            throw new Exception(context().getString(R.string.error_no_authenticated_user));
        }

        // MESSAGE DELETION

        // We always delete typing state and presence messages
        Observable<Sendable> stream = getEvents().getSendables().pastAndNewEvents();
        if (!config.deleteMessagesOnReceipt) {
            stream = stream.filter(MessageStreamFilter.bySendableType(SendableType.typingState(), SendableType.presence()));
        }
        // If deletion is enabled, we don't filter so we delete all the message types
        dl.add(stream.flatMapCompletable(this::deleteSendable).subscribe());

        // DELIVERY RECEIPTS

        dl.add(getEvents().getMessages().pastAndNewEvents().subscribe(message -> {
            // If delivery receipts are enabled, send the delivery receipt
            if (config.deliveryReceiptsEnabled) {
                dl.add(sendDeliveryReceipt(message.from, DeliveryReceiptType.received(), message.id)
                        .doOnError(Firefly.this)
                        .subscribe());
            }
            // If message deletion is disabled, instead mark the message as received. This means
            // that when we add a listener, we only get new messages
            if (!config.deleteMessagesOnReceipt) {
                dl.add(sendDeliveryReceipt(currentUserId(), DeliveryReceiptType.received(), message.id)
                        .doOnError(Firefly.this)
                        .subscribe());
            }
        }));

        // INVITATIONS

        dl.add(getEvents().getInvitations().pastAndNewEvents().flatMapCompletable(invitation -> {
            if (config.autoAcceptChatInvite) {
                return invitation.accept();
            }
            return Completable.complete();
        }).doOnError(this).subscribe());

        // BLOCKED USERS

        dl.add(listChangeOn(Paths.blockedPath()).subscribe(listEvent -> {
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

        dl.add(listChangeOn(Paths.contactsPath()).subscribe(listEvent -> {
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

        dl.add(listChangeOn(Paths.userGroupChatsPath()).subscribe(listEvent -> {
            ChatEvent chatEvent = ChatEvent.from(listEvent);
            Chat chat = chatEvent.chat;
            if (chatEvent.type == EventType.Added) {
                chat.connect();
                chats.add(chat);
                chatEvents.onNext(chatEvent);
            }
            else if (chatEvent.type == EventType.Removed) {
                dl.add(chat.leave().subscribe(() -> {
                    chats.remove(chat);
                    chatEvents.onNext(chatEvent);
                }, this));
            } else {
                chatEvents.onNext(chatEvent);
            }
        }));

        // Connect to the message events AFTER we have added our events listeners
        super.connect();
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

    public Single<String> sendPresence(String userId, PresenceType type) {
        return send(userId, new Presence(type));
    }

    public Single<String> sendInvitation(String userId, InvitationType type, String groupId) {
        return send(userId, new Invitation(type, groupId));
    }

    public Single<String> send(String toUserId, Sendable sendable) {
        return this.send(Paths.messagesPath(toUserId), sendable);
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
    public Single<String> sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId) {
        return send(userId, new DeliveryReceipt(type, messageId));
    }

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param userId - the recipient user id
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Single<String> sendTypingIndicator(String userId, TypingStateType type) {
        return send(userId, new TypingState(type));
    }

    public Single<String> sendMessageWithText(String userId, String text) {
        return send(userId, new TextMessage(text));
    }

    public Single<String> sendMessageWithBody(String userId, HashMap<String, Object> body) {
        return send(userId, new Message(body));
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

    //
    // Events
    //

    public Observable<ChatEvent> getChatEvents() {
        return chatEvents.hide();
    }

    public Observable<UserEvent> getBlockedEvents() {
        return blockedEvents.hide();
    }

    public Observable<UserEvent> getContactEvents() {
        return contactEvents.hide();
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
}
