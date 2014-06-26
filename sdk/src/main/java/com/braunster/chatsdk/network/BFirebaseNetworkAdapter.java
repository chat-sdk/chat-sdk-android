package com.braunster.chatsdk.network;

import android.util.Log;

import com.braunster.chatsdk.BuildConfig;
import com.braunster.chatsdk.dao.BLinkData;
import com.braunster.chatsdk.dao.BLinkedAccount;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.core.Entity;
import com.braunster.chatsdk.dao.entities.BLinkedAccountEntity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListener;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.firebase.BFirebaseDefines;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.braunster.chatsdk.network.BDefines.*;
import static com.braunster.chatsdk.network.BDefines.BAccountType.*;

/**
 * Created by braunster on 23/06/14.
 */
public class BFirebaseNetworkAdapter extends AbstractNetworkAdapter {

    private static final String TAG = BFirebaseNetworkAdapter.class.getSimpleName();
    private static boolean DEBUG = true;

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

    @Override // Note done!
    public void authenticateWithMap(Map<String, Object> details, final CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object> listener) {

        Firebase ref = FirebasePaths.firebaseRef();
        SimpleLogin simpleLogin = new SimpleLogin(ref);

        SimpleLoginAuthenticatedHandler handler = new SimpleLoginAuthenticatedHandler() {
            @Override
            public void authenticated(FirebaseSimpleLoginError firebaseSimpleLoginError, final FirebaseSimpleLoginUser firebaseSimpleLoginUser) {
                if (firebaseSimpleLoginError != null || firebaseSimpleLoginUser == null)
                    listener.onDoneWithError(firebaseSimpleLoginUser, firebaseSimpleLoginError);
                else handleFAUser(firebaseSimpleLoginUser, new CompletionListenerWithDataAndError<BUser, Object>() {
                    @Override
                    public void onDone(BUser user) {
                        listener.onDone(firebaseSimpleLoginUser);
                    }

                    @Override
                    public void onDoneWithError(BUser bUser, Object o) {
                        listener.onDoneWithError(null, o);
                    }
                });
            }
        };

        switch ((Integer)details.get(Prefs.LoginTypeKey))
        {
            case Facebook:
                // TODO add premission if using this system of connecting, More reasnoble approach is to user the FBManager.
                //Note we have to obtain the access token first so i will change this to work with the FBManager.
                simpleLogin.loginWithFacebook(com.braunster.chatsdk.network.BDefines.FacebookAppId, (String) details.get(BFacebookManager.ACCESS_TOKEN), handler);
                break;

            case Twitter:
                // TODO get twitter app id and etc.
                simpleLogin.loginWithTwitter(com.braunster.chatsdk.network.BDefines.TwitterApiKey, "", 0L, handler);
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

    //Note Done!
    public void handleFAUser(final FirebaseSimpleLoginUser fuser, final CompletionListenerWithDataAndError<BUser, Object> listener){
        if (fuser == null)
        {
            // If the user isn't authenticated they'll need to login
            listener.onDoneWithError(null, null);
            return;
        }

        // Flag that the user has been authenticated
        setAuthenticated(true);

        String token = (String) fuser.getThirdPartyUserData().get("accessToken");

        String aid = BUser.safeAuthenticationID(fuser.getUserId(), fuser.getProvider());

        // Save the authentication ID for the current user
        // Set the current user
        Map<String, Object> loginInfoMap = new HashMap<String, Object>();
        loginInfoMap.put(Prefs.AuthenticationID, aid);
        loginInfoMap.put(Prefs.AccountTypeKey, fuser.getProvider().ordinal());
        setLoginInfo(loginInfoMap);

        final BUser user = DaoCore.fetchOrCreateUserWithAuthinticationID(aid);
        user.setAuthenticationType(fuser.getProvider().ordinal());

        BFirebaseInterface.selectEntity(user,
                new CompletionListenerWithDataAndError<BUser, FirebaseError>() {
                    @Override
                    public void onDone(BUser buser) {
                        //ASK should i update the entity found? or the one that i already had(Which doens't make to much sense i know).
                        // ASK if should i check for null?

                        if(buser == null)
                        {
                            if (DEBUG) Log.e(TAG, "User from select entity is null");
                        }
                        else
                        {
                            if (DEBUG) Log.d(TAG, "User from select entity is initialized.");
                        }

                        updateUserFromFUser(buser, fuser);

                        DaoCore.updateEntity(buser);

                        listener.onDone(buser);
                    }

                    @Override
                    public void onDoneWithError(BUser bUser, FirebaseError error) {
                        listener.onDoneWithError(bUser, error);
                    }
                });
    }

    //Note Done!
    /**Copy some details from the FAUser like name etc...*/
    public void updateUserFromFUser(final BUser user, FirebaseSimpleLoginUser fireUser){
        Map <String, Object> thirdPartyData = fireUser.getThirdPartyUserData();
        String name;
        String email;
        BLinkedAccount linkedAccount;

        switch (fireUser.getProvider())
        {
            case FACEBOOK:
                // Setting the name.
                name = (String) thirdPartyData.get(BFacebookManager.DISPLAY_NAME);
                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                    user.setMetaName(name);

                // Setting the email.//TODO get email
                email = (String) thirdPartyData.get(BFacebookManager.DISPLAY_NAME);
                if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(user.getMetaEmail()))
                    user.setMetaEmail(email);

                linkedAccount = user.getAccountWithType(BLinkedAccount.Type.FACEBOOK);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.FACEBOOK);
                    linkedAccount.setUser(user.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(BFacebookManager.ACCESS_TOKEN));

                break;

            case TWITTER:
                // Setting the name.
                name = (String) thirdPartyData.get(BFacebookManager.DISPLAY_NAME);
                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                    user.setMetaName(name);

                linkedAccount = user.getAccountWithType(BLinkedAccount.Type.TWITTER);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.TWITTER);
                    linkedAccount.setUser(user.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(BFacebookManager.ACCESS_TOKEN));

                break;

            case PASSWORD:
                email = fireUser.getEmail();
                if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(user.getMetaEmail()))
                    user.setMetaEmail(email);
                break;

            default: break;
        }

        // ASK is this draw a bitmap with his name panted?
        /*// If the user doesn't have an image generate a temporay image
        if (!user.picture) {
            user.picture = [JSQMessagesAvatarFactory standardAvatarWithName:user.name];
        }*/

        // Message Color.
        if (StringUtils.isEmpty(user.getMessageColor()))
        {
            if (StringUtils.isNotEmpty(BFirebaseDefines.Defaults.MessageColor))
                user.setMessageColor(BFirebaseDefines.Defaults.MessageColor);
            /*else {
                user.messageColor = [BMessage colorToString:[BMessage randomColor]];
            }*/
            // TODO else get random color from BMessage class.
        }


        // Text Color.
        if (StringUtils.isEmpty(user.getTextColor()))
        {
            //TODO get random color from BMessage class.
        }

        // Font name.
        if (StringUtils.isEmpty(user.getFontName()))
        {
            if (StringUtils.isNotEmpty(BFirebaseDefines.Defaults.MessageFontName))
                user.setFontName(BFirebaseDefines.Defaults.MessageFontName);
            /*else {
                user.messageFontName = bSystemFont;
            }*/
            //ASK do i need to change the roboto font if there isnt a default?
        }

        // Font size.
        if (user.getFontSize() == null || user.getFontSize() == 0){
            user.setFontSize(BFirebaseDefines.Defaults.MessageFontSize);
        }

        // Save the data
        DaoCore.updateEntity(user);

        pushUserWithCallback(new CompletionListener() {
            @Override
            public void onDone() {
                if (DEBUG) Log.i(TAG, "User pushed After update from FUser, EntityID: " + currentUser().getEntityID());

                FirebasePaths userOnlineRef = FirebasePaths.userOnlineRef(currentUser().getEntityID());

                // Set the current state of the user as online.
                // And add a listener so when the user log off from firebase he will be set as disconnected.
                userOnlineRef.setValue(true);
                userOnlineRef.onDisconnect().setValue(false);

                //ASK why saving nothing is changed locally?

                BFirebaseInterface.observerUser(currentUser());

                //TODO subscribe to push notifications.
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "Failed to push user After update from FUser");
                //ASK need to do something if an error happend?
            }
        });
    }

    @Override //Note Done!
    /** Unlike the iOS code the current user need to be saved before you call this method.*/
    public void pushUserWithCallback(final CompletionListener listener) {
        // Update the user in the server.
        BFirebaseInterface.pushEntity(currentUser(), new RepetitiveCompletionListenerWithError() {
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

    @Override //Note Done!
    public void checkUserAuthenticatedWithCallback(final CompletionListenerWithDataAndError<BUser, Object> listener) {

        //    if ([lastLoginInfo[bAccountTypeKey] intValue] == bAccountTypeFacebook) {
//        [[BFacebookManager sharedManager] authenticateWithCompletion:^(NSError * error, BOOL success) {
//            if (success) {
//                BUser * user = [[BCoreDataManager sharedManager] fetchOrCreateUserWithEntityID:bUserEntity withAuthenticationID:lastLoginInfo[bAuthenticationIDKey]];
//                completion(Nil, user);
//            }
//            else {
//                completion(error, Nil);
//            }
//        }];
//    }
//    // Otherwise use Firebase's simple login
//    else {

        SimpleLogin simpleLogin = new SimpleLogin(FirebasePaths.firebaseRef());
        simpleLogin.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
            @Override
            public void authenticated(FirebaseSimpleLoginError firebaseSimpleLoginError, FirebaseSimpleLoginUser firebaseSimpleLoginUser) {
                if (firebaseSimpleLoginError != null || firebaseSimpleLoginUser == null)
                    listener.onDoneWithError(null, firebaseSimpleLoginError);
                else handleFAUser(firebaseSimpleLoginUser, listener);
            }
        });
    }

    @Override //Note Done!
    public void logout() {

        setAuthenticated(false);

        // Stop listening to user related alerts. (added message or thread.)
        BFirebaseInterface.removeAllObservers();

        // Obtaining the simple login object from the ref.
        SimpleLogin simpleLogin = new SimpleLogin(FirebasePaths.firebaseRef());

        // TODO check why the online flag does not change after logout.
        // Login out
        simpleLogin.logout();
 /*      TODO
        // Also log out of Facebook
        //[[BFacebookManager sharedManager] logout];

        // Post a notification
        [[NSNotificationCenter defaultCenter] postNotificationName:bLogoutNotification object:Nil];
*/
    }

    @Override //Note Done!
    public void usersForIndex(String index, final RepetitiveCompletionListener<BUser> listener) {
        mapForIndex(index, new MapForIndex() {
            @Override
            public void Completed(Firebase ref, String index, Map<String, Object> values) {
                if (ref == null && values == null)
                {
                    if (DEBUG) Log.e(TAG, "Error occurred while fetching the map for the index.");
                    return;
                }

                final List<BUser> usersToGo = new ArrayList<BUser>();
                List<String> keys = new ArrayList<String>();

                // So we dont have to call the db for each key.
                String currentUserEntityID= currentUser().getEntityID();

                // Adding all keys to the list, Except the current user key.
                for (String key : values.keySet())
                    if (!key.equals(currentUserEntityID))
                        keys.add(key);


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


/*######################################################################################################*/
    /* Indexing
    * To allow searching we're going to implement a simple index. Strings can be registered and
    * associated with users i.e. if there's a user called John Smith we could make a new index
    * like this:
    *
    * indexes/[index ID (priority is: johnsmith)]/[entity ID of John Smith]
    *
    * This will allow us to find the user*/
    /**This method get the map values of data stored at a particular index*/ //Note Done!
    private void mapForIndex(String index, final MapForIndex mapForIndex){
        FirebasePaths indexRef = FirebasePaths.indexRef();

        // Remove spaces from string and make it lower case
        index = index.replace(" ", "");
        index.toLowerCase();

        Query query = indexRef.startAt(index).endAt(index);

        final String finalIndex = index;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot != null && snapshot.getValue() != null && snapshot.hasChildren())
                {
                    // Check to see if this user is already registered

                    for (DataSnapshot child : snapshot.getChildren())
                    {
                        // The child will contain a dictionary of users i.e. [user ID] => name => [user name]
                        if (child.getValue() != null)
                        {
                            // Return the index location and the value at the index
                            mapForIndex.Completed(child.getRef(), finalIndex, (Map<String, Object>) child.getValue());
                        }
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
            /*

                // This kind of query will always return a list of children
                // there should only be one! This is the index location

                for (FDataSnapshot * child in snapshot.children) {

                    // The child will contain a dictionary of users i.e. [user ID] => name => [user name]

                    // Check to see if this user is already registered
                    if (child.value && ![child.value isEqual: [NSNull null]]) {

                        NSMutableDictionary * dict = [NSMutableDictionary dictionaryWithDictionary:child.value];

                        // Return the index location and the value at hte index
                        completion(child.ref, index, dict);

                    }
                    break;
                }
            }
    }*/
    }

    /** Interface to return values from the <b>mapForIndex</b> method.*///Note Done!
    public interface MapForIndex{
        public void Completed(Firebase ref, String index, Map<String, Object> values);
    }

    @Override// Note done!
    public void removeUserFromIndex(final BUser user, String index, final CompletionListener listener) {
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

    @Override //Note Done!
    public void addUserToIndex(final BUser user, String index, CompletionListener listener) {
        // We don't want to index null strings!
        index = index.replace(" ", "");

        if (StringUtils.isEmpty(index))
        {
            if (DEBUG) Log.e(TAG, "Index is empty");
            if (listener != null)
                listener.onDoneWithError();
        }

        mapForIndex(index, new MapForIndex() {
            @Override
            public void Completed(Firebase ref, String index, Map<String, Object> values) {
                if (ref == null || values == null)
                {
                    if (DEBUG) Log.e(TAG, "Error occurred while fetching the map for the index.");
                    return;
                }

                //ASK not sure what is going on with this method below.
                /*dict[user.entityID] = @{b_Value: index_};

                [ref setValue:dict andPriority:index_ withCompletionBlock:^(NSError * error, Firebase * firebase) {
                    if (completion != Nil) {
                        completion(error);
                    }
                }];*/
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

    @Override//Note Done!
    public BUser currentUser() {
        if (getCurrentUserAuthenticationId() != null)
            return DaoCore.fetchOrCreateUserWithAuthinticationID(getCurrentUserAuthenticationId());

        return null;
    }

    /** Send a message,
     *  The message need to have a owner thread attached to it or it cant be added.
     *  If the destination thread is public the system will add the user to the message thread if needed.
     *  The uploading to the server part can bee seen her @see BFirebaseNetworkAdapter#PushMessageWithComplition.*/
      @Override //Note done!
    public void sendMessage(final BMessage message, final CompletionListenerWithData<BMessage> listener) {
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
            else new PushMessageWithComplition(message, listener);
        } else if (DEBUG) Log.e(TAG, "Message doesn't have an owner thread.");
    }

    /** Push the message to the firebase server and update the thread. */ //Note Done!
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

                    if(listener != null)
                        listener.onDone(message);

                    return false;
                }

                @Override
                public void onDone() {

                }

                @Override
                public void onItemError(Object o, Object o2) {
                    if(listener != null)
                        listener.onDoneWithError();
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
    @Override //Note done!
    public void createThreadWithUsers(String name, final List<BUser> users, final RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object> listener) {

        BUser currentUser = currentUser();

        final BThread thread = new BThread();
        thread.setCreationDate(new Date());
        thread.setCreator(currentUser);

        // If we're assigning users then the thread is always going to be private
        thread.setType(BThread.Type.Private);

        BFirebaseInterface.pushEntity(thread, new RepetitiveCompletionListenerWithError<BThread, Object>() {
            @Override
            public boolean onItem(BThread bThread) {
                // Thread is added successfully

                // Save the thread to the local db.
                DaoCore.createEntity(thread);

                // Report back that the thread is added.
                listener.onMainFinised(thread, null);

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
                // return null instead of the thread because the listener expect BUser item as a return value.
                listener.onItemError(null , error);
            }
        });

        updateLastOnline();
    }

    @Override //Note done!
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

                listener.onDone(thread);
                return false;
            }

            @Override
            public void onDone() {

            }

            @Override
            public void onItemError(BThread bThread, FirebaseError error) {
                if (DEBUG) Log.e(TAG, "Failed to push public thread.");
                DaoCore.deleteEntity(bThread);
                listener.onDoneWithError(thread, error);
            }
        });
    }

    /** Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.*/
    @Override //Note done!
    public void addUsersToThread(final BThread thread, final List<BUser> users, final RepetitiveCompletionListenerWithError<BUser, Object> listener) {
        final List<BUser> usersToGo = new ArrayList<BUser>(users);

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
            // Add the user to the thread
            if (!user.getThreads().contains(thread))
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

    //Note done!
    /** adds a thread to the user */
    private void addUserToThread(final BThread thread, final BUser user, final RepetitiveCompletionListenerWithError<BUser, Object> listener){

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

                    Map<String , Object> values = new HashMap<String, Object>();
                    values.put(Keys.BName, user.getName());
                    threadRef.setValue(values, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError error, Firebase firebase) {
                            if (error == null)
                            {
                                listener.onItem(user);
//                                listener.onDone();
                                //Note this done caued double message sending because its affect the repetitive listner which should call done when entities are all added.
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

    @Override // Note done!
    public void loadMoreMessagesForThread(BThread thread, CompletionListenerWithData<List<BMessage>> listener) {
        BFirebaseInterface.loadMoreMessagesForThread(thread, BFirebaseDefines.NumberOfMessagesPerBatch, listener);
    }

    @Override //Note done!
    public void deleteThreadWithEntityID(String entityID, CompletionListener completionListener) {

        final BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, entityID);

        BUser user = currentUser();

        FirebasePaths threadRef = FirebasePaths.firebaseRef();
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, threadRef: " + threadRef.toString());
        threadRef = threadRef.appendPathComponent(thread.getPath().getPath());
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, threadRef: " + threadRef.toString());

        FirebasePaths messageRef = FirebasePaths.firebaseRef();
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, messageRef: " + messageRef.toString());
        messageRef = threadRef.appendPathComponent(BFirebaseDefines.Path.BMessagesPath);
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, messageRef: " + messageRef.toString());

        FirebasePaths threadUsersRef = FirebasePaths.firebaseRef();
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, messageRef: " + threadUsersRef.toString());
        threadUsersRef = threadRef.appendPathComponent(BFirebaseDefines.Path.BUsersPath);
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, messageRef: " + threadUsersRef.toString());

        /* TODO remove all data listeners form the reference.
           TODO All assign listeners need to be saved in this class so we vould unregister them here.
        [messagesRef removeAllObservers];
        [threadUsersRef removeAllObservers];*/

        FirebasePaths userThreadRef = FirebasePaths.firebaseRef();
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, messageRef: " + threadUsersRef.toString());
        userThreadRef = userThreadRef.appendPathComponent(user.getPath().getPath()).appendPathComponent(thread.getPath().getPath());
        if (DEBUG) Log.d(TAG, "deleteThreadWithEntityID, messageRef: " + threadUsersRef.toString());
        userThreadRef.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError error, Firebase firebase) {
                // Delete the thread if no error occurred when deleting from firebase.
                if (error == null)
                    DaoCore.deleteEntity(thread);
            }
        });

        updateLastOnline();
    }

    @Override//ASK this method is missing in the firebse adapter.
    public void deleteThread(BThread thread, CompletionListener completionListener) {

    }

    @Override //Note done!
    public String getServerURL() {
        return FirebasePaths.FIREBASE_PATH;
    }

    @Override // Note done
    public void setLastOnline(Date lastOnline) {
        this.currentUser().setLastOnline(lastOnline);
        // TODO push entity back to the firebase server.
    }

    //Note done!
    private void updateLastOnline(){
        setLastOnline(new Date());
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

+(BOOL) pushEnabled {
    return bParseAppKey.length && bParseClientKey.length;
}

-(void) subscribeToPushChannel: (NSString *) channel {
    if (![BFirebaseNetworkAdapter pushEnabled]) {
        return;
    }
    PFInstallation *currentInstallation = [PFInstallation currentInstallation];
    [currentInstallation addUniqueObject:channel forKey:@"channels"];
    [currentInstallation saveInBackground];
}

// Check when recipients was last online
// Don't use push notifications for public threads because
// they could have hundreds of users and we don't want to be spammed
// with push notifications
-(void) pushForMessage: (BMessage *) message {
    if (message.thread.type.intValue == bThreadTypePrivate) {
        for (BUser * user in message.thread.users) {
            if (![user isEqual:self.currentUser]) {
                if(!user.online.boolValue) {
                    NSLog(@"Sending push to: %@", user.name);
                    [self pushToUsers:@[user] withMessage:message];
                }
            }
        }
    }
}

-(void) pushToUsers: (NSArray *) users withMessage: (BMessage *) message {
    if (![BFirebaseNetworkAdapter pushEnabled]) {
        return;
    }

    // We're identifying each user using push channels. This means that
    // when a user signs up, they register with parse on a particular
    // channel. In this case user_[user id] this means that we can
    // send a push to a specific user if we know their user id.
    NSMutableArray * userChannels = [NSMutableArray new];
    for (BUser * user in users) {
        if(![user isEqual:self.currentUser])
            [userChannels addObject:user.pushChannel];
    }

    // Format the message that we're going to push
    NSString * text = message.text;

    if (message.type.intValue == bMessageTypeLocation) {
        text = @"Location message!";
    }
    if (message.type.intValue == bMessageTypeImage) {
        text = @"Picture message!";
    }
    text = [NSString stringWithFormat:@"%@: %@", message.user.name, text];

    // Send the push message to the users specified
    PFPush * push = [[PFPush alloc] init];
    [push setChannels:userChannels];
    [push setData:@{@"alert":text,
                    @"badge":@"Increment"}];
    [push sendPushInBackground];
}
*/
}
