package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.User;
import io.reactivex.Completable;

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
    Completable setUserOnline();

    Completable setUserOffline();

    /**
    * Connect to the server
    */
    void goOffline();

    /**
    * Disconnect from the server
    */
    void goOnline();

    void userOn (User user);
    void userOff (User user);

    void save();

}
