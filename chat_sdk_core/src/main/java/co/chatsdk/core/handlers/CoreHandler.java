package co.chatsdk.core.handlers;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

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

    void presenceMonitoringOn (User user);
    void presenceMonitoringOff (User user);

    void save();

}
