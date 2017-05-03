package co.chatsdk.core.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import co.chatsdk.core.entities.Message;
import co.chatsdk.core.entities.ThreadType;
import io.reactivex.Observable;
import co.chatsdk.core.entities.User;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface CoreHandler {

    enum bSystemMessageType {
        bSystemMessageTypeInfo(1),
        bSystemMessageTypeError(2);

        private int numVal;

        bSystemMessageType(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }
    }

    /**
     * Update the user on the server
     */
    public Observable<Void> pushUser ();

    /**
    * Return the current user data
    */
    public User currentUserModel();

    // TODO: Consider removing / refactoring this

    /**
    * Mark the user as online
    */
    public void setUserOnline();

    /**
    * Connect to the server
    */
    public void goOffline();

    /**
    * Disconnect from the server
    */
    public void goOnline();

    // TODO: Consider removing / refactoring this
    /**
    * Subscribe to a user's updates
    */
    public void observeUser(String entityID);

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

    //-(id<PUser>) userForEntityID: (NSString *) entityID;

    // TODO: Consider removing this
    /**
    * Core Data doesn't save data to disk automatically. During the programs execution
    * Core Data stores all data chages in memory and when the program terminates these
    * changes are lost. Calling save forces Core Data to persist the data to perminant storage
    */
    //-(void) save;
    public void save();

    //-(void) sendLocalSystemMessageWithText:(NSString *)text withThreadEntityID:(NSString *)threadID;
    public void sendLocalSystemMessageWithTextAndThreadEntityID(String text, String threadID);

    //(final String key, final String field)

    //-(void) sendLocalSystemMessageWithText:(NSString *)text type: (bSystemMessageType) type withThreadEntityID:(NSString *)threadID;
    public void sendLocalSystemMessageWithTextTypeThreadEntityID(String text, bSystemMessageType type, String threadID);
}
