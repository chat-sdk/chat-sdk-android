package com.braunster.chatsdk.network.firebase;

import android.content.Context;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.MainActivity;
import com.braunster.chatsdk.dao.BLinkData;
import com.braunster.chatsdk.dao.BLinkDataDao;
import com.braunster.chatsdk.dao.BLinkedAccount;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BMessageEntity;
import com.braunster.chatsdk.dao.entities.BThreadEntity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListener;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.TwitterManager;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.parse.PushUtils;
import com.bugsense.trace.BugSenseHandler;
import com.facebook.Session;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.firebase.simplelogin.User;
import com.firebase.simplelogin.enums.Error;
import com.firebase.simplelogin.enums.Provider;
import com.parse.PushService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.braunster.chatsdk.network.BDefines.BAccountType.Anonymous;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Facebook;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Password;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Register;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Twitter;
import static com.braunster.chatsdk.network.BDefines.Keys;
import static com.braunster.chatsdk.network.BDefines.Prefs;

/**
 * Created by braunster on 23/06/14.
 */
public class BFirebaseNetworkAdapter extends AbstractNetworkAdapter {

    private static final String TAG = BFirebaseNetworkAdapter.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    private Context context;

    public BFirebaseNetworkAdapter(Context context){
        this.context = context;
    }


    //Note this is added so there wont be two auth processes running simultaneously.
    private enum AuthStatus{
        IDLE {
            @Override
            public String toString() {
                return "Idle";
            }
        },
        AUTH_WITH_MAP{
            @Override
            public String toString() {
                return "Auth with map";
            }
        },
        HANDLING_F_USER{
            @Override
            public String toString() {
                return "Handling F user";
            }
        },
        UPDATING_USER{
            @Override
            public String toString() {
                return "Updating user";
            }
        },
        PUSHING_USER{
            @Override
            public String toString() {
                return "Pushing user";
            }
        },
        CHECKING_IF_AUTH{
            @Override
            public String toString() {
                return "Checking if Authenticated";
            }
        }
    }

    private AuthStatus authingStatus = AuthStatus.IDLE;

    public AuthStatus getAuthingStatus() {
        return authingStatus;
    }

    public boolean isAuthing(){
        return authingStatus != AuthStatus.IDLE;
    }

    private void resetAuth(){
        authingStatus = AuthStatus.IDLE;
    }

    // NOTE Not implemented yet. Will be in the future.
/*    -(void) loginWithFacebookWithCompletion: (void(^)(NSError * error, FAUser * user)) completion {
        [[BFacebookManager sharedManager] loginWithCompletion:^(NSError * error) {

            Firebase * ref = [Firebase firebaseRef];
            FirebaseSimpleLogin * auth = [[FirebaseSimpleLogin alloc] initWithRef:ref];

            [auth createFacebookUserWithToken:[BFacebookManager sharedManager].accessToken
            appId:bFacebookAppID
            withCompletionBlock:completion];

        }];
    }*/

   /* -(void) getUserFacebookFriendListWithCompletion: (void(^)(NSError * error, NSArray * friends)) completion {
        // Check to see if the user has a facebook network associated
        BUser * currentUser = (BUser *) [self currentUser];
        BUserAccount * account = [currentUser accountWithType:bAccountTypeFacebook];

        if (account && account.token) {

            [BFacebookManager sharedManager].accessToken = account.token;
            [[BFacebookManager sharedManager] getUserFriendListWithCompletion:^(NSError * error, NSArray * friends) {

                if (friends.count) {
                    BUser * currentUser = self.currentUser;

                    // Add the users to contacts
                    for (BUser * user in friends) {

                        [currentUser addContact:user];
                    }
                }

                completion(error, friends);
            }];
        }
        else {
            completion(Nil, @[]);
        }
    }
*/
    public void test(){

    }

    @Override
    public void authenticateWithMap(Map<String, Object> details, final CompletionListenerWithDataAndError<User, Object> listener) {
        if (DEBUG) Log.v(TAG, "authenticateWithMap, KeyType: " + details.get(Prefs.LoginTypeKey));

        if (isAuthing())
        {
            if (DEBUG) Log.d(TAG, "Already Authing!, Status: " + authingStatus.name());
            return;
        }

        authingStatus = AuthStatus.AUTH_WITH_MAP;

        Firebase ref = FirebasePaths.firebaseRef();
        SimpleLogin simpleLogin = new SimpleLogin(ref, context);

        SimpleLoginAuthenticatedHandler handler = new SimpleLoginAuthenticatedHandler() {
            @Override
            public void authenticated(Error error, final User firebaseSimpleLoginUser) {
                if (error != null || firebaseSimpleLoginUser == null)
                {
                    if (DEBUG) Log.e(TAG, "Error login in, Name: " + error.name());
                    resetAuth();
                    listener.onDoneWithError(firebaseSimpleLoginUser, error);
                }
                else handleFAUser(firebaseSimpleLoginUser, new CompletionListenerWithDataAndError<BUser, Object>() {
                    @Override
                    public void onDone(BUser user) {
                        resetAuth();
                        listener.onDone(firebaseSimpleLoginUser);
                    }

                    @Override
                    public void onDoneWithError(BUser bUser, Object o) {
                        listener.onDoneWithError(null, o);
                        resetAuth();
                    }
                });
            }
        };

        switch ((Integer)details.get(Prefs.LoginTypeKey))
        {
            case Facebook:
                if (DEBUG) Log.d(TAG, "authing with fb.");
                simpleLogin.loginWithFacebook(BDefines.APIs.FacebookAppId, (String) details.get(Keys.Facebook.AccessToken), handler);
                break;

            case Twitter:
                // TODO get twitter app id and etc.
                Long userId;
                if (details.get(Keys.UserId) instanceof Integer)
                    userId = new Long((Integer) details.get(Keys.UserId));
                else userId = (Long) details.get(Keys.UserId);

                if (DEBUG) Log.d(TAG, "authing with twitter. id: " + userId);
                simpleLogin.loginWithTwitter(BDefines.APIs.TwitterAccessToken, BDefines.APIs.TwitterAccessTokenSecret, userId, handler);
                break;

            case Password:
                simpleLogin.loginWithEmail((String) details.get(Prefs.LoginEmailKey),
                        (String) details.get(Prefs.LoginPasswordKey), handler);
                break;
            case  Register:
                simpleLogin.createUser((String) details.get(Prefs.LoginEmailKey),
                        (String) details.get(Prefs.LoginPasswordKey), handler);
                break;
            case Anonymous:
                simpleLogin.loginAnonymously(handler);
                break;

            default:
                break;
        }


    }


    public void handleFAUser(final User fuser, final CompletionListenerWithDataAndError<BUser, Object> listener){
        if (DEBUG) Log.v(TAG, "handleFAUser");

        authingStatus = AuthStatus.HANDLING_F_USER;

        if (fuser == null)
        {
            resetAuth();
            // If the user isn't authenticated they'll need to login
            listener.onDoneWithError(null, new BError(BError.Code.SESSION_CLOSED));
            return;
        }

        // Flag that the user has been authenticated
        setAuthenticated(true);

        String aid = BUser.safeAuthenticationID(fuser.getUserId(), fuser.getProvider());

        // Save the authentication ID for the current user
        // Set the current user
        Map<String, Object> loginInfoMap = new HashMap<String, Object>();
        loginInfoMap.put(Prefs.AuthenticationID, aid);
        loginInfoMap.put(Prefs.AccountTypeKey, fuser.getProvider().ordinal());
        setLoginInfo(loginInfoMap);

        final BUser user = DaoCore.fetchOrCreateUserWithAuthenticationID(aid);
        user.setAuthenticationType(fuser.getProvider().ordinal());

        BFirebaseInterface.selectEntity(user,
                new CompletionListenerWithDataAndError<BUser, FirebaseError>() {
                    @Override
                    public void onDone(BUser buser) {

                        updateUserFromFUser(buser, fuser);

                        listener.onDone(buser);
                    }

                    @Override
                    public void onDoneWithError(BUser bUser, FirebaseError error) {
                        resetAuth();
                        listener.onDoneWithError(bUser, new BError(BError.Code.FIREBASE_ERROR, error));
                    }
                });
    }


    /**Copy some details from the FAUser like name etc...*/
    public void updateUserFromFUser(final BUser user, User fireUser){
        if (DEBUG) Log.v(TAG, "updateUserFromFUser");

        authingStatus = AuthStatus.UPDATING_USER;

        Map <String, Object> thirdPartyData = fireUser.getThirdPartyUserData();
        String name;
        String email;
        BLinkedAccount linkedAccount;

        user.setOnline(true);

        switch (fireUser.getProvider())
        {
            case FACEBOOK:
                // Setting the name.
                name =(String) thirdPartyData.get(Keys.Facebook.DisplayName);
                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                {
                    user.setMetaName(name);
                }

                // Setting the email.//TODO get email
                email = "Email Adress";
                if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(user.getMetaEmail()))
                {
                    user.setMetaEmail(email);
                }

                linkedAccount = user.getAccountWithType(BLinkedAccount.Type.FACEBOOK);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.FACEBOOK);
                    linkedAccount.setUser(user.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(BDefines.Keys.Facebook.AccessToken));

                break;

            case TWITTER:
                // Setting the name.
                name = (String) thirdPartyData.get(Keys.Twitter.DisplayName);

                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                    user.setMetaName(name);

                TwitterManager.userId = Long.parseLong(fireUser.getUserId());
                TwitterManager.profileImageUrl = (String) thirdPartyData.get(Keys.Twitter.ImageURL);

                linkedAccount = user.getAccountWithType(BLinkedAccount.Type.TWITTER);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.TWITTER);
                    linkedAccount.setUser(user.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(Keys.Twitter.AccessToken));

                break;

            case PASSWORD:
                email = fireUser.getEmail();
                if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(user.getMetaEmail()))
                    user.setMetaEmail(email);
                break;

            default: break;
        }

        // Message Color.
        if (StringUtils.isEmpty(user.getMessageColor()) /*FIxME*/|| user.getMessageColor().equals("Red"))
        {
            if (StringUtils.isNotEmpty(BDefines.Defaults.MessageColor))
                user.setMessageColor(BDefines.Defaults.MessageColor);
            else user.setMessageColor( BMessageEntity.colorToString( BMessageEntity.randomColor() ) );
        }


        // Text Color.
        if (StringUtils.isEmpty(user.getTextColor()))
            user.setTextColor(BDefines.Defaults.MessageTextColor);

        // Font name.
        if (StringUtils.isEmpty(user.getFontName()))
        {
            if (StringUtils.isNotEmpty(BDefines.Defaults.MessageFontName))
                user.setFontName(BDefines.Defaults.MessageFontName);
            /*else {
                user.messageFontName = bSystemFont;
            }*/
            //ASK do i need to change the roboto font if there isnt a default?
        }

        // Font size.
        if (user.getFontSize() == null || user.getFontSize() == 0){
            user.setFontSize(BDefines.Defaults.MessageFontSize);
        }

        // Save the data
        DaoCore.updateEntity(user);

        authingStatus = AuthStatus.PUSHING_USER;
        pushUserWithCallback(new CompletionListener() {

            @Override
            public void onDone() {
                BUser currentUser = currentUser();

                if (DEBUG) Log.i(TAG, "User pushed After update from FUser, EntityID: " + currentUser.getEntityID());

                FirebasePaths userOnlineRef = FirebasePaths.userOnlineRef(currentUser.getEntityID());

                // Set the current state of the user as online.
                // And add a listener so when the user log off from firebase he will be set as disconnected.
                userOnlineRef.setValue(true);
                userOnlineRef.onDisconnect().setValue(false);

                updateLastOnline();

                EventManager.getInstance().observeUser(currentUser);

                // Subscribe to Parse push channel.
                subscribeToPushChannel(currentUser.getPushChannel());

                // Adding the user identifier to the bugsense so we could track problems by user id.
                if (BNetworkManager.BUGSENSE_ENABLED)
                    BugSenseHandler.setUserIdentifier(currentUser.getEntityID());

                resetAuth();
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "Failed to push user After update from FUser");
                resetAuth();
            }
        });
    }

    @Override
    /** Unlike the iOS code the current user need to be saved before you call this method.*/
    public void pushUserWithCallback(final CompletionListener listener) {
        // Update the user in the server.
        BUser user = currentUser();
        BFirebaseInterface.pushEntity(user, new RepetitiveCompletionListenerWithError() {
            @Override
            public boolean onItem(Object o) {
                return false;
            }

            @Override
            public void onDone() {
                if (listener != null)
                    listener.onDone();
            }

            @Override
            public void onItemError(Object o, Object o2) {
                if (listener != null)
                    listener.onDoneWithError();

            }
        });
    }

    @Override
    public void checkUserAuthenticatedWithCallback(final AuthListener listener) {
        if (DEBUG) Log.v(TAG, "checkUserAuthenticatedWithCallback, " + getLoginInfo().get(Prefs.AccountTypeKey));

        if (isAuthing())
        {
            if (DEBUG) Log.d(TAG, "Already Authing!, Status: " + authingStatus.name());
            return;
        }

        authingStatus = AuthStatus.CHECKING_IF_AUTH;

        if (!getLoginInfo().containsKey(Prefs.AccountTypeKey))
        {
            resetAuth();
            listener.onLoginFailed(new BError(BError.Code.NO_LOGIN_INFO));
            return;
        }

        SimpleLoginAuthenticatedHandler handler = new SimpleLoginAuthenticatedHandler() {
            @Override
            public void authenticated(Error error, User firebaseSimpleLoginUser) {
                if (error != null || firebaseSimpleLoginUser == null)
                {
                    resetAuth();

                    listener.onCheckDone(false);
                    if (DEBUG) Log.d(TAG, "Firebase SimpleLogin,  not authenticated");
                    if (error != null)
                    {
                        if (DEBUG) Log.d(TAG, error.name());
                    }
                    else if (DEBUG) Log.d(TAG, "No Error");

                    if (firebaseSimpleLoginUser != null)
                    {
                        if (DEBUG) Log.d(TAG, "fire base user is not null");
                    }
                    else if (DEBUG) Log.d(TAG, "firebase user is null");
                }
                else {
                    if (DEBUG) Log.d(TAG, "Firebase SimpleLogin, Authenticated");
                    listener.onCheckDone(true);
                    handleFAUser(firebaseSimpleLoginUser, new CompletionListenerWithDataAndError<BUser, Object>() {
                        @Override
                        public void onDone(BUser user) {
                            resetAuth();
                            listener.onLoginDone();
                        }

                        @Override
                        public void onDoneWithError(BUser user, Object o) {
                            resetAuth();
                            listener.onLoginFailed((BError) o);
                        }
                    });
                }
            }
        };

        if ((Integer) getLoginInfo().get(Prefs.AccountTypeKey) == Provider.FACEBOOK.ordinal())
        {
            Session session = Session.getActiveSession();
            if (session == null)
            {
                if (DEBUG) Log.d(TAG, "Session is null");
                session = Session.openActiveSessionFromCache(context);
            }
            if (session != null && session.isOpened())
            {
                if (DEBUG) Log.d(TAG, "FB Session is open");
//                listener.onCheckDone(true);
                Firebase ref = FirebasePaths.firebaseRef();
                final SimpleLogin simpleLogin = new SimpleLogin(ref, context);
                simpleLogin.checkAuthStatus(handler);
            }
            else {
                if (DEBUG) Log.d(TAG, "FB Session is closed");
                resetAuth();
                listener.onCheckDone(false);
            }
        }
        else
        {
            Firebase ref = FirebasePaths.firebaseRef();
            final SimpleLogin simpleLogin = new SimpleLogin(ref, context);
            simpleLogin.checkAuthStatus(handler);
        }
    }

    @Override
    public void logout() {

        /* No need to logout from facebook due to the fact that the logout from facebook event will trigger this event.
        *  The logout from fb is taking care of by the fb login button.*/
        setAuthenticated(false);

        // Stop listening to user related alerts. (added message or thread.)
        EventManager.getInstance().removeAll();

        // Obtaining the simple login object from the ref.
        SimpleLogin simpleLogin = new SimpleLogin(FirebasePaths.firebaseRef(), context);
        // Login out
        FirebasePaths userOnlineRef = FirebasePaths.userOnlineRef(currentUser().getEntityID());
        userOnlineRef.setValue(false);
        simpleLogin.logout();
 /*
TODO
        // Post a notification
        [[NSNotificationCenter defaultCenter] postNotificationName:bLogoutNotification object:Nil];
*/
    }
/*######################################################################################################*/
    /** Indexing
    * To allow searching we're going to implement a simple index. Strings can be registered and
    * associated with users i.e. if there's a user called John Smith we could make a new index
    * like this:
    *
    * indexes/[index ID (priority is: johnsmith)]/[entity ID of John Smith]
    *
    * This will allow us to find the user*/

    @Override
    public void usersForIndex(String index, final RepetitiveCompletionListener<BUser> listener) {
        mapForIndex(index, new MapForIndex() {
            @Override
            public void Completed(Firebase ref, String index, Map<String, Object> values) {
                if (DEBUG) Log.d(TAG, "usersForIndex, Completed");

                if (ref == null && values == null)
                {
                    if (DEBUG) Log.e(TAG, "Error occurred while fetching the map for the index.");
                    return;
                }

                final List<BUser> usersToGo = new ArrayList<BUser>();
                List<String> keys = new ArrayList<String>();

                // So we dont have to call the db for each key.
                String currentUserEntityID = currentUser().getEntityID();

                // Adding all keys to the list, Except the current user key.
                for (String key : values.keySet())
                    if (!key.equals(currentUserEntityID))
                        keys.add(key);

                if (keys.size() == 0)
                    if (DEBUG) Log.e(TAG, "Keys size is zero");

                /* Note this methods(Getting the user and selecting entities) is separated here not like in the iOS version.
                // Note because i am afraid the call back return to fast from selectEnity and a bad result will happen.*/

                // Fetch or create users in the local db.
                BUser bUser;
                if (keys.size() > 0){
                    for (String entityID : keys)
                    {
                        bUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityID);
                        usersToGo.add(bUser);
                    }

                    for (BUser user : usersToGo)
                    {
                        BFirebaseInterface.selectEntity(user, new CompletionListenerWithDataAndError<BUser, Object>() {
                            @Override
                            public void onDone(BUser u) {
                                // Remove the user.
                                usersToGo.remove(u);

                                // Notify that a user has been found.
                                listener.onItem(u);

                                // if no more users to found call on done.
                                if (usersToGo.size() == 0)
                                    listener.onDone();
                            }

                            @Override
                            public void onDoneWithError(BUser bUser1, Object o) {
                                if (DEBUG) Log.e(TAG, "usersForIndex, onDoneWithError.");
                                // Notify that an error occurred while selecting.
                                listener.onItemError(o);
                            }
                        });
                    }
                }
                else listener.onDone();
            }
        });
    }

    /**This method get the map values of data stored at a particular index*/
    private void mapForIndex(String index, final MapForIndex mapForIndex){
        Log.v(TAG, "mapForIndex, Index: " + index);
        FirebasePaths indexRef = FirebasePaths.indexRef();

        // Remove spaces from string and make it lower case
        index = index.replace(" ", "");
        index = index.toLowerCase();

        if (DEBUG) Log.d(TAG, "index after fix: " + index);

        Query query = indexRef.startAt(index).endAt(index);

        final String finalIndex = index;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot != null && snapshot.getValue() != null && snapshot.hasChildren())
                {
                    Log.v(TAG, "mapForIndex, onDataChanged, Has children.");
                    // Check to see if this user is already registered

                    for (DataSnapshot child : snapshot.getChildren())
                    {
                        // The child will contain a dictionary of users i.e. [user ID] => name => [user name]
                        if (child.getValue() != null)
                        {
                            // Return the index location and the value at the index
                            mapForIndex.Completed(child.getRef(), finalIndex, (Map<String, Object>) child.getValue());
                        } else if (DEBUG) Log.e(TAG, "apForIndex, onDataChanged, Value is null");
                        break;
                        // ASK why break here after the first child? is it because we can do snapshot.get(0)?
                    }

                }
                // Otherwise create a new index and add the user
                else
                {
                    Firebase ref = snapshot.getRef().push();

                    // Return the new index
                    mapForIndex.Completed(ref, finalIndex, new HashMap<String, Object>());
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
                mapForIndex.Completed(null, finalIndex, null);
            }
        });
    }

    /** Interface to return values from the <b>mapForIndex</b> method.*/
    public interface MapForIndex{
        public void Completed(Firebase ref, String index, Map<String, Object> values);
    }

    @Override
    public void removeUserFromIndex(final BUser user, String index, final CompletionListener listener) {
        if (index == null)
        {
            Log.d(TAG, "removeUserFromIndex, Index is null");

            //Note for now we will return onDone because the reason index is null is that we first remove index before adding it.so it might be the first time.
            listener.onDone();
            return;
        }

        mapForIndex(index, new MapForIndex() {
            @Override
            public void Completed(Firebase ref, String index, Map<String, Object> values) {
                if (ref == null || values == null)
                {
                    if (DEBUG) Log.e(TAG, "Error occurred while fetching the map for the index.");
                    return;
                }

                values.remove(user.getEntityID());

                ref.setValue(values, index, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError error, Firebase firebase) {
                        if (listener == null)
                            return;

                        if (error != null)
                        {
                            listener.onDoneWithError();
                        }
                        else listener.onDone();
                    }
                });
            }
        });
    }

    @Override
    public void addUserToIndex(final BUser user, String index, final CompletionListener listener) {
        // We don't want to index null strings!
        index = index.replace(" ", "");
        index = index.toLowerCase();

        if (StringUtils.isEmpty(index))
        {
            if (DEBUG) Log.e(TAG, "Index is empty");
            if (listener != null)
                listener.onDoneWithError();
            return;
        }

        mapForIndex(index, new MapForIndex() {
            @Override
            public void Completed(Firebase ref, String index, Map<String, Object> values) {
                if (ref == null || values == null)
                {
                    if (DEBUG) Log.e(TAG, "Error occurred while fetching the map for the index.");
                    return;
                }

                // Getting the user index by his entity id.
                Map<String, Object> map = ((Map<String, Object>) values.get(user.getEntityID()));

                // Map for index could not find older data so we create a new one.
                if (map == null)
                {
                    // Creating the new index
                    map = new HashMap<String, Object>();
                    map.put(Keys.BValue, index);
                }
                // Updating the index.
                else map.put(Keys.BValue, index);

                // Adding the new data to the index.
                values.put(user.getEntityID(), map);

                ref.setValue(values, index, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError error, Firebase firebase) {
                        if (listener == null)
                            return;

                        if (error!=null)
                            listener.onDoneWithError();
                        else listener.onDone();
                    }
                });

            }
        });
    }
/*######################################################################################################*/

   @Override
    public void getUserFacebookFriendsToAppWithComplition(CompletionListenerWithData<List<BUser>> listener) {
     /*   // Check to see if the user has a facebook network associated
        BUser * currentUser = (BUser *) [self currentUser];
        BUserAccount * account = [currentUser accountWithType:bAccountTypeFacebook];

        if (account && account.token) {

            [BFacebookManager sharedManager].accessToken = account.token;
            [[BFacebookManager sharedManager] getUserFriendListWithCompletion:^(NSError * error, NSArray * friends) {

                if (friends.count) {
                    BUser * currentUser = self.currentUser;

                    // Add the users to contacts
                    for (BUser * user in friends) {

                        [currentUser addContact:user];
                    }
                }

                completion(error, friends);
            }];
        }
        else {
            completion(Nil, @[]);
        }*/
    }

    @Override
    public void getUserFacebookFriendsWithCallback(CompletionListenerWithData listener) {
        BFacebookManager.getUserFriendList(listener);
    }

    @Override
    public BUser currentUser() {

        if (currentUser != null && System.currentTimeMillis() - lastCurrentUserCall < currentUserCallInterval)
        {
            return currentUser;
        }

        if (getCurrentUserAuthenticationId() != null)
        {
            String authID = getCurrentUserAuthenticationId();
            if (DEBUG) Log.d(TAG, "AuthID: "  + authID);

            currentUser = DaoCore.fetchOrCreateUserWithAuthenticationID(authID);

            lastCurrentUserCall = System.currentTimeMillis();

            if(DEBUG) {
                if (currentUser == null) Log.e(TAG, "Current user is null");
                else if (StringUtils.isEmpty(currentUser.getEntityID())) Log.e(TAG, "Current user entity id is null");
            }

            return currentUser;
        }
        if (DEBUG) Log.e(TAG, "getCurrentUserAuthenticationIdr is null");
        return null;
    }

    private long lastCurrentUserCall = 0;
    private final long currentUserCallInterval = 2000;
    private BUser currentUser = null;

    public void isOnline(final CompletionListenerWithData<Boolean> listener){
        if (currentUser() == null)
        {
            listener.onDoneWithError(BError.getError(BError.Code.NULL, "Current user is null"));
            return;
        }

        FirebasePaths.userOnlineRef(currentUser().getEntityID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listener.onDone((Boolean) snapshot.getValue());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                listener.onDoneWithError(BError.getFirebaseError(firebaseError));
            }
        });
    }
    /** Send a message,
     *  The message need to have a owner thread attached to it or it cant be added.
     *  If the destination thread is public the system will add the user to the message thread if needed.
     *  The uploading to the server part can bee seen her {@see BFirebaseNetworkAdapter#PushMessageWithComplition}.*/
      @Override //Note done!
    public void sendMessage(final BMessage message, final CompletionListenerWithData<BMessage> listener){
          if (DEBUG) Log.v(TAG, "sendMessage");
        if (message.getBThreadOwner() != null)
        {
            // If this is a public thread we need to add the user to it
            if (message.getBThreadOwner().getType() == BThread.Type.Public)
            {
                addUsersToThread(message.getBThreadOwner(), new RepetitiveCompletionListenerWithError<BUser, Object>() {
                    @Override
                    public boolean onItem(BUser user) {

                        return false;
                    }

                    @Override
                    public void onDone() {
                        if (DEBUG) Log.d(TAG, "sendMessage, OnDone");
                        new PushMessageWithComplition(message, listener);
                    }

                    @Override
                    public void onItemError(BUser user, Object o) {

                    }
                }, currentUser());
            }
            else
            {
                BUser currentUSer = currentUser();
                // Adding all the threads participants as contacts.
                for (BUser user : message.getBThreadOwner().getUsers())
                    currentUSer.addContact(user);

                new PushMessageWithComplition(message, listener);
            }
        } else if (DEBUG) Log.e(TAG, "Message doesn't have an owner thread.");
    }

    /** Push the message to the firebase server and update the thread. */
    private class PushMessageWithComplition{

        public PushMessageWithComplition(final BMessage message, final CompletionListenerWithData<BMessage> listener){
            if (DEBUG) Log.v(TAG, "PushMessageWithComplition");

            BFirebaseInterface.pushEntity(message, new RepetitiveCompletionListenerWithError() {
                @Override
                public boolean onItem(Object o) {
                    // Update the thread with the time the last message was added
                    FirebasePaths threadRef = FirebasePaths.threadRef(message.getBThreadOwner().getEntityID());
                    if (DEBUG) Log.d(TAG, "PushMessageWithComplition, RefPath: " + threadRef.toString());
                    threadRef = threadRef.appendPathComponent(BFirebaseDefines.Path.BDetailsPath);
                    if (DEBUG) Log.d(TAG, "PushMessageWithComplition, RefPath: " + threadRef.toString());

                    threadRef.updateChildren(FirebasePaths.getMap( new String[]{Keys.BLastMessageAdded},  System.currentTimeMillis()));

                    pushForMessage(message);

                    if(listener != null)
                        listener.onDone(message);

                    return false;
                }

                @Override
                public void onDone() {

                }

                @Override
                public void onItemError(Object o, Object error) {
                    if(listener != null)
                        listener.onDoneWithError((com.braunster.chatsdk.object.BError) error);
                }
            });
        }
    }


    /** Create thread for given users.
         *  When the thread is added to the server the "onMainFinished" will be invoked,
         *  If an error occurred the error object would not be null.
         *  For each user that was successfully added the "onItem" method will be called,
         *  For any item adding failure the "onItemFailed will be called.
         *   If the main task will fail the error object in the "onMainFinished" method will be called."*/
    @Override
    public void createThreadWithUsers(String name, final List<BUser> users, final RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object> listener) {

        BUser currentUser = currentUser();

        // Checking to see if this users already has a private thread.
        if (users.size() == 2)
        {
            Log.d(TAG, "Checking if allready has a thread.");
            List<BUser> threadusers;

            BUser userToCheck;
            if (users.get(0).getEntityID().equals(currentUser.getEntityID()))
                userToCheck = users.get(1);
            else userToCheck = users.get(0);

            for (BThread t : currentUser.getThreads())
            {
                // Skipping public threads.
                if (t.getType() == null || t.getType() == BThreadEntity.Type.Public)
                    continue;

                threadusers = t.getUsers();
                if (threadusers.size() == 2) {
                    if (threadusers.get(0).getEntityID().equals(userToCheck.getEntityID()) ||
                            threadusers.get(1).getEntityID().equals(userToCheck.getEntityID())) {
                        listener.onMainFinised(t, null);
                        listener.onDone();
                        return;
                    }
                }
            }
        }

        final BThread thread = new BThread();
        thread.setCreationDate(new Date());
        thread.setCreator(currentUser);

        // If we're assigning users then the thread is always going to be private
        thread.setType(BThread.Type.Private);

        // Save the thread to the database.
        DaoCore.createEntity(thread);

        updateLastOnline();

        BFirebaseInterface.pushEntity(thread, new RepetitiveCompletionListenerWithError<BThread, Object>() {
            @Override
            public boolean onItem(BThread bThread) {
                // Thread is added successfully

                // Save the thread to the local db.
                DaoCore.updateEntity(thread);

                // Report back that the thread is added.
                listener.onMainFinised(thread, null);

                //ASK if this is good.
                // Adding the thread to the current user ref.


                // Add users, For each added user the listener passed here will get a call.
                addUsersToThread(thread, users, listener);
                return false;
            }

            @Override
            public void onDone() {

            }

            @Override
            public void onItemError(BThread bThread, Object error) {
                if (DEBUG) Log.e(TAG, "Error while pushing threda.");
                // Delete the thread if failed to push
                DaoCore.deleteEntity(thread);

                // return null instead of the thread because the listener expect BUser item as a return value.
                listener.onItemError(null, error);
            }
        });
    }

    @Override
    public void createPublicThreadWithName(String name, final CompletionListenerWithDataAndError<BThread, Object> listener) {
        // Crating the new thread.
        // This thread would not be saved to the local db until it is successfully uploaded to the firebase server.
        final BThread thread = new BThread();
        thread.setCreationDate(new Date());
        thread.setCreator(currentUser());

        thread.setType(BThread.Type.Public);
        thread.setName(name);

        // Save the entity to the local db.
        DaoCore.createEntity(thread);

        BFirebaseInterface.pushEntity(thread, new RepetitiveCompletionListenerWithError<BThread, FirebaseError>() {
            @Override
            public boolean onItem(BThread bThread) {

                DaoCore.updateEntity(bThread);

                if (DEBUG) Log.d(TAG, "public thread is pushed and saved.");

                // Add the thread to the list of public threads
                FirebasePaths publicThreadRef = FirebasePaths.publicThreadsRef().appendPathComponent(thread.getEntityID());
                publicThreadRef.setValue("null", new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError error, Firebase firebase) {
                        if (error == null)
                            listener.onDone(thread);
                        else
                        {
                            if (DEBUG) Log.e(TAG, "Unable to add thread to public thread ref.");
                            DaoCore.deleteEntity(thread);
                            listener.onDoneWithError(thread, error);
                        }
                    }
                });

                return false;
            }

            @Override
            public void onDone() {

            }

            @Override
            public void onItemError(BThread bThread, FirebaseError error) {
                if (DEBUG) Log.e(TAG, "Failed to push thread to ref.");
                DaoCore.deleteEntity(bThread);
                listener.onDoneWithError(thread, error);
            }
        });
    }

    /** Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.*/
    @Override
    public void addUsersToThread(final BThread thread, final List<BUser> users, final RepetitiveCompletionListenerWithError<BUser, Object> listener) {
        if (thread == null)
        {
            if (DEBUG) Log.e(TAG,"addUsersToThread, Thread is null" );
            return;
        }

        final List<BUser> usersToGo = new ArrayList<BUser>(users);

        if (DEBUG) Log.d(TAG, "Users Amount: " + users.size());

        final RepetitiveCompletionListenerWithError repetitiveCompletionListener = new RepetitiveCompletionListenerWithError<BUser, FirebaseError>() {
            @Override
            public boolean onItem(BUser user) {
                if (DEBUG) Log.d(TAG,"addUsersToThread, OnItem, Users TO go: " + usersToGo.size() );

                usersToGo.remove(user);

                if (listener != null)
                    listener.onItem(user);

                if (usersToGo.size() == 0)
                    onDone();

                return false;
            }

            @Override
            public void onDone() {
                if (listener != null)
                    listener.onDone();
            }

            @Override
            public void onItemError(BUser user, FirebaseError error) {
                if (listener != null)
                    listener.onItemError(user, error);
            }
        };

        for (BUser user : users){
            if (user == null)
            {
                if (DEBUG) Log.e(TAG, "user is null");
                continue;
            }

            // Add the user to the thread
            if (!user.hasThread(thread))
            {
                DaoCore.connectUserAndThread(user, thread);
            }

            // Check to see if the user already exists on Firebase
            if (user.getEntityID() != null) {
                addUserToThread(thread, user, repetitiveCompletionListener);
            }
            else {
                // Create or update the user
                BFirebaseInterface.pushEntity(user, new RepetitiveCompletionListenerWithError<BUser, FirebaseError>() {
                    @Override
                    public boolean onItem(BUser user) {
                        // Addding the new user to the thread.
                        addUserToThread(thread, user, repetitiveCompletionListener);
                        return false;
                    }

                    @Override
                    public void onDone() {

                    }

                    @Override
                    public void onItemError(BUser user, FirebaseError error) {
                        repetitiveCompletionListener.onItemError(user, error);
                    }
                });
            }
        }
    }

    /** adds a thread to the user */
    private void addUserToThread(final BThread thread, final BUser user, final RepetitiveCompletionListenerWithError<BUser, Object> listener){
        if (DEBUG) Log.d(TAG, "addUserToThread");
        // Get the user's reference
        FirebasePaths userThreadsRef = FirebasePaths.userRef(user.getEntityID());
        if (DEBUG) Log.d(TAG, "addUserToThread, userThreadsRef: " + userThreadsRef.toString());
        userThreadsRef = userThreadsRef.appendPathComponent(thread.getPath().getPath());

        if (DEBUG) Log.d(TAG, "addUserToThread, userThreadsRef: " + userThreadsRef.toString());

        // Write a null value to this location
        // the list will look like this:
        // user/threads/[thread id]/null
        userThreadsRef.setValue("null", new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError error, Firebase firebase) {
                if (error == null)
                {
                    FirebasePaths threadRef = FirebasePaths.threadRef(thread.getEntityID());
                    if (DEBUG) Log.d(TAG, "addUserToThread, threadRef: " + threadRef.toString());
                    threadRef = threadRef.appendPathComponent(user.getPath().getPath());
                    if (DEBUG) Log.d(TAG, "addUserToThread, threadRef: " + threadRef.toString());

                    if (DEBUG) Log.e(TAG, "#########################NAME#####" + user.getMetaName() + " , ID " + user.getEntityID());
                    Map<String , Object> values = new HashMap<String, Object>();

                    // If metaname is null the data wont be saved so we have to do so.
                    values.put(Keys.BName, (user.getMetaName()== null ?"no_name":user.getMetaName()) ) ;
                    threadRef.setValue(values, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError error, Firebase firebase) {
                            if (error == null)
                            {
                                listener.onItem(user);
//                                listener.onDone();
                                //Note this done caused double message sending because its affect the repetitive listner which should call done when entities are all added.
                            }
                            else listener.onItemError(user, error);
                        }
                    });
                }
                else
                {
                    listener.onItemError(user, error);
                }
            }
        });
    }

    @Override
    public void loadMoreMessagesForThread(BThread thread, CompletionListenerWithData<BMessage[]> listener) {
        BFirebaseInterface.loadMoreMessagesForThread(thread, BFirebaseDefines.NumberOfMessagesPerBatch, listener);
    }

    @Override
    public void deleteThreadWithEntityID(final String entityID, final CompletionListener completionListener) {

        final BThread thread = DaoCore.<BThread>fetchEntityWithEntityID(BThread.class, entityID);

        BUser user = currentUser();

        // Stop listening to thread events, Details change, User added and incoming mesages.
        EventManager.getInstance().stopListeningToThread(entityID);

        // Removing the thread from the currnt user thread ref.
        FirebasePaths userThreadRef = FirebasePaths.firebaseRef();
        userThreadRef = userThreadRef.appendPathComponent(user.getPath().getPath()).appendPathComponent(thread.getPath().getPath());
        userThreadRef.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError error, Firebase firebase) {
                // Delete the thread if no error occurred when deleting from firebase.
                if (error == null)
                {
                    if (DEBUG) Log.d(TAG, "Deleting thread from db.");
                    List<BLinkData> list =  DaoCore.fetchEntitiesWithProperty(BLinkData.class, BLinkDataDao.Properties.ThreadID, thread.getId());
                    List<BMessage> messages = DaoCore.fetchEntitiesWithProperty(BMessage.class, BMessageDao.Properties.OwnerThread, thread.getId());

                    DaoCore.deleteEntity(thread);

                    for (BLinkData d : list)
                        DaoCore.deleteEntity(d);

                    for (BMessage m : messages)
                        DaoCore.deleteEntity(m);

                    if (DEBUG)
                    {
                        BThread deletedThread = DaoCore.<BThread>fetchEntityWithEntityID(BThread.class, entityID);
                        if (deletedThread == null)
                            Log.d(TAG, "Thread deleted successfully.");
                        else Log.d(TAG, "Thread isn't deleted.");
                    }
                    completionListener.onDone();
                } else completionListener.onDoneWithError();
            }
        });

        updateLastOnline();
    }

    @Override//ASK this method is missing in the firebse adapter.
    public void deleteThread(BThread thread, CompletionListener completionListener) {

    }

    @Override
    public String getServerURL() {
        return FirebasePaths.FIREBASE_PATH;
    }

    @Override
    public void setLastOnline(Date lastOnline) {
        BUser currentUser  = currentUser();
        currentUser.setLastOnline(lastOnline);
        currentUser = DaoCore.updateEntity(currentUser);

        pushUserWithCallback(null);
//        FirebasePaths.userRef(currentUser.getEntityID()).appendPathComponent(BFirebaseDefines.Path.BDetailsPath).appendPathComponent(Keys.BLastOnline).setValue(lastOnline.getTime());


    }

    private void updateLastOnline(){
        setLastOnline(new Date());
    }

    private void pushForMessage(BMessage message){
        if (DEBUG) Log.v(TAG, "pushForMessage");
        BUser currentUser = currentUser();
        List<BUser> users = new ArrayList<BUser>();
        if (message.getBThreadOwner().getType() == BThread.Type.Private)
        {
            for (BUser user : message.getBThreadOwner().getUsers())
                if (!user.equals(currentUser))
                    if (user.getOnline() == null || !user.getOnline())
                    {
                        if (DEBUG) Log.d(TAG, "Pushing message.");
                        users.add(user);
                    }

            pushToUsers(message, users);
        }
    }

    private void pushToUsers(BMessage message, List<BUser> users){
        if (DEBUG) Log.v(TAG, "pushToUsers");

        if (!pushEnabled())
            return;

        // We're identifying each user using push channels. This means that
        // when a user signs up, they register with parse on a particular
        // channel. In this case user_[user id] this means that we can
        // send a push to a specific user if we know their user id.
        List<String> channels = new ArrayList<String>();
        for (BUser user : users)
            channels.add(user.getPushChannel());

        // FIXME this is not right need to do it like in the iOS.
        PushUtils.sendMessage(message, channels);
    }

    private void subscribeToPushChannel(String channel){
        if (!pushEnabled())
            return;

        PushService.subscribe(context, channel, MainActivity.class);
    }

    private boolean pushEnabled(){
        return StringUtils.isNotEmpty(BDefines.APIs.ParseAppId) && StringUtils.isNotEmpty(BDefines.APIs.ParseClientKey);
    }
    /*
    * #pragma Push Notifications

+(void) registerForPushNotificationsWithApplication: (UIApplication *) app launchOptions: (NSDictionary *) options {
    if (![self pushEnabled]) {
        return;
    }

    [Parse setApplicationId:bParseAppKey
                  clientKey:bParseClientKey];

    [PFAnalytics trackAppOpenedWithLaunchOptions:options];
    //[app unregisterForRemoteNotifications];
    [app registerForRemoteNotificationTypes:UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeSound];
}

-(void) application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    if (![BFirebaseNetworkAdapter pushEnabled]) {
        return;
    }

    [PFPush handlePush:userInfo];
}

-(void) application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    if (![BFirebaseNetworkAdapter pushEnabled]) {
        return;
    }

    // Store the deviceToken in the current installation and save it to Parse.
    PFInstallation *currentInstallation = [PFInstallation currentInstallation];
    [currentInstallation setDeviceTokenFromData:deviceToken];
    [currentInstallation saveInBackground];
}

*/
}
