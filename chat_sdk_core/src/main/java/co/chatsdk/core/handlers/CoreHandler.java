package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.core.BUser;
import io.reactivex.Completable;
import io.reactivex.Observable;

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
    public Completable pushUser ();

    /**
    * Return the current user data
    */
    public BUser currentUserModel();

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

    // TODO: Consider removing this
    /**
     * Core Data doesn't save data to disk automatically. During the programs execution
     * Core Data stores all data chages in memory and when the program terminates these
     * changes are lost. Calling save forces Core Data to persist the data to perminant storage
     */
    //-(void) save;
    public void save();


}
