package sdk.chat.core.handlers;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.SystemMessageType;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

public interface ThreadHandler {

    /**
     * The list of users should not contain the current user.
     */
    Single<Thread> createThread(@Nullable String name, List<User> users);
    Single<Thread> createThread(List<User> users);
    Single<Thread> createThread(@Nullable String name, User... users);
    Single<Thread> createThread(@Nullable String name, List<User> users, int type);
    Single<Thread> createThread(@Nullable String name, List<User> users, int type, @Nullable String entityID);
    Single<Thread> createThread(@Nullable String name, List<User> users, int type, @Nullable String entityID, @Nullable String imageURL);
    Single<Thread> createThread(@Nullable String name, List<User> users, int type, @Nullable String entityID, @Nullable String imageURL, @Nullable Map<String, Object> meta);

    Single<Thread> create1to1Thread(User otherUser, @Nullable Map<String, Object> meta);
    Single<Thread> createPrivateGroupThread(@Nullable String name, List<User> users, @Nullable String entityID, @Nullable String imageURL, @Nullable Map<String, Object> meta);

    /**
     * Remove users from a thread
     */
    boolean canRemoveUsersFromThread(Thread thread, List<User> users);
    Completable removeUsersFromThread(Thread thread, List<User> users);
    Completable removeUsersFromThread(Thread thread, User... users);
    /**
     * Add users to a thread
     */

    boolean canAddUsersToThread(Thread thread);
    Completable addUsersToThread(Thread thread, List<User> users);
    Completable addUsersToThread(Thread thread, User... users);
    /**
     * Lazy loading of messages this method will load
     * that are not already in memory
     */
    Single<List<Message>> loadMoreMessagesBefore(Thread thread, @Nullable Date before, boolean loadFromServer);
    Single<List<Message>> loadMoreMessagesBefore(Thread thread, @Nullable Date before);
    Single<List<Message>> loadMoreMessagesAfter(Thread thread, @Nullable Date after, boolean loadFromServer);

    boolean canEditThreadDetails(Thread thread);

        /**
         * This method deletes an existing thread. It deletes the thread from memory
         * and removes the user from the thread so the user no longer recieves notifications
         * from the thread
         */
    Completable deleteThread(Thread thread);

    boolean canLeaveThread(Thread thread);
    Completable leaveThread(Thread thread);

    Completable joinThread(Thread thread);

    Completable deleteMessage(Message message);
    Completable deleteMessages(Message... messages);
    Completable deleteMessages(List<Message> messages);
    boolean canDeleteMessage(Message message);

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

    Single<Integer> getUnreadMessagesAmount(boolean onePerThread);

    // TODO: Consider making this a PThread for consistency
    /**
     * Get the messages for a particular thread
     */
    //List<Message> messagesForThread (String threadID, boolean ascending);

    /**
     * Get a list of all threads
     */
    List<Thread> getThreads(int type, boolean allowDeleted);
    List<Thread> getThreads(int type);

    void sendLocalSystemMessage(String text, Thread thread);
    void sendLocalSystemMessage(String text, SystemMessageType type, Thread thread);

    Completable pushThread(Thread thread);
    Completable pushThreadMeta(Thread thread);

    // Muting notifications
    boolean muteEnabled(Thread thread);
    Completable mute(Thread thread);
    Completable unmute(Thread thread);

    Message newMessage(int type, Thread thread);

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

    List<String> localizeRoles(List<String> roles);
    String localizeRole(String role);
    List<String> localizeRoles(String... roles);

    // Moderation
    Completable grantVoice(Thread thread, User user);
    Completable revokeVoice(Thread thread, User user);
    boolean canChangeVoice(Thread thread, User user);
    boolean hasVoice(Thread thread, User user);

    Completable grantModerator(Thread thread, User user);
    Completable revokeModerator(Thread thread, User user);
    boolean canChangeModerator(Thread thread, User user);
    boolean isModerator(Thread thread, User user);

    boolean isBanned(Thread thread, User user);

}

