package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.User;
import io.reactivex.Completable;
import io.reactivex.Single;

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

         int getNumVal() {
            return numVal;
        }
    }

    /**
     * Update the user on the server
     */
    Completable pushUser ();

    /**
    * Return the current user data
    */
    User currentUserModel();

    /**
    * Mark the user as online
    */
    void setUserOnline();

    void setUserOffline();

    /**
    * Connect to the server
    */
    void goOffline();

    /**
    * Disconnect from the server
    */
    void goOnline();

    Single<Boolean> isOnline();

    // TODO: Consider removing / refactoring this
    /**
    * Subscribe to a user's updates
    */
    void observeUser(String entityID);

    // TODO: Consider removing this
    /**
     * Core Data doesn't save data to disk automatically. During the programs execution
     * Core Data stores all data chages in memory and when the program terminates these
     * changes are lost. Calling save forces Core Data to persist the data to perminant storage
     */
    void save();

}
