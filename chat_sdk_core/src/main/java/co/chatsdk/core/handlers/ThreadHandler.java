package co.chatsdk.core.handlers;

import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
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
    /**
     * Remove users from a thread
     */
    Completable removeUsersFromThread(Thread thread, List<User> users);
    Completable removeUsersFromThread(Thread thread, User... users);
    /**
     * Add users to a thread
     */
    Completable addUsersToThread(Thread thread, List<User> users);
    Completable addUsersToThread(Thread thread, User... users);
    /**
     * Lazy loading of messages this method will load
     * that are not already in memory
     */
    Single<List<Message>> loadMoreMessagesForThread(Message fromMessage, Thread thread);

    /**
     * This method deletes an existing thread. It deletes the thread from memory
     * and removes the user from the thread so the user no longer recieves notifications
     * from the thread
     */
    Completable deleteThread(Thread thread);
    Completable leaveThread (Thread thread);
    Completable joinThread (Thread thread);

    /**
     * Send different types of message to a particular thread
     */
    Observable<MessageSendProgress> sendMessageWithText(String text, Thread thread);
    /**
     * Send a message object
     */
    Observable<MessageSendProgress> sendMessage(Message message);

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
    void sendLocalSystemMessage(String text, CoreHandler.bSystemMessageType type, Thread thread);

    Completable pushThread(Thread thread);
}
