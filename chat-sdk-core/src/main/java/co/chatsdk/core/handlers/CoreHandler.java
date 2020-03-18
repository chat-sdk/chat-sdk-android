package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.User;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

 public interface CoreHandler {

    /**
     * Update the user on the server
     */
    Completable pushUser();

    /**
    * Return the current user data
    */
    @Deprecated
    User currentUserModel();

    User currentUser();

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

    Completable userOn (User user);
    void userOff (User user);

    void save();

    /**
     * Get a user from Firebase. Note that this will return even if the
     * user doesn't exist on the server... The user will just be empty
     * @param entityID of user
     * @return user on completion
     */
    Single<User> getUserForEntityID(String entityID);

}
