package sdk.chat.core.handlers;

import sdk.chat.core.dao.User;
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
     * Get a a user object. This will fetch or create a user object and try to
     * update it from the server. Once it's been updated, the user will be
     * returned by the single
     * @param entityID of user
     * @return user on completion
     */
    Single<User> getUserForEntityID(String entityID);

   /**
    * Get a a user object. This will fetch or create a user object and try to
    * update it from the server. The user will be returned immediately and
    * it will be updated at some point in the future when the server
    * responds. When the user is updated, the client will update anyway
    * because UserMetaUpdated and UserPresenceUpdated network events will be
    * emitted
    * @param entityID of user
    * @return user
    */
    User getUserNowForEntityID(String entityID);

}
