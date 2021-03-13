package sdk.chat.core.handlers;

import io.reactivex.Completable;
import sdk.chat.core.dao.User;
import sdk.chat.core.types.AccountDetails;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AuthenticationHandler {

    /**
     * Check to see if the user is already authenticated
     */
    Completable authenticate();

    /**
    * Authenticate with Firebase
    */
    Completable authenticate (AccountDetails details);

    /**
     * Checks whether the user is authenticated
     */
    Boolean isAuthenticated();
    Boolean isAuthenticating();
    Boolean cachedCredentialsAvailable();

    void cancel();

    /**
     * Has been authenticated this session. If they are authenticated this session
     * it means that the server is fully setup and the session is established
     */
    Boolean isAuthenticatedThisSession();

    /**
    * DidLogout the user from the current account
    */
    Completable logout();

    Boolean accountTypeEnabled(AccountDetails.Type type);

    /**
    * Get the current user's authentication id
    */
    String getCurrentUserEntityID();
    User currentUser();

    Completable changePassword(String email, String oldPassword, final String newPassword);
    Completable sendPasswordResetMail(String email);

    void stop();

}
