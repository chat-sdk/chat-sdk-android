package co.chatsdk.core.handlers;

import java.util.ArrayList;

import co.chatsdk.core.entities.Message;
import co.chatsdk.core.entities.ThreadType;
import co.chatsdk.core.entities.User;
import io.reactivex.Observable;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

public interface ThreadHandler {

    /**
     * This method invites adds the users provided to a a conversation thread
     * Register block to:
     * - Handle thread creation
     */
    public Observable<Thread> createThread (ArrayList<User> users, String name);
    public Observable<Thread> createThread (ArrayList<User> users);

    /**
     * Add users to a thread
     */
    //-(RXPromise *) addUsers: (NSArray<PUser> *) userIDs toThread: (id<PThread>) threadModel;
    public Observable<User> addUsersToThread (ArrayList<User> users, Thread thread);

    /**
     * Remove users from a thread
     */
    //-(RXPromise *) removeUsers: (NSArray<PUser> *) userIDs fromThread: (id<PThread>) threadModel;
    public Observable<User> removeUsersFromThread (ArrayList<User> users, Thread thread);

    /**
     * Lazy loading of messages this method will load
     * that are not already in memory
     */
    //-(RXPromise *) loadMoreMessagesForThread: (id<PThread>) threadModel;
    public Observable<Void> loadMoreMessagesForThread (Thread thread);

    /**
     * This method deletes an existing thread. It deletes the thread from memory
     * and removes the user from the thread so the user no longer recieves notifications
     * from the thread
     */
    //-(RXPromise *) deleteThread: (id<PThread>) thread;
    //-(RXPromise *) leaveThread: (id<PThread>) thread;
    //-(RXPromise *) joinThread: (id<PThread>) thread;
    public Observable<Void> deleteThread (Thread thread);
    public Observable<Void> leaveThread (Thread thread);
    public Observable<Void> joinThread (Thread thread);


    /**
     * Send different types of message to a particular thread
     */
    //-(RXPromise *) sendMessageWithText: (NSString *) text withThreadEntityID: (NSString *) threadID;
    public Observable<Void> sendMessage(String text, String threadID);

    /**
     * Send a message object
     */
    //-(RXPromise *) sendMessage: (id<PMessage>) messageModel;
    public Observable<Void> sendMessage (Message message);

    // TODO: Consider making this a PThread for consistency
    /**
     * Get the messages for a particular thread
     */
    //-(NSArray<PMessage> *) messagesForThreadWithEntityID:(NSString *) entityID order: (NSComparisonResult) order;
    public ArrayList<Message> messagesForThread (String threadID, boolean ascending);

    /**
     * Get a list of all threads
     */
    //-(NSArray<PThread> *) threadsWithType: (bThreadType) type;
    public ArrayList<Thread> threadsWithType (ThreadType type);

    //-(void) sendLocalSystemMessageWithText:(NSString *)text withThreadEntityID:(NSString *)threadID;
    public void sendLocalSystemMessageWithTextAndThreadEntityID(String text, String threadID);

    //-(void) sendLocalSystemMessageWithText:(NSString *)text type: (bSystemMessageType) type withThreadEntityID:(NSString *)threadID;
    public void sendLocalSystemMessageWithTextTypeThreadEntityID(String text, CoreHandler.bSystemMessageType type, String threadID);
}
