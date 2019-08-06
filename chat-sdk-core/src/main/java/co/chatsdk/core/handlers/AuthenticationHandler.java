package co.chatsdk.core.handlers;

import java.util.Map;

import co.chatsdk.core.types.AccountDetails;
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

    /**
     * Has been authenticated this session
     * @deprecated use {@link #isAuthenticatedThisSession()}
     */
    @Deprecated
    Boolean userAuthenticatedThisSession();
    Boolean isAuthenticatedThisSession();

    /**
    * DidLogout the user from the current account
    */
    Completable logout();

    /**
    * Says which networks are available this can be setup in bFirebaseDefines
    * if you set the API key to @"" for Twitter Facebook or Google then it's automatically
    * disabled
    */
    Boolean accountTypeEnabled(AccountDetails.Type type);

    /**
    * Get the user's stored login credentials
    */
    Map<String, ?> getLoginInfo();

    /**
    * Set the user's stored login credentials
    */
    void setLoginInfo(Map<String, Object> info);

    /**
    * Get the current user's authentication id
    */
    String getCurrentUserEntityID();

    // TODO: Implement something like this
    /**
    * The view controller that should be displayed when the user isn't logged in
    */

    void addLoginInfoData (String key, Object value);

    Completable changePassword(String email, String oldPassword, final String newPassword);
    Completable sendPasswordResetMail(String email);


}
