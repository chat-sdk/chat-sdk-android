package com.braunster.chatsdk.network.firebase;

import android.content.Context;
import android.util.Log;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.dao.BFollower;
import com.braunster.chatsdk.dao.BLinkData;
import com.braunster.chatsdk.dao.BLinkDataDao;
import com.braunster.chatsdk.dao.BLinkedAccount;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BMetadata;
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
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.parse.PushService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.braunster.chatsdk.network.BDefines.BAccountType.Anonymous;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Facebook;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Password;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Register;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Twitter;
import static com.braunster.chatsdk.network.BDefines.Keys;
import static com.braunster.chatsdk.network.BDefines.Prefs;
import static com.braunster.chatsdk.network.firebase.FirebasePaths.ProviderInt;

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

    public void test(){

    }

    @Override
    public void authenticateWithMap(final Map<String, Object> details, final CompletionListenerWithDataAndError<AuthData, Object> listener) {
        if (DEBUG) Log.v(TAG, "authenticateWithMap, KeyType: " + details.get(Prefs.LoginTypeKey));

        if (isAuthing())
        {
            if (DEBUG) Log.d(TAG, "Already Authing!, Status: " + authingStatus.name());
            return;
        }

        authingStatus = AuthStatus.AUTH_WITH_MAP;

        Firebase ref = FirebasePaths.firebaseRef();

        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(final AuthData authData) {
                handleFAUser(authData, new CompletionListenerWithDataAndError<BUser, Object>() {
                    @Override
                    public void onDone(BUser user) {
                        resetAuth();
                        listener.onDone(authData);
                    }

                    @Override
                    public void onDoneWithError(BUser bUser, Object o) {
                        listener.onDoneWithError(null, o);
                        resetAuth();
                    }
                });
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                if (DEBUG) Log.e(TAG, "Error login in, Name: " + firebaseError.getMessage());
                resetAuth();
                listener.onDoneWithError(null, firebaseError);
            }
        };

        switch ((Integer)details.get(Prefs.LoginTypeKey))
        {
            case Facebook:
                if (DEBUG) Log.d(TAG, "authing with fb, AccessToken: " + BFacebookManager.userFacebookAccessToken);
                ref.authWithOAuthToken("facebook", BFacebookManager.userFacebookAccessToken, authResultHandler);
                break;

            case Twitter:
                Long userId;
                if (details.get(Keys.UserId) instanceof Integer)
                    userId = new Long((Integer) details.get(Keys.UserId));
                else userId = (Long) details.get(Keys.UserId);

                Map<String, String> options = new HashMap<String, String>();
                options.put("oauth_token", BDefines.APIs.TwitterAccessToken);
                options.put("oauth_token_secret",BDefines.APIs.TwitterAccessTokenSecret);
                options.put("user_id", String.valueOf(userId));

                if (DEBUG) Log.d(TAG, "authing with twitter. id: " + userId);
                ref.authWithOAuthToken("twitter", options, authResultHandler);
                break;

            case Password:
                ref.authWithPassword((String) details.get(Prefs.LoginEmailKey),
                        (String) details.get(Prefs.LoginPasswordKey), authResultHandler);
                break;
            case  Register:
                ref.createUser((String) details.get(Prefs.LoginEmailKey),
                        (String) details.get(Prefs.LoginPasswordKey), new Firebase.ResultHandler() {
                            @Override
                            public void onSuccess() {
                                resetAuth();
                                //Authing the user after creating it.
                                details.put(Prefs.LoginTypeKey, Password);
                                authenticateWithMap(details, listener);
                            }

                            @Override
                            public void onError(FirebaseError firebaseError) {
                                if (DEBUG) Log.e(TAG, "Error login in, Name: " + firebaseError.getMessage());
                                resetAuth();
                                listener.onDoneWithError(null, firebaseError);
                            }
                        });
                break;
            case Anonymous:
                ref.authAnonymously(authResultHandler);
                break;

            default:
                if (DEBUG) Log.d(TAG, "No login type was found");
                if (listener!=null)
                    listener.onDoneWithError(null, BError.getError(BError.Code.NO_LOGIN_TYPE, "No matching login type was found"));
                break;
        }


    }

    public void handleFAUser(final AuthData authData, final CompletionListenerWithDataAndError<BUser, Object> listener){
        if (DEBUG) Log.v(TAG, "handleFAUser");

        authingStatus = AuthStatus.HANDLING_F_USER;

        if (authData == null)
        {
            resetAuth();
            // If the user isn't authenticated they'll need to login
            listener.onDoneWithError(null, new BError(BError.Code.SESSION_CLOSED));
            return;
        }

        // Flag that the user has been authenticated
        setAuthenticated(true);

        String provider = authData.getProvider();
        String aid;

        // We need to get the user safe if, Password has it saved in the Uid also anonymous.
        if (provider.equals(FirebasePaths.ProviderString.Password))
        {
            if (DEBUG) Log.d(TAG, "Uid: " + authData.getUid());
            aid = BUser.safeAuthenticationID(authData.getUid().replace("simplelogin:", ""), FirebasePaths.providerToInt(authData.getProvider()));
        }
        else if (provider.equals(FirebasePaths.ProviderString.Anonymous))
        {
            aid = BUser.safeAuthenticationID(authData.getUid().replace("anonymous:", ""), FirebasePaths.providerToInt(authData.getProvider()));
        }
        else
        {
            aid = BUser.safeAuthenticationID((String) authData.getProviderData().get(Keys.ThirdPartyData.ID), FirebasePaths.providerToInt(authData.getProvider()));
        }

        // Save the authentication ID for the current user
        // Set the current user
        final Map<String, Object> loginInfoMap = new HashMap<String, Object>();
        loginInfoMap.put(Prefs.AuthenticationID, aid);
        loginInfoMap.put(Prefs.AccountTypeKey, FirebasePaths.providerToInt(authData.getProvider()));

        final BUser user = DaoCore.fetchOrCreateUserWithAuthenticationID(aid);
        user.setAuthenticationType(FirebasePaths.providerToInt(authData.getProvider()));

        BFirebaseInterface.selectEntity(user,
                new CompletionListenerWithDataAndError<BUser, FirebaseError>() {
                    @Override
                    public void onDone(BUser buser) {
                        loginInfoMap.put(Prefs.PushEnabled,
                                BNetworkManager.getUserPrefs(buser.getEntityID()).getBoolean(Prefs.PushEnabled, BNetworkManager.PushEnabledDefaultValue));

                        setLoginInfo(loginInfoMap);

                        updateUserFromFUser(buser, authData, listener);

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
    public void updateUserFromFUser(final BUser user, AuthData authData, final CompletionListenerWithDataAndError<BUser, Object> listener){
        if (DEBUG) Log.v(TAG, "updateUserFromFUser");

        authingStatus = AuthStatus.UPDATING_USER;

        Map <String, Object> thirdPartyData = authData.getProviderData();
        String name = (String) thirdPartyData.get(Keys.ThirdPartyData.DisplayName);;
        String email = (String) thirdPartyData.get(Keys.ThirdPartyData.EMail);;
        BLinkedAccount linkedAccount;

        user.setOnline(true);

        switch (FirebasePaths.providerToInt(authData.getProvider()))
        {
            case ProviderInt.Facebook:
                // Setting the name.
                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                {
                    user.setMetaName(name);
                }

                // Setting the email.//
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
                linkedAccount.setToken((String) thirdPartyData.get(Keys.ThirdPartyData.AccessToken));

                break;

            case ProviderInt.Twitter:
                // Setting the name
                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                    user.setMetaName(name);

                // Setting the email.//
                if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(user.getMetaEmail()))
                {
                    user.setMetaEmail(email);
                }

                TwitterManager.userId = Long.parseLong((String) thirdPartyData.get(Keys.ThirdPartyData.ID));
                TwitterManager.profileImageUrl = (String) thirdPartyData.get(Keys.ThirdPartyData.ImageURL);

                linkedAccount = user.getAccountWithType(BLinkedAccount.Type.TWITTER);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.TWITTER);
                    linkedAccount.setUser(user.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(Keys.ThirdPartyData.AccessToken));

                break;

            case ProviderInt.Password:
                // Setting the name
                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                    user.setMetaName(name);

                if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(user.getMetaEmail()))
                    user.setMetaEmail(email);
                break;

            default: break;
        }

        // Message Color.
        if (StringUtils.isEmpty(user.getMessageColor()) /*FIxME due to old data*/|| user.getMessageColor().equals("Red"))
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

        DaoCore.updateEntity(user.getMetadataForKey(BDefines.Keys.BPictureURLThumbnail, BMetadata.Type.STRING));
        DaoCore.updateEntity(user.getMetadataForKey(Keys.BPictureURL, BMetadata.Type.STRING));

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

                if (listener!=null)
                    listener.onDone(currentUser);
            }

            @Override
            public void onDoneWithError(BError error) {
                if (DEBUG) Log.e(TAG, "Failed to push user After update from FUser");
                resetAuth();

                /*FIXME see if needed. if (listener!=null)
                    listener.onDoneWithError(error);*/
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
                    listener.onDoneWithError((BError) o2);

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
            if (DEBUG) Log.d(TAG, "No account type key");
            resetAuth();
            listener.onLoginFailed(new BError(BError.Code.NO_LOGIN_INFO));
            return;
        }


        Firebase ref = FirebasePaths.firebaseRef();
        if (ref.getAuth()!=null)
        {
            handleFAUser(ref.getAuth(), new CompletionListenerWithDataAndError<BUser, Object>() {
                @Override
                public void onDone(BUser user) {
                    listener.onLoginDone();
                }

                @Override
                public void onDoneWithError(BUser user, Object o) {
                    //TODO Add real error value.
                    listener.onLoginFailed(null);
                }
            });

        }
        else listener.onCheckDone(false);

        resetAuth();

        /*if ((Integer) getLoginInfo().get(Prefs.AccountTypeKey) == Provider.FACEBOOK.ordinal())
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
            if (DEBUG) Log.d(TAG, "SimpleLogin");
            Firebase ref = FirebasePaths.firebaseRef();


            final SimpleLogin simpleLogin = new SimpleLogin(ref, context);
            simpleLogin.checkAuthStatus(handler);
        }*/
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword, Firebase.ResultHandler resultHandler){
        FirebasePaths.firebaseRef().changePassword(email, oldPassword, newPassword, resultHandler);
    }

    @Override
    public void sendPasswordResetMail(String email, Firebase.ResultHandler resultHandler){
        FirebasePaths.firebaseRef().resetPassword(email, resultHandler);
    }

    @Override
    public void logout() {

        BUser user = currentUser();

        /* No need to logout from facebook due to the fact that the logout from facebook event will trigger this event.
        *  The logout from fb is taking care of by the fb login button.*/
        setAuthenticated(false);

        // Stop listening to user related alerts. (added message or thread.)
        EventManager.getInstance().removeAll();

        // Removing the push channel
        unsubscribeToPushChannel(user.getPushChannel());

        // Obtaining the simple login object from the ref.
        FirebasePaths ref = FirebasePaths.firebaseRef();

        // Login out
        FirebasePaths userOnlineRef = FirebasePaths.userOnlineRef(user.getEntityID());
        userOnlineRef.setValue(false);

        ref.unauth();
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
    public void usersForIndex(final String index, final String value, final RepetitiveCompletionListener<BUser> listener) {
        if (StringUtils.isBlank(value))
        {
            if (listener!=null)
                listener.onDone();
            return;
        }

        Query query = FirebasePaths.indexRef().orderByChild(index).startAt(processForQuery(value)).limitToFirst(BFirebaseDefines.NumberOfUserToLoadForIndex);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Map<String, Objects> values = (Map<String, Objects>) snapshot.getValue();

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
                    if (keys.size() > 0) {
                        for (String entityID : keys) {
                            bUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityID);
                            usersToGo.add(bUser);
                        }

                        for (BUser user : usersToGo) {
                            BFirebaseInterface.selectEntity(user, new CompletionListenerWithDataAndError<BUser, Object>() {
                                @Override
                                public void onDone(BUser u) {
                                    // Remove the user.
                                    usersToGo.remove(u);

                                    // Notify that a user has been found.
                                    // Making sure the user due start with the wanted name
                                    if (processForQuery(u.metaStringForKey(index)).startsWith(processForQuery(value)))
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
                    } else listener.onDone();
                } else {
                    if (listener != null)
                        listener.onItemError(BError.getError(BError.Code.NULL, "No values found for this index."));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void updateIndexForUser(BUser user, final CompletionListener listener){
        if (StringUtils.isEmpty(user.getEntityID()))
        {
            if (listener!= null)
                listener.onDoneWithError(BError.getError(BError.Code.NULL, "Entity id is null"));
            return;
        }

        FirebasePaths ref = FirebasePaths.indexRef().appendPathComponent(user.getEntityID());

        Map<String, String> values = new HashMap<String, String>();
        values.put(BDefines.Keys.BName, processForQuery(user.getMetaName()));
        values.put(BDefines.Keys.BEmail, processForQuery(user.getMetaEmail()));

        // No listener was assigned so ne need for callback
        if (listener==null)
            ref.setValue(values);
        else ref.setValue(values, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError==null)
                {
                    listener.onDone();
                }
                else{
                    listener.onDoneWithError(BError.getFirebaseError(firebaseError));
                }
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

    /*    if (currentUser != null && System.currentTimeMillis() - lastCurrentUserCall < currentUserCallInterval)
        {
            return currentUser;
        }*/

        String authID = getCurrentUserAuthenticationId();
        if (StringUtils.isNotEmpty(authID))
        {
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
    private final long currentUserCallInterval = 100;
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

    /*######################################################################################################*/
    /*Followers*/
    @Override/*TODO report success and error*/
    public void followUser(final BUser userToFollow, final CompletionListener listener) {

        if (!BDefines.EnableFollowers)
            throw new IllegalStateException("You need to enable followers in defines before you can use this method.");

        final BUser user = currentUser();

        // Add the current user to the userToFollow "followers" path
        FirebasePaths userToFollowRef = FirebasePaths.userRef(userToFollow.getEntityID()).appendPathComponent(BFirebaseDefines.Path.BFollowers).appendPathComponent(user.getEntityID());
        if (DEBUG) Log.d(TAG, "followUser, userToFollowRef: " + userToFollowRef.toString());

        userToFollowRef.setValue("null", new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError!=null)
                {
                    //TODO handle error.
                }
                else
                {
                    BFollower follows = user.fetchOrCreateFollower(userToFollow, BFollower.Type.FOLLOWS);

                    user.addContact(userToFollow);

                    // Add the user to follow to the current user follow
                    FirebasePaths curUserFollowsRef = FirebasePaths.firebaseRef().appendPathComponent(follows.getPath().getPath());
                    if (DEBUG) Log.d(TAG, "followUser, curUserFollowsRef: " + curUserFollowsRef.toString());
                    curUserFollowsRef.setValue("null", new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                            // Send a push to the user that is now followed.
                            PushUtils.sendFollowPush(userToFollow.getPushChannel(), user.getMetaName() + " " + context.getString(R.string.not_follower_content));

                            if (listener!=null)
                                listener.onDone();
                        }
                    });
                }
            }
        });

    }

    @Override/*TODO report success and error*/
    public void unFollowUser(BUser userToUnfollow, final CompletionListener listener) {
        if (!BDefines.EnableFollowers)
            throw new IllegalStateException("You need to enable followers in defines before you can use this method.");

        final BUser user = currentUser();

        // Remove the current user to the userToFollow "followers" path
        FirebasePaths userToFollowRef = FirebasePaths.userRef(userToUnfollow.getEntityID()).appendPathComponent(BFirebaseDefines.Path.BFollowers).appendPathComponent(user.getEntityID());
        if (DEBUG) Log.d(TAG, "followUser, userToFollowRef: " + userToFollowRef.toString());

        userToFollowRef.removeValue();

        BFollower follows = user.fetchOrCreateFollower(userToUnfollow, BFollower.Type.FOLLOWS);

        // Add the user to follow to the current user follow
        FirebasePaths curUserFollowsRef = FirebasePaths.firebaseRef().appendPathComponent(follows.getPath().getPath());
        if (DEBUG) Log.d(TAG, "followUser, curUserFollowsRef: " + curUserFollowsRef.toString());
        curUserFollowsRef.removeValue();

        DaoCore.deleteEntity(follows);
    }

    @Override
    public void getFollowers(String entityId, final RepetitiveCompletionListener<BUser> listener){
        if (DEBUG) Log.v(TAG, "getFollowers, Id: " + entityId);

        if (StringUtils.isEmpty(entityId))
        {
            listener.onItemError(BError.getError(BError.Code.NULL, "Entity id is empty"));
            listener.onDone();
            return;
        }

        final BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);

        FirebasePaths followersRef = FirebasePaths.userFollowersRef(entityId);

        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (DEBUG) Log.v(TAG, "onDataChanged: " + snapshot.getChildrenCount());

                final List<BUser> followers = new ArrayList<BUser>();

                if (DEBUG)Log.d(TAG, snapshot.getChildren()!=null ? "has children, has next? " + snapshot.getChildren().iterator().hasNext() : "no children");

                if (DEBUG)Log.d(TAG, snapshot.getRef().toString());

                for (DataSnapshot snap : snapshot.getChildren())
                {
                    if (DEBUG) Log.d(TAG, "this is it.");
                    if (DEBUG) Log.d(TAG, "Snapshot Name: " + snap.getName());
                    String followingUserID = snap.getName();

                    if (StringUtils.isNotEmpty(followingUserID))
                    {
                        BUser follwer = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followingUserID);

                        BFollower f = user.fetchOrCreateFollower(follwer, BFollower.Type.FOLLOWER);

                        followers.add(follwer);
                    } else if (DEBUG) Log.e(TAG, "Follower id is empty");
                }

                for (BUser u : followers)
                {
                    if (listener!=null)
                    {
                        BFirebaseInterface.selectEntity(u, new CompletionListenerWithDataAndError<BUser, Object>() {
                            @Override
                            public void onDone(BUser u) {
                                // Remove the user.
                                followers.remove(u);

                                // Notify that a user has been found.
                                listener.onItem(u);

                                // if no more users to found call on done.
                                if (followers.size() == 0)
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
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    public void getFollows(String entityId, final RepetitiveCompletionListener<BUser> listener){
        if (DEBUG) Log.v(TAG, "getFollowers, Id: " + entityId);

        if (StringUtils.isEmpty(entityId))
        {
            listener.onItemError(BError.getError(BError.Code.NULL, "Entity id is empty"));
            listener.onDone();
            return;
        }

        final BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);

        FirebasePaths followersRef = FirebasePaths.userFollowsRef(entityId);

        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final List<BUser> followers = new ArrayList<BUser>();

                for (DataSnapshot snap : snapshot.getChildren())
                {
                    Log.d(TAG, "Name: " + snap.getName());
                    String followingUserID = snap.getName();

                    if (StringUtils.isNotEmpty(followingUserID))
                    {
                        BUser follwer = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followingUserID);

                        BFollower f = user.fetchOrCreateFollower(follwer, BFollower.Type.FOLLOWS);

                        followers.add(follwer);
                    }
                }

                for (BUser u : followers)
                {
                    if (listener!=null)
                    {
                        BFirebaseInterface.selectEntity(u, new CompletionListenerWithDataAndError<BUser, Object>() {
                            @Override
                            public void onDone(BUser u) {
                                // Remove the user.
                                followers.remove(u);

                                // Notify that a user has been found.
                                listener.onItem(u);

                                // if no more users to found call on done.
                                if (followers.size() == 0)
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
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }
/*######################################################################################################*/
    /** Send a message,
     *  The message need to have a owner thread attached to it or it cant be added.
     *  If the destination thread is public the system will add the user to the message thread if needed.
     *  The uploading to the server part can bee seen her {@see BFirebaseNetworkAdapter#PushMessageWithComplition}.*/
    @Override
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
                        new PushMessageWithCompletion(message, listener);
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

                new PushMessageWithCompletion(message, listener);
            }
        } else if (DEBUG) Log.e(TAG, "Message doesn't have an owner thread.");
    }

    /** Push the message to the firebase server and update the thread. */
    private class PushMessageWithCompletion {

        public PushMessageWithCompletion(final BMessage message, final CompletionListenerWithData<BMessage> listener){
            if (DEBUG) Log.v(TAG, "PushMessageWithCompletion");

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
                        listener.onDoneWithError((BError) error);
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
            if (listener!=null)
                listener.onItemError(null, new BError(BError.Code.NULL, "Thread is null"));
            return;
        }

        final List<BUser> usersToGo = new ArrayList<BUser>(users);

        if (DEBUG) Log.d(TAG, "Users Amount: " + users.size());

        final RepetitiveCompletionListenerWithError repetitiveCompletionListener = new RepetitiveCompletionListenerWithError<BUser, FirebaseError>() {
            @Override
            public boolean onItem(BUser user) {

                usersToGo.remove(user);

                if (DEBUG) Log.d(TAG,"addUsersToThread, OnItem, Users TO go: " + usersToGo.size() );

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
                if (listener!=null)
                    listener.onItemError(null, new BError(BError.Code.NULL, "User is null"));
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
                } else completionListener.onDoneWithError(BError.getFirebaseError(error));
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



    /*PUSH*/
    private void pushForMessage(final BMessage message){
        if (DEBUG) Log.v(TAG, "pushForMessage");
        if (message.getBThreadOwner().getType() == BThread.Type.Private) {

            // Loading the message from firebase to get the timestamp from server.
            FirebasePaths firebase = FirebasePaths.threadRef(
                    message.getBThreadOwner().getEntityID())
                    .appendPathComponent(BFirebaseDefines.Path.BMessagesPath)
                    .appendPathComponent(message.getEntityID());

            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Long date = null;
                    try {
                        date = (Long) snapshot.child(BDefines.Keys.BDate).getValue();
                    } catch (ClassCastException e) {
                        date = (((Double)snapshot.child(BDefines.Keys.BDate).getValue()).longValue());
                    }
                    finally {
                        if (date != null)
                        {
                            Log.d(TAG, "Setting new date.");
                            message.setDate(new Date(date));
                            DaoCore.updateEntity(message);
                        }
                    }

                    // If we failed to get date dont push.
                    if (message.getDate()==null)
                        return;

                    BUser currentUser = currentUser();
                    List<BUser> users = new ArrayList<BUser>();

                    for (BUser user : message.getBThreadOwner().getUsers())
                        if (!user.equals(currentUser))
                            if (user.getOnline() == null || !user.getOnline())
                            {
                                if (DEBUG) Log.d(TAG, "Pushing message.");
                                users.add(user);
                            }

                    pushToUsers(message, users);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void pushToUsers(BMessage message, List<BUser> users){
        if (DEBUG) Log.v(TAG, "pushToUsers");

        if (!pushEnabled() || users.size() == 0)
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

        PushService.subscribe(context, channel, ChatSDKUiHelper.getInstance().mainActivity);
    }

    public void unsubscribeToPushChannel(String channel){
        if (!pushEnabled())
            return;

        PushService.unsubscribe(context, channel);
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
