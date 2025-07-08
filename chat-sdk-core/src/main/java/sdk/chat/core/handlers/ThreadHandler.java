package sdk.chat.core.handlers;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.SystemMessageType;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

public interface ThreadHandler {

    /**
     * The list of users should not contain the current user.
     */
    Single<ThreadX> createThread(@Nullable String name, List<User> users);
    Single<ThreadX> createThread(List<User> users);
    Single<ThreadX> createThread(@Nullable String name, User... users);
    Single<ThreadX> createThread(@Nullable String name, List<User> users, int type);
    Single<ThreadX> createThread(@Nullable String name, List<User> users, int type, @Nullable String entityID);
    Single<ThreadX> createThread(@Nullable String name, List<User> users, int type, @Nullable String entityID, @Nullable String imageURL);
    Single<ThreadX> createThread(@Nullable String name, List<User> users, int type, @Nullable String entityID, @Nullable String imageURL, @Nullable Map<String, Object> meta);

    Single<ThreadX> create1to1Thread(User otherUser, @Nullable Map<String, Object> meta);
    Single<ThreadX> createPrivateGroupThread(@Nullable String name, List<User> users, @Nullable String entityID, @Nullable String imageURL, @Nullable Map<String, Object> meta);

    /**
     * Remove users from a thread
     */
    boolean canRemoveUsersFromThread(ThreadX thread, List<User> users);
    boolean canRemoveUserFromThread(ThreadX thread, User user);
    Completable removeUsersFromThread(ThreadX thread, List<User> users);
    Completable removeUsersFromThread(ThreadX thread, User... users);
    /**
     * Add users to a thread
     */

    boolean canAddUsersToThread(ThreadX thread);
    Completable addUsersToThread(ThreadX thread, List<User> users);
    Completable addUsersToThread(ThreadX thread, User... users);
    /**
     * Lazy loading of messages this method will load
     * that are not already in memory
     */
    Single<List<Message>> loadMoreMessagesBefore(ThreadX thread, @Nullable Date before, boolean loadFromServer);
    Single<List<Message>> loadMoreMessagesBefore(ThreadX thread, @Nullable Date before);
    Single<List<Message>> loadMoreMessagesAfter(ThreadX thread, @Nullable Date after, boolean loadFromServer);

    boolean canEditThreadDetails(ThreadX thread);

        /**
         * This method deletes an existing thread. It deletes the thread from memory
         * and removes the user from the thread so the user no longer recieves notifications
         * from the thread
         */
    Completable deleteThread(ThreadX thread);

    boolean canLeaveThread(ThreadX thread);
    Completable leaveThread(ThreadX thread);

    Completable joinThread(ThreadX thread);
    boolean canJoinThread(ThreadX thread);

    Completable deleteMessage(Message message);
    Completable deleteMessages(Message... messages);
    Completable deleteMessages(List<Message> messages);
    boolean canDeleteMessage(Message message);

    /**
     * Send different types of text to a particular thread
     */
    Completable sendMessageWithText(String text, ThreadX thread);

        /**
         * Send a text object
         */
    Completable sendMessage(Message message);
    Completable forwardMessage(ThreadX thread, Message message);
    Completable forwardMessages(ThreadX thread, Message... messages);
    Completable forwardMessages(ThreadX thread, List<Message> messages);

    Completable replyToMessage(ThreadX thread, Message message, String reply);

    Single<Integer> getUnreadMessagesAmount(boolean onePerThread);

    // TODO: Consider making this a PThread for consistency
    /**
     * Get the messages for a particular thread
     */
    //List<Message> messagesForThread (String threadID, boolean ascending);

    /**
     * Get a list of all threads
     */
    List<ThreadX> getThreads(int type, boolean allowDeleted);
    List<ThreadX> getThreads(int type);

    void sendLocalSystemMessage(String text, ThreadX thread);
    void sendLocalSystemMessage(String text, SystemMessageType type, ThreadX thread);

    Completable pushThread(ThreadX thread);
    Completable pushThreadMeta(ThreadX thread);

    // Muting notifications
    boolean muteEnabled(ThreadX thread);
    Completable mute(ThreadX thread);
    Completable unmute(ThreadX thread);

    Message newMessage(int type, ThreadX thread, boolean notify);

    boolean canDestroy(ThreadX thread);
    Completable destroy(ThreadX thread);

    // Roles
    // Generally it works like this:
    // Owner can grant ownership, set admins
    // Admins can grant moderator, add / remove user
    // Users can chat
    boolean rolesEnabled(ThreadX thread);
    boolean canChangeRole(ThreadX thread, User user);
    String roleForUser(ThreadX thread, User user);
    Completable setRole(String role, ThreadX thread, User user);
    List<String> availableRoles(ThreadX thread, User user);

    List<String> localizeRoles(List<String> roles);
    String localizeRole(String role);
    List<String> localizeRoles(String... roles);

    // Moderation
    Completable grantVoice(ThreadX thread, User user);
    Completable revokeVoice(ThreadX thread, User user);
    boolean canChangeVoice(ThreadX thread, User user);
    boolean hasVoice(ThreadX thread, User user);

    Completable grantModerator(ThreadX thread, User user);
    Completable revokeModerator(ThreadX thread, User user);
    boolean canChangeModerator(ThreadX thread, User user);
    boolean isModerator(ThreadX thread, User user);

    boolean canRefreshRoles(ThreadX thread);
    Completable refreshRoles(ThreadX thread);

    boolean isBanned(ThreadX thread, User user);

    boolean isActive(ThreadX thread, User user);
    String generateNewMessageID(ThreadX thread);

    String readableEntityId(String entityID);
}

