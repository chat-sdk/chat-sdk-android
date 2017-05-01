package wanderingdevelopment.tk.chatsdkcore.Interfaces;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AuthenticationHandler {

    enum bAccountType {
        bAccountTypePassword(1),
        bAccountTypeFacebook(2),
        bAccountTypeTwitter(3),
        bAccountTypeAnonymous(4),
        bAccountTypeGoogle(5),
        bAccountTypeCustom(6),
        bAccountTypeRegister(99);

        private int numVal;

        bAccountType(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }
    }

    //public Promise<Void, Void, Void> sendMessage (data data, double duration, String threadID)

    /**
     * @brief Check to see if the user is already authenticated
     */
    //-(RXPromise *) authenticateWithCachedToken;

    /**
    * @brief Authenticate with Firebase
    */
    //-(RXPromise *) authenticateWithDictionary: (NSDictionary *) details;

    /**
    * @brief Checks whether the user has been authenticated this session
    */
    //-(BOOL) userAuthenticated;
    public Boolean userAuthenticated();

    /**
    * @brief Logout the user from the current account
    */
    //-(RXPromise *) logout;

    /**
    * @brief Says which networks are available this can be setup in bFirebaseDefines
    * if you set the API key to @"" for Twitter Facebook or Google then it's automatically
    * disabled
    */
    //-(BOOL) accountTypeEnabled: (bAccountType) type;
    public Boolean accountTypeEnabled(bAccountType type);

    /**
    * @brief Get the user's stored login credentials
    */
    //-(NSDictionary *) loginInfo;

    /**
    * @brief Set the user's stored login credentials
    */
    //-(void) setLoginInfo: (NSDictionary *) info;

    /**
    * @brief Get the current user's authentication id
    */
    //-(NSString *) currentUserEntityID;
    public String currentUserEntityID();

    /**
    * @brief The view controller that should be displayed when the user isn't logged in
    */
    //-(UIViewController *) challengeViewController;
    //-(void) setChallengeViewController: (UIViewController *) viewController;
}
