package co.chatsdk.core.handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.types.AccountType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.internal.operators.observable.ObservableElementAt;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AuthenticationHandler {

    /**
     * Check to see if the user is already authenticated
     */
    //-(RXPromise *) authenticateWithCachedToken;
    public Completable authenticateWithCachedToken();

    /**
    * Authenticate with Firebase
    */
    //-(RXPromise *) authenticateWithDictionary: (NSDictionary *) details;
    public Completable authenticateWithMap (final Map<String, Object> details);

    /**
    * Checks whether the user has been authenticated this session
    */
    public Boolean userAuthenticated();

    /**
    * Logout the user from the current account
    */
    public Completable logout();

    /**
    * Says which networks are available this can be setup in bFirebaseDefines
    * if you set the API key to @"" for Twitter Facebook or Google then it's automatically
    * disabled
    */
    public Boolean accountTypeEnabled(int type);

    /**
    * Get the user's stored login credentials
    */
    public Map<String, ?> getLoginInfo();

    /**
    * Set the user's stored login credentials
    */
    public void setLoginInfo(Map<String, Object> info);


    /**
    * Get the current user's authentication id
    */
    public String getCurrentUserEntityID();

    // TODO: Implement something like this
    /**
    * The view controller that should be displayed when the user isn't logged in
    */

    public Completable changePassword(String email, String oldPassword, final String newPassword);
    public Completable sendPasswordResetMail(String email);
}
