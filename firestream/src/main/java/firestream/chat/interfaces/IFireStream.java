package firestream.chat.interfaces;

import android.content.Context;

import java.util.Date;
import java.util.List;
import java.util.Map;

import firestream.chat.FirestreamConfig;
import firestream.chat.chat.Chat;
import firestream.chat.chat.User;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.firebase.rx.MultiRelay;
import firestream.chat.firebase.service.FirebaseService;
import firestream.chat.message.Body;
import firestream.chat.message.Sendable;
import firestream.chat.types.ContactType;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.InvitationType;
import firestream.chat.types.PresenceType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import sdk.guru.common.Event;

public interface IFireStream extends IAbstractChat {

    /**
     *
     * @param context
     * @param config
     * @param service - Firestore or Realtime service
     */
    void initialize(Context context, @Nullable FirestreamConfig config, FirebaseService service);
    void initialize(Context context, FirebaseService service);
    boolean isInitialized();

    /**
     * @return authenticated user
     */
    User currentUser();

    /**
     * @return id of authenticated user
     */
    String currentUserId();

    // Messages

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a message type delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the message
     * @param userId - the recipient user id
     * @param type - the status getTypingStateType
     * @return - subscribe to get a completion, error update from the method
     */
    Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId);
    Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId);

    Completable sendInvitation(String userId, InvitationType type, String id);
    Completable sendInvitation(String userId, InvitationType type, String groupId, @Nullable Consumer<String> newId);

    Completable send(String toUserId, Sendable sendable);
    Completable send(String toUserId, Sendable sendable, @Nullable Consumer<String> newId);

    /**
     * Messages can always be deleted locally. Messages can only be deleted remotely
     * for recent messages. Specifically, when the client connects, it will add a
     * message listener to get an update for "new" messages. By default, we listen
     * to messages that were added after we last sent a message or a received delivery
     * receipt. This is the dateOfLastDeliveryReceipt. A client will only pick up
     * remote delivery receipts if the date of delivery is after this date.
     * @param sendable to be deleted
     * @return completion
     */
    Completable deleteSendable(Sendable sendable);
    Completable deleteSendable(String toUserId, Sendable sendable);
    Completable deleteSendable(String sendableId);
    Completable deleteSendable(String toUserId, String sendableId);

    Completable sendPresence(String userId, PresenceType type);
    Completable sendPresence(String userId, PresenceType type, @Nullable Consumer<String> newId);

    Completable sendMessageWithText(String userId, String text);
    Completable sendMessageWithText(String userId, String text, @Nullable Consumer<String> newId);

    Completable sendMessageWithBody(String userId, Body body);
    Completable sendMessageWithBody(String userId, Body body, @Nullable Consumer<String> newId);

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param userId - the recipient user id
     * @return - subscribe to get a completion, error update from the method
     */
    Completable startTyping(String userId);

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param userId - the recipient user id
     * @return - subscribe to get a completion, error update from the method
     */
    Completable stopTyping(String userId);
    // Blocked

    Completable block(User user);
    Completable unblock(User user);
    List<User> getBlocked();
    boolean isBlocked(User user);

    // Contacts

    Completable addContact(User user, ContactType type);
    Completable removeContact(User user);
    List<User> getContacts();

    // Chats

    Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, User... users);
    Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, @Nullable Map<String, Object> customData, User... users);
    Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, List<? extends User> users);
    Single<Chat> createChat(@Nullable String name, @Nullable String imageURL, @Nullable Map<String, Object> customData, List<? extends User> users);

    /**
     * Leave the chat. When you leave, you will be removed from the
     * chat's roster
     * @param chat to leave
     * @return completion
     */
    Completable leaveChat(IChat chat);

    /**
     * Join the chat. To join you must already be in the chat roster
     * @param chat to join
     * @return completion
     */
    Completable joinChat(IChat chat);

    IChat getChat(String chatId);
    List<IChat> getChats();

    // Events

    MultiRelay<Event<Chat>> getChatEvents();
    MultiRelay<Event<User>> getBlockedEvents();
    MultiRelay<Event<User>> getContactEvents();
    Observable<ConnectionEvent> getConnectionEvents();

    Completable markReceived(String fromUserId, String sendableId);
    Completable markRead(String fromUserId, String sendableId);

    /**
     * If you set the
     * @param filter
     */
    void setMarkReceivedFilter(Predicate<Event<? extends Sendable>> filter);

    /**
     * Mute notifications for a user
     * @param user to mute
     * @return completion
     */
    Completable mute(User user);

    /**
     * Mute notifications until a future date
     * @param user to mute
     * @param until to mute until
     * @return completion
     */
    Completable mute(User user, Date until);

    /**
     * Unmute notifications for a user
     * @param user to unmute
     * @return completion
     */
    Completable unmute(User user);

    /**
     * Use this method to find out if the user is muted and until when
     * @param user to check
     * @return date or null if not muted
     */
    Date mutedUntil(User user);

    /**
     * Is a user muted?
     * @param user to mute
     * @return true / false
     */
    boolean muted(User user);

    /**
     * Get the current FireStream configuration
     * @return
     */
    FirestreamConfig getConfig();

    /**
     * Get the current Firebase service
     * @return
     */
    FirebaseService getFirebaseService();
}
