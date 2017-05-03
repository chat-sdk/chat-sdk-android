package co.chatsdk.core.handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.types.AccountType;
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
    public Observable<Void> authenticateWithCachedToken();

    /**
    * Authenticate with Firebase
    */
    //-(RXPromise *) authenticateWithDictionary: (NSDictionary *) details;
    public Observable<Void> authenticateWithMap (final Map<String, Object> details);

    /**
    * Checks whether the user has been authenticated this session
    */
    //-(BOOL) userAuthenticated;
    public Boolean userAuthenticated();

    /**
    * Logout the user from the current account
    */
    //-(RXPromise *) logout;
    public Observable<Void> logout();

    /**
    * Says which networks are available this can be setup in bFirebaseDefines
    * if you set the API key to @"" for Twitter Facebook or Google then it's automatically
    * disabled
    */
    //-(BOOL) accountTypeEnabled: (bAccountType) type;
    public Boolean accountTypeEnabled(AccountType type);

    /**
    * Get the user's stored login credentials
    */
    //-(NSDictionary *) loginInfo;
    public Map<String, Object> loginInfo();

    /**
    * Set the user's stored login credentials
    */
    //-(void) setLoginInfo: (NSDictionary *) info;
    public void setLoginInfo(Map<String, Object> info);

    /**
    * Get the current user's authentication id
    */
    //-(NSString *) currentUserEntityID;
    public String currentUserEntityID();

    // TODO: Implement something like this
    /**
    * The view controller that should be displayed when the user isn't logged in
    */
    //-(UIViewController *) challengeViewController;
    //-(void) setChallengeViewController: (UIViewController *) viewController;
}
