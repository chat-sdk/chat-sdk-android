package sdk.chat.core.handlers;

import sdk.chat.core.types.AccountDetails;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AuthenticationHandler {

    /**
     * Check to see if the user is already authenticated
     * @deprecated use {@link #authenticate()}
     */
    @Deprecated
    Completable authenticateWithCachedToken();
    Completable authenticate();

    /**
    * Authenticate with Firebase
    */
    Completable authenticate (AccountDetails details);

    /**
     * Checks whether the user is authenticated
     * @deprecated use {@link #isAuthenticated()}
     */
    @Deprecated
    Boolean userAuthenticated();
    Boolean isAuthenticated();
    Boolean isAuthenticating();

    void cancel();

    /**
     * Has been authenticated this session. If they are authenticated this session
     * it means that the server is fully setup and the session is established
     * @deprecated use {@link #isAuthenticatedThisSession()}
     */
    @Deprecated
    Boolean userAuthenticatedThisSession();
    Boolean isAuthenticatedThisSession();

    /**
    * DidLogout the user from the current account
    */
    Completable logout();

    Boolean accountTypeEnabled(AccountDetails.Type type);

    /**
    * Get the user's stored login credentials
    */
    String getSavedCurrentUserEntityID();

    /**
    * Set the user's stored login credentials
    */
    void saveCurrentUserEntityID(String userEntityID);

    /**
    * Get the current user's authentication id
    */
    String getCurrentUserEntityID();

    Completable changePassword(String email, String oldPassword, final String newPassword);
    Completable sendPasswordResetMail(String email);

}
