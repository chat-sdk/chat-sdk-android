package wanderingdevelopment.tk.chatsdkcore.Interfaces;

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
     * @brief Update the user on the server
     */
    //-(RXPromise *) pushUser;

    /**
    * @brief Return the current user data
    */
    //-(id<PUser>) currentUserModel;

    // TODO: Consider removing / refactoring this
    /**
    * @brief Mark the user as online
    */
    //-(void) setUserOnline;
    public void setUserOnline();

    /**
    * @brief Connect to the server
    */
    //-(void) goOffline;
    public void goOffline();

    /**
    * @brief Disconnect from the server
    */
    //-(void) goOnline;
    public void goOnline();

    // TODO: Consider removing / refactoring this
    /**
    * @brief Subscribe to a user's updates
    */
    //-(void)observeUser: (NSString *)entityID;
    public void observeUser(String entityID);

    /**
    * @brief This method invites adds the users provided to a a conversation thread
    * Register block to:
    * - Handle thread creation
    */
    //-(RXPromise *) createThreadWithUsers: (NSArray *) users name: (NSString *) name threadCreated: (void(^)(NSError * error, id<PThread> thread)) threadCreated;
    //-(RXPromise *) createThreadWithUsers: (NSArray *) users threadCreated: (void(^)(NSError * error, id<PThread> thread)) thread;


    /**
    * @brief Add users to a thread
    */
    //-(RXPromise *) addUsers: (NSArray<PUser> *) userIDs toThread: (id<PThread>) threadModel;

    /**
    * @brief Remove users from a thread
    */
    //-(RXPromise *) removeUsers: (NSArray<PUser> *) userIDs fromThread: (id<PThread>) threadModel;

    /**
    * @brief Lazy loading of messages this method will load
    * that are not already in memory
    */
    //-(RXPromise *) loadMoreMessagesForThread: (id<PThread>) threadModel;

    /**
    * @brief This method deletes an existing thread. It deletes the thread from memory
    * and removes the user from the thread so the user no longer recieves notifications
    * from the thread
    */
    //-(RXPromise *) deleteThread: (id<PThread>) thread;
    //-(RXPromise *) leaveThread: (id<PThread>) thread;
    //-(RXPromise *) joinThread: (id<PThread>) thread;


    /**
    * @brief Send different types of message to a particular thread
    */
    //-(RXPromise *) sendMessageWithText: (NSString *) text withThreadEntityID: (NSString *) threadID;

    /**
    * @brief Send a message object
    */
    //-(RXPromise *) sendMessage: (id<PMessage>) messageModel;

    // TODO: Consider making this a PThread for consistency
    /**
    * @brief Get the messages for a particular thread
    */
    //-(NSArray<PMessage> *) messagesForThreadWithEntityID:(NSString *) entityID order: (NSComparisonResult) order;

    /**
    * @brief Get a list of all threads
    */
    //-(NSArray<PThread> *) threadsWithType: (bThreadType) type;

    //-(id<PUser>) userForEntityID: (NSString *) entityID;

    // TODO: Consider removing this
    /**
    * @brief Core Data doesn't save data to disk automatically. During the programs execution
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
