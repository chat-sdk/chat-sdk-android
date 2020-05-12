package firestream.chat.interfaces;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import firestream.chat.chat.User;
import firestream.chat.firebase.rx.MultiQueueSubject;
import firestream.chat.message.Sendable;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.RoleType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import sdk.guru.common.Event;

/**
 * This interface type just provided for clarity
 */
public interface IChat extends IAbstractChat {

    /**
     * The unique chat id
     * @return id string
     */
    String getId();

    /**
     * Remove the user from the chat's roster. It may be preferable to call
     * @see IFireStream#leaveChat(IChat)
     * @return completion
     */
    Completable leave();

    /**
     * Get the chat name
     * @return name
     */
    String getName();

    /**
     * Set the chat name.
     * @param name new name
     * @return completion
     */
    Completable setName(String name);

    /**
     * Get the group image url
     * @return image url
     */
    String getImageURL();

    /**
     * Set the chat image url
     * @param url of group image
     * @return completion
     */
    Completable setImageURL(String url);

    /**
     * Get any custom data associated from the chat
     * @return custom data
     */
    HashMap<String, Object> getCustomData();

    /**
     * Associate custom data from the chat - you can add your own
     * data to a chat - topic, extra links etc...
     * @param data custom data to write
     * @return completion
     */
    Completable setCustomData(final HashMap<String, Object> data);

    /**
     * Get a list of members of the chat
     * @return list of users
     */
    ArrayList<User> getUsers();

    /**
     * Get a list of users from the FireStreamUser namespace
     * These are exactly the same users but may be useful if
     * your project already has a User class to avoid a clash
     * @return list of FireStreamUsers
     */
    ArrayList<FireStreamUser> getFireStreamUsers();

    /**
     * Add users to a chat
     * @param sendInvite should an invitation message be sent?
     * @param users users to add, set the role of each user using user.setRoleType()
     * @return completion
     */
    Completable addUsers(Boolean sendInvite, User... users);

    /**
     * @see IChat#addUsers(Boolean, User...)
     */
    Completable addUsers(Boolean sendInvite, List<? extends User> users);

    /**
     * @see IChat#addUsers(Boolean, User...)
     */
    Completable addUser(Boolean sendInvite, User user);

    /**
     * Update users in chat
     * @param users users to update
     * @return completion
     */
    Completable updateUsers(User... users);

    /**
     * @see IChat#updateUsers(User...)
     */
    Completable updateUsers(List<? extends User> users);

    /**
     * @see IChat#updateUsers(User...)
     */
    Completable updateUser(User user);

    /**
     * Remove users from a chat
     * @param users users to remove
     * @return completion
     */
    Completable removeUsers(User... users);

    /**
     * @see IChat#removeUsers(User...)
     */
    Completable removeUsers(List<? extends User> users);

    /**
     * @see IChat#removeUsers(User...)
     */
    Completable removeUser(User user);

    /**
     * Send an invite message to users
     * @param users to invite
     * @return completion
     */
    Completable inviteUsers(List<? extends User> users);

    /**
     * Set the role of a user
     * @param user to update
     * @param roleType new role type
     * @return completion
     */
    Completable setRole(User user, RoleType roleType);

    /**
     * Get the users for a particular role
     * @param roleType to find
     * @return list of users
     */
    List<User> getUsersForRoleType(RoleType roleType);

    /**
     * Get the role for a user
     * @param theUser to who's role to find
     * @return role
     */
    RoleType getRoleType(User theUser);

    /**
     * Get the role for the current user
     * @return role
     */
    RoleType getMyRoleType();

    /**
     * Get a list of roles that this user could be changed to. This will vary
     * depending on our own role level
     * @param user to test
     * @return list of roles
     */
    List<RoleType> getAvailableRoles(User user);

    /**
     * Test to see if the current user has the required permission
     * @param required permission
     * @return true / false
     */
    boolean hasPermission(RoleType required);

    /**
     * Get an observable which type called when the name changes
     * @return observable
     */
    Observable<String> getNameChangeEvents();

    /**
     * Get an observable which type called when the chat image changes
     * @return observable
     */
    Observable<String> getImageURLChangeEvents();

    /**
     * Get an observable which type called when the custom data associated from the
     * chat type updated
     * @return observable
     */
    Observable<HashMap<String, Object>> getCustomDataChangedEvents();

    /**
     * Get an observable which type called when the a user type added, removed or updated
     * @return observable
     */
    MultiQueueSubject<Event<User>> getUserEvents();

    /**
     * Send a custom message
     * @param body custom message data
     * @param newId message's new ID before sending
     * @return completion
     */
    Completable sendMessageWithBody(HashMap<String, Object> body, @Nullable Consumer<String> newId);

    /**
     * Send a custom message
     * @param body custom message data
     * @return completion
     */
    Completable sendMessageWithBody(HashMap<String, Object> body);

    /**
     * Send a text message
     * @param text message text
     * @param newId message's new ID before sending
     * @return completion
     */
    Completable sendMessageWithText(String text, @Nullable Consumer<String> newId);

    /**
     * Send a text message
     * @param text message text
     * @return completion
     */
    Completable sendMessageWithText(String text);

    /**
     * Send a custom sendable
     * @param sendable to send
     * @param newId message's new ID before sending
     * @return completion
     */
    Completable send(Sendable sendable, @Nullable Consumer<String> newId);

    /**
     * Send a custom sendable
     * @param sendable to send
     * @return completion
     */
    Completable send(Sendable sendable);

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
    Completable deleteSendable(String sendableId);

    /**
     * Mute notifications for a user
     * @return completion
     */
    Completable mute();

    /**
     * Mute notifications for a user
     * @param until mute the thread until this date
     * @return completion
     */
    Completable mute(Date until);

    /**
     * Unmute notifications for a user
     * @return completion
     */
    Completable unmute();

    /**
     * Is a user muted?
     * @return true / false
     */
    boolean muted();

    /**
     * Thread is muted until this date
     * @return date or null if not muted
     */
    Date mutedUntil();
}
