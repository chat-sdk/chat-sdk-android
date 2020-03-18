package co.chatsdk.core.handlers;

import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.SystemMessageType;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

public interface ThreadHandler {

    /**
     * The list of users should not contain the current user.
     */
    Single<Thread> createThread(String name, List<User> users);
    Single<Thread> createThread(List<User> users);
    Single<Thread> createThread(String name, User... users);
    Single<Thread> createThread(String name, List<User> users, int type);
    Single<Thread> createThread(String name, List<User> users, int type, String entityID);
    Single<Thread> createThread(String name, List<User> users, int type, String entityID, String imageURL);
    /**
     * Remove users from a thread
     */
    boolean removeUsersEnabled(Thread thread);
    Completable removeUsersFromThread(Thread thread, List<User> users);
    Completable removeUsersFromThread(Thread thread, User... users);
    /**
     * Add users to a thread
     */
    boolean addUsersEnabled(Thread thread);
    Completable addUsersToThread(Thread thread, List<User> users);
    Completable addUsersToThread(Thread thread, User... users);
    /**
     * Lazy loading of messages this method will load
     * that are not already in memory
     */
    Single<List<Message>> loadMoreMessagesForThread(Date fromDate, Thread thread, boolean loadFromServer);
    Single<List<Message>> loadMoreMessagesForThread(Date fromDate, Thread thread);

    /**
     * This method deletes an existing thread. It deletes the thread from memory
     * and removes the user from the thread so the user no longer recieves notifications
     * from the thread
     */
    Completable deleteThread(Thread thread);
    Completable leaveThread (Thread thread);
    Completable joinThread (Thread thread);

    Completable deleteMessage (Message message);
    Completable deleteMessages (Message... messages);
    Completable deleteMessages (List<Message> messages);
    boolean deleteMessageEnabled (Message message);

    /**
     * Send different types of text to a particular thread
     */
    Completable sendMessageWithText(String text, Thread thread);
    /**
     * Send a text object
     */
    Completable sendMessage(Message message);
    Completable forwardMessage(Thread thread, Message message);
    Completable forwardMessages(Thread thread, Message... messages);
    Completable forwardMessages(Thread thread, List<Message> messages);

    Completable replyToMessage(Thread thread, Message message, String reply);

    int getUnreadMessagesAmount(boolean onePerThread);

    // TODO: Consider making this a PThread for consistency
    /**
     * Get the messages for a particular thread
     */
    //List<Message> messagesForThread (String threadID, boolean ascending);

    /**
     * Get a list of all threads
     */
    List<Thread> getThreads (int type, boolean allowDeleted);
    List<Thread> getThreads (int type);

    void sendLocalSystemMessage(String text, Thread thread);
    void sendLocalSystemMessage(String text, SystemMessageType type, Thread thread);

    Completable pushThread(Thread thread);
    Completable pushThreadMeta(Thread thread);

    // Muting notifications
    boolean muteEnabled(Thread thread);
    Completable mute(Thread thread);
    Completable unmute(Thread thread);

    // Roles
    // Generally it works like this:
    // Owner can grant ownership, set admins
    // Admins can grant moderator, add / remove user
    // Users can chat
    boolean rolesEnabled(Thread thread);
    boolean canChangeRole(Thread thread, User user);
    String roleForUser(Thread thread, User user);
    Completable setRole(String role, Thread thread, User user);
    List<String> availableRoles(Thread thread, User user);

    // Moderation
    Completable grantVoice(Thread thread, User user);
    Completable revokeVoice(Thread thread, User user);
    boolean canChangeVoice(Thread thread, User user);
    boolean hasVoice(Thread thread, User user);

    Completable grantModerator(Thread thread, User user);
    Completable revokeModerator(Thread thread, User user);
    boolean canChangeModerator(Thread thread, User user);
    boolean isModerator(Thread thread, User user);

}

