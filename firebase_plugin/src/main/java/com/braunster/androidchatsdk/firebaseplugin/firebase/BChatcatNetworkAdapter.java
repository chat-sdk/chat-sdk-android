/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.content.Context;

import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BMessageWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BThreadWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.sorter.UsersSorter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.BUserConnection;
import com.braunster.chatsdk.dao.BUserDao;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BThreadEntity;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.object.BError;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterDeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.greenrobot.dao.query.QueryBuilder;
import jdeferred.android.AndroidDeferredObject;
import jdeferred.android.AndroidExecutionScope;
import timber.log.Timber;

import static com.braunster.chatsdk.network.BDefines.Keys;
import static com.braunster.chatsdk.network.BDefines.Prefs;

public class BChatcatNetworkAdapter extends BFirebaseNetworkAdapter {

    private static final String TAG = BChatcatNetworkAdapter.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    public BChatcatNetworkAdapter(Context context){
        super(context);
    }




    public Promise<BUser, BError, Void> handleFAUser(final AuthData authData){
        if (DEBUG) Timber.v("handleFAUser");

        final Deferred<BUser, BError, Void> deferred = new DeferredObject<>();
        
        authingStatus = AuthStatus.HANDLING_F_USER;

        if (authData == null)
        {
            resetAuth();
            // If the user isn't authenticated they'll need to login
            deferred.reject(new BError(BError.Code.SESSION_CLOSED));
        }
        else
        {
            // Flag that the user has been authenticated
            setAuthenticated(true);

            String token = authData.getToken();
            String uid = authData.getUid();

            // Save the authentication ID for the current user
            // Set the current user
            final Map<String, Object> loginInfoMap = new HashMap<String, Object>();
            loginInfoMap.put(Prefs.AuthenticationID, uid);
            loginInfoMap.put(Prefs.AccountTypeKey, FirebasePaths.providerToInt(authData.getProvider()));
            loginInfoMap.put(Prefs.TokenKey, token);

            setLoginInfo(loginInfoMap);

            // Doint a once() on the user to push its details to firebase.
            final BUserWrapper wrapper = BUserWrapper.initWithEntityId(uid);
            
            wrapper.metaOnce().then(new DoneCallback<BUser>() {
                @Override
                public void onDone(BUser bUser) {

                    if (DEBUG) Timber.v("OnDone, user was pulled from firebase.");

                    wrapper.updateUserFromAuthData(authData);

                    getEventManager().userOn(bUser);

                    wrapper.pushMeta().done(new DoneCallback<BUser>() {
                        @Override
                        public void onDone(BUser u) {
                            if (DEBUG) Timber.v("OnDone, user was pushed from firebase.");
                            resetAuth();

                            goOnline();
                            deferred.resolve(u);
                        }
                    }).fail(new FailCallback<BError>() {
                        @Override
                        public void onFail(BError error) {
                            resetAuth();

                            deferred.reject(error);
                        }
                    });
                }
            }, new FailCallback<BError>() {
                @Override
                public void onFail(BError bError) {
                    deferred.reject(bError);
                }
            });
        }

        return deferred.promise();

    }

    @Override
    public Promise<BUser, BError, Void> checkUserAuthenticated() {
        if (DEBUG) Timber.v("checkUserAuthenticatedWithCallback, %s", getLoginInfo().get(Prefs.AccountTypeKey));

        final Deferred<BUser, BError, Void> deferred = new DeferredObject<>();

        if (isAuthing())
        {
            if (DEBUG) Timber.d("Already Authing!, Status: %s", authingStatus.name());

            deferred.reject(BError.getError(BError.Code.AUTH_IN_PROCESS, "Cant run two auth in parallel"));
        }
        else
        {
            authingStatus = AuthStatus.CHECKING_IF_AUTH;

            if (!getLoginInfo().containsKey(Prefs.AccountTypeKey))
            {
                if (DEBUG) Timber.d(TAG, "No account type key");

                resetAuth();
                deferred.reject(new BError(BError.Code.NO_LOGIN_INFO));
            }


            Firebase ref = FirebasePaths.firebaseRef();
            if (ref.getAuth()!=null)
            {
                handleFAUser(ref.getAuth()).done(new DoneCallback<BUser>() {
                    @Override
                    public void onDone(BUser bUser) {
                        resetAuth();
                        deferred.resolve(bUser);
                    }
                }).fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError bError) {
                        resetAuth();
                        deferred.reject(bError);
                    }
                });
            }
            else{
                resetAuth();
                deferred.reject(BError.getError(BError.Code.NO_AUTH_DATA, "No auth data found"));
            }
        }

        return deferred.promise();
    }

    @Override
    public Promise<Void, BError, Void> changePassword(String email, String oldPassword, String newPassword){
        
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();
        
        Firebase.ResultHandler resultHandler = new Firebase.ResultHandler(){

            @Override
            public void onSuccess() {
                deferred.resolve(null);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                deferred.reject(getFirebaseError(firebaseError));
            }
        };

        FirebasePaths.firebaseRef().changePassword(email, oldPassword, newPassword, resultHandler);

        return deferred.promise();
    }

    @Override
    public Promise<Void, BError, Void> sendPasswordResetMail(String email){
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();
        
        Firebase.ResultHandler resultHandler = new Firebase.ResultHandler(){
            @Override
            public void onSuccess() {
                deferred.resolve(null);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                deferred.reject(getFirebaseError(firebaseError));
            }
        };

        FirebasePaths.firebaseRef().resetPassword(email, resultHandler);
        
        return deferred.promise();
    }
    
    @Override
    /** Unlike the iOS code the current user need to be saved before you call this method.*/
    public Promise<BUser, BError, Void> pushUser() {
        return currentUser().pushMeta();
    }

    @Override
    public BUser currentUserModel() {
        String authID = getCurrentUserAuthenticationId();
        if (StringUtils.isNotEmpty(authID))
        {
            BUser currentUser = DaoCore.fetchEntityWithEntityID(BUser.class, authID);

            if(DEBUG) {
                if (currentUser == null) Timber.e("Current user is null");
                else if (StringUtils.isEmpty(currentUser.getEntityID())) 
                    Timber.e("Current user entity id is null");
            }

            return currentUser;
        }
        if (DEBUG) Timber.e("getCurrentUserAuthenticationIdr is null");
        return null;
    }
    
    public BUserWrapper currentUser(){
        return BUserWrapper.initWithModel(currentUserModel());
    }

    @Override
    public void logout() {

        BUser user = currentUserModel();

        Firebase.goOffline();

        /* No need to logout from facebook due to the fact that the logout from facebook event will trigger this event.
        *  The logout from fb is taking care of by the fb login button.*/
        setAuthenticated(false);

        // Stop listening to user related alerts. (added message or thread.)
        getEventManager().userOff(user);

        // Obtaining the simple login object from the ref.
        FirebasePaths ref = FirebasePaths.firebaseRef();

        // Login out
        if (user != null)
        {
            Firebase userOnlineRef = FirebasePaths.userOnlineRef(user.getEntityID());
            userOnlineRef.setValue(false);
        }

        ref.unauth();
    }

    @Override
    public void setUserOnline() {
        BUser current = currentUserModel();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            currentUser().goOnline();
        }
    }

    @Override
    public void goOffline() {
        Firebase.goOffline();
    }

    @Override
    public void goOnline() {
        Firebase.goOnline();
        
        setUserOnline();
    }

    public Promise<Boolean, BError, Void> isOnline(){

        final Deferred<Boolean, BError, Void> deferred = new DeferredObject<>();

        if (currentUserModel() == null)
        {
            return  deferred.reject(BError.getError(BError.Code.NULL, "Current user is null"));
        }

        FirebasePaths.userOnlineRef(currentUserModel().getEntityID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                deferred.resolve(snapshot.getValue() != null);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                deferred.reject(getFirebaseError(firebaseError));
            }
        });

        return deferred.promise();
    }

    /** Send a message,
     *  The message need to have a owner thread attached to it or it cant be added.
     *  If the destination thread is public the system will add the user to the message thread if needed.
     *  The uploading to the server part can bee seen her {@see BFirebaseNetworkAdapter#PushMessageWithComplition}.*/
    @Override
    public Promise<BMessage, BError, BMessage> sendMessage(final BMessage message){
        if (DEBUG) Timber.v("sendMessage");
        
        return new BMessageWrapper(message).send().done(new DoneCallback<BMessage>() {
            @Override
            public void onDone(BMessage message) {

                BThreadWrapper threadWrapper = new BThreadWrapper(message.getThread());

                threadWrapper.setLastMessage(message);
                threadWrapper.updateStateWithKey(BFirebaseDefines.Path.BMessages);

                // Pushing the message to all offline users. we cant push it before the message was
                // uploaded as the date is saved by the firebase server using the timestamp.
                pushForMessage(message);
            }
        });
    }

    /** Indexing
     * To allow searching we're going to implement a simple index. Strings can be registered and
     * associated with users i.e. if there's a user called John Smith we could make a new index
     * like this:
     *
     * indexes/[index ID (priority is: johnsmith)]/[entity ID of John Smith]
     *
     * This will allow us to find the user*/
    @Override
    public Promise<List<BUser>, BError, Integer> usersForIndex(final String index, final String value) {
        
        final Deferred<List<BUser>, BError, Integer> deferred = new DeferredObject<>();

        if (StringUtils.isBlank(value))
        {
            return deferred.reject(BError.getError(BError.Code.NULL, "Value is blank"));
        }

        Query query = FirebasePaths.searchIndexRef().orderByChild(index).startAt(
                processForQuery(value)).limitToFirst(BFirebaseDefines.NumberOfUserToLoadForIndex);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    
                    Map<String, Objects> values = (Map<String, Objects>) snapshot.getValue();

                    final List<BUser> usersToGo = new ArrayList<BUser>();
                    List<String> keys = new ArrayList<String>();

                    // So we dont have to call the db for each key.
                    String currentUserEntityID = currentUserModel().getEntityID();

                    // Adding all keys to the list, Except the current user key.
                    for (String key : values.keySet())
                        if (!key.equals(currentUserEntityID))
                            keys.add(key);

                    // Fetch or create users in the local db.
                    BUser bUser;
                    if (keys.size() > 0) {
                        for (String entityID : keys) {
                            // Making sure that we wont try to get users with a null object id in the index section
                            // If we will try the query will never end and there would be no result from the index.
                            if(StringUtils.isNotBlank(entityID) && !entityID.equals(Keys.BNull) && !entityID.equals("(null)"))
                            {
                                bUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityID);
                                usersToGo.add(bUser);
                            }
                        }

                        Promise[] promises = new Promise[keys.size()];
                        
                        int count = 0;
                        for (final BUser user : usersToGo) {
                            
                            final Deferred<BUser, BError, Integer>  d = new DeferredObject<>();

                            BUserWrapper.initWithModel(user)
                                    .once()
                                    .done(new DoneCallback<BUser>() {
                                        @Override
                                        public void onDone(BUser bUser) {
                                            if (DEBUG)
                                                Timber.d("onDone, index: %s, Value: %s", index, value);

                                            // Notify that a user has been found.
                                            // Making sure the user due start with the wanted name
                                            if (processForQuery(bUser.metaStringForKey(index)).startsWith(processForQuery(value))) {
                                                d.resolve(bUser);
                                            } 
                                            else {
                                                if (DEBUG) 
                                                    Timber.d("Not valid result, " +
                                                        "index: %s, UserValue: %s Value: %s", index, bUser.metaStringForKey(index), value);

                                                // Remove the not valid user from the list.
                                                usersToGo.remove(user);

                                                d.resolve(null);
                                            }
                                        }
                                    })
                                    .fail(new FailCallback<BError>() {
                                        @Override
                                        public void onFail(BError bError) {
                                            if (DEBUG) Timber.e("usersForIndex, onDoneWithError.");
                                            // Notify that an error occurred while selecting.
                                            d.reject(bError);
                                        }
                                    });

                            promises[count] = d.promise();
                            count++;
                        }
                        
                        MasterDeferredObject masterDeferredObject = new MasterDeferredObject(promises);
                        
                        masterDeferredObject.progress(new ProgressCallback<MasterProgress>() {
                            @Override
                            public void onProgress(MasterProgress masterProgress) {
                                
                                if (DEBUG) Timber.d("MasterDeferredProgress, done: %s, failed: %s, total: %s", masterProgress.getDone(), masterProgress.getFail(), masterProgress.getTotal());

                                // Reject the promise if all promises failed.
                                if (masterProgress.getFail() == masterProgress.getTotal())
                                {
                                    deferred.reject(BError.getError(BError.Code.OPERATION_FAILED, "All promises failed"));
                                }
                                // If all was done lets resolve the promise.
                                else if (masterProgress.getFail() + masterProgress.getDone() == masterProgress.getTotal())
                                    deferred.resolve(usersToGo);
                            }
                        });

                        
                    } else deferred.reject(BError.getError(BError.Code.NO_USER_FOUND, "Unable to found user."));
                } else {
                    if (DEBUG) Timber.d("Value is null");
                    deferred.reject(BError.getError(BError.Code.NO_USER_FOUND, "Unable to found user."));
                }
            }

            
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                deferred.reject(getFirebaseError(firebaseError));
            }
        });
        
        return deferred.promise();
    }


    @Override
    public Promise<Void, BError, Void> updateIndexForUser(BUser user){
        return BUserWrapper.initWithModel(user).updateIndex();
    }

    @Override
    public Promise<List<BMessage>, Void, Void> loadMoreMessagesForThread(BThread thread) {
        return new BThreadWrapper(thread).loadMoreMessages(BFirebaseDefines.NumberOfMessagesPerBatch);
    }

    @Override
    public Promise<BThread, BError, Void> createPublicThreadWithName(String name) {
        
        final Deferred<BThread, BError, Void> deferred = new DeferredObject<>();

        // Crating the new thread.
        // This thread would not be saved to the local db until it is successfully uploaded to the firebase server.
        final BThread thread = new BThread();

        BUser curUser = currentUserModel();
        thread.setCreator(curUser);
        thread.setCreatorEntityId(curUser.getEntityID());
        thread.setType(BThread.Type.Public);
        thread.setName(name);
        thread.setUserCreated(true);
        thread.setInvitesEnabled(true);
        thread.setWeight(0);
        thread.setCreationDate(new Date());
        thread.setEntityID(FirebasePaths.threadRef().push().getKey());

        // Save the entity to the local db.
        DaoCore.createEntity(thread);

        Map<String, Object> data = new HashMap<>();
        data.put(Keys.BToken, token());
        data.put(Keys.BAPIKey, BDefines.BRootPath);
        data.put(BDefines.Keys.BName, name);
        data.put(BDefines.Keys.BType, thread.getType());
        data.put(BDefines.Keys.BCreatorEntityId, thread.getCreatorEntityId());
        data.put(BDefines.Keys.BRID, thread.getEntityID());
        data.put(BDefines.Keys.BDescription, "");
        data.put(BDefines.Keys.BUserCreated, thread.getUserCreated());
        data.put(BDefines.Keys.BInvitesEnabled, thread.getInvitesEnabled());
        data.put(BDefines.Keys.BWeight, thread.getWeight());

        ParseCloud.callFunctionInBackground("createRoom", data, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    if (DEBUG) Timber.d("public thread is pushed and saved.");

                    deferred.resolve(thread);
                } else {
                    DaoCore.deleteEntity(thread);
                    deferred.reject(getParseError(e));
                }
            }
        });

        return deferred.promise();
    }

    /** Create thread for given users.
     *  When the thread is added to the server the "onMainFinished" will be invoked,
     *  If an error occurred the error object would not be null.
     *  For each user that was successfully added the "onItem" method will be called,
     *  For any item adding failure the "onItemFailed will be called.
     *   If the main task will fail the error object in the "onMainFinished" method will be called."*/
    @Override
    public Promise<BThread, BError, Void> createThreadWithUsers(String name, final List<BUser> users) {

        final Deferred<BThread, BError, Void> deferred = new DeferredObject<>();
        
        BUser currentUser = currentUserModel();

        // Checking to see if this users already has a private thread.
        if (users.size() == 2)
        {
            if (DEBUG) Timber.d("Checking if already has a thread.");
            List<BUser> threadusers;

            BUser userToCheck;
            if (users.get(0).getEntityID().equals(currentUser.getEntityID()))
                userToCheck = users.get(1);
            else userToCheck = users.get(0);

            if (DEBUG) Timber.d("UserToCheck: %s", userToCheck.getEntityID());

            BThread deletedThreadFound = null;
            for (BThread t : currentUser.getThreads(BThreadEntity.Type.NoType, true))
            {
                // Skipping public threads and groups.
                if (t.isPublic() || t.isGroup())
                    continue;

                threadusers = t.getUsers();
                if (threadusers.size() == 2) {
                    if (threadusers.get(0).getEntityID().equals(userToCheck.getEntityID()) ||
                            threadusers.get(1).getEntityID().equals(userToCheck.getEntityID())) {

                        // If the thread is deleted we will look for other thread with the user. 
                        // if nothing found we will use the deleted thread and un delete it
                        if (t.isDeleted())
                        {
                            deletedThreadFound = t;
                        }
                        else 
                            return deferred.resolve(t);
                    }
                }
            }
            
            if (deletedThreadFound != null){
                
                new BThreadWrapper(deletedThreadFound).recoverThread();
                
                return deferred.resolve(deletedThreadFound);
            }
        }

        // Didnt find a new thread so we create a new.
        final BThread thread = new BThread();

        thread.setCreator(currentUser);
        thread.setCreatorEntityId(currentUser.getEntityID());
        thread.setEntityID(FirebasePaths.threadRef().push().getKey());
        thread.setUserCreated(true);
        thread.setInvitesEnabled(true);
        thread.setWeight(0);
        thread.setCreationDate(new Date());

        // If we're assigning users then the thread is always going to be private or a group
        thread.setType(users.size() < 3 ? BThreadEntity.Type.OneToOne : BThreadEntity.Type.Group);

        Timber.d("Creating new thread, UserAmount: %s, BThread: %s", users.size(), thread);

        // Save the thread to the database.
        DaoCore.createEntity(thread);

        Map<String, Object> data = new HashMap<>();
        data.put(Keys.BToken, token());
        data.put(Keys.BAPIKey, BDefines.BRootPath);
        data.put(BDefines.Keys.BRID, thread.getEntityID());
        data.put(BDefines.Keys.BType, thread.getType());
        data.put(BDefines.Keys.BName, "");
        data.put(BDefines.Keys.BDescription, "");
        data.put(BDefines.Keys.BUserCreated, thread.getUserCreated());
        data.put(BDefines.Keys.BInvitesEnabled, thread.getInvitesEnabled());
        data.put(BDefines.Keys.BWeight, thread.getWeight());
        data.put(BDefines.Keys.BCreatorEntityId, thread.getCreatorEntityId());

        ParseCloud.callFunctionInBackground("createRoom", data, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    if (DEBUG) Timber.d("parse function finished.");

                    // Add users, For each added user the listener passed here will get a call.
                    addUsersToThread(thread, users)
                            .done(new DoneCallback<BThread>() {
                                @Override
                                public void onDone(BThread thread) {
                                    deferred.resolve(thread);
                                }
                            })
                            .fail(new FailCallback<BError>() {
                                @Override
                                public void onFail(BError error) {
                                    deferred.reject(error);
                                }
                            });

                } else {
                    DaoCore.deleteEntity(thread);
                    deferred.reject(getParseError(e));
                }
            }
        });

        return deferred.promise();
    }

    /** Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.*/
    @Override
    public Promise<BThread, BError, Void> addUsersToThread(final BThread thread, final List<BUser> users) {

        // FIXME run on a background thread.
        final Deferred<BThread, BError, Void>  deferred = new DeferredObject<>();

        final AndroidDeferredObject<BThread, BError, Void> androidDeferredObject = new AndroidDeferredObject<BThread, BError, Void>(deferred, AndroidExecutionScope.BACKGROUND);

        if (thread == null)
        {
            if (DEBUG) Timber.e("addUsersToThread, Thread is null" );
            return deferred.reject(new BError(BError.Code.NULL, "Thread is null"));
        }

        JSONArray usersStatuses = new JSONArray();
        JSONObject jsonObject;
        for (BUser u : users)
        {
            jsonObject = new JSONObject();
            try {
                jsonObject.put(Keys.BUID, u.getEntityID());
                jsonObject.put(Keys.BStatus, u.equals(currentUserModel()) ? BDefines.BUserStatus.Owner : BDefines.BUserStatus.Member);
                usersStatuses.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Map<String, Object> data = new HashMap<>();

        data.put(Keys.BToken, token());
        data.put(Keys.BAPIKey, BDefines.BRootPath);
        data.put(Keys.BUsers, usersStatuses);
        data.put(Keys.BInvitedBy, currentUserModel().getEntityID());
        data.put(Keys.BRID, thread.getEntityID());

        if (DEBUG) Timber.d("Users Amount: %s", users.size());

        ParseCloud.callFunctionInBackground("addUsersToRoom", data, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    Timber.d("addUsersToRoom, Done.");
                    deferred.resolve(thread);
                } else
                    deferred.reject(getParseError(e));
            }
        });
        
        
        return androidDeferredObject.promise();
    }

    @Override
    public Promise<Void, BError, Void> deleteThreadWithEntityID(final String entityID) {

        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        Map<String, Object> data = new HashMap<>();
        data.put(Keys.BRID, entityID);
        data.put(Keys.BUID, currentUserModel().getEntityID());
        data.put(Keys.BToken, token());
        data.put(Keys.BAPIKey, BDefines.BRootPath);

        final BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, entityID);
        
        new BThreadWrapper(thread).deleteThread();

        ParseCloud.callFunctionInBackground("leaveRoom", data, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    deferred.resolve(null);
                } else
                    deferred.reject(getParseError(e));
            }
        });
        return deferred.promise();
    }

    @Override
    public Promise<Void, BError, Void> joinThread(final BThread thread) {
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        Map<String, Object> data = new HashMap<>();

        data.put(Keys.BRID, thread.getEntityID());
        data.put(Keys.BUID, currentUserModel().getEntityID());
        data.put(Keys.BToken, token());
        data.put(Keys.BAPIKey, BDefines.BRootPath);
        data.put(Keys.BStatus, BDefines.BUserStatus.Member);

        ParseCloud.callFunctionInBackground("joinRoom", data, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {

                if (e == null)
                {
                    // If it's a public thread then add the on-disconnect listener
                    if (thread.getType().equals(BThread.Type.Public))
                    {
                        Firebase threadUserRef = FirebasePaths.threadUserRef(thread.getEntityID(), currentUserModel().getEntityID());
                        threadUserRef.onDisconnect().removeValue();
                    }

                    deferred.resolve(null);
                }
                else
                {
                    deferred.reject(BFirebaseNetworkAdapter.getParseError(e));
                }
            }
        });

        return deferred.promise();
    }

    @Override
    public Promise<Void, BError, Void> leaveThread(final BThread thread) {

        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        Map<String, Object> data = new HashMap<>();

        data.put(Keys.BRID, thread.getEntityID());
        data.put(Keys.BUID, currentUserModel().getEntityID());
        data.put(Keys.BToken, token());
        data.put(Keys.BAPIKey, BDefines.BRootPath);

        ParseCloud.callFunctionInBackground("leaveRoom", data, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {

                if (e == null)
                {
                    deferred.resolve(null);
                }
                else
                {
                    deferred.reject(BFirebaseNetworkAdapter.getParseError(e));
                }
            }
        });

        return deferred.promise();
    }

    @Override
    public void startTypingOnThread(BThread thread, BUser user) {
        if (user == null)
            user = currentUserModel();

        if (user != null)
            new BThreadWrapper(thread).startTyping(user);
    }

    @Override
    public void finishTypingOnThread(BThread thread, BUser user) {
        if (user == null)
            user = currentUserModel();

        if (user != null)
            new BThreadWrapper(thread).finishTyping(user);
    }

    @NotNull
    @Override
    public List<BUser> onlineUsers() {
        List<BUser> onlineUsers;
        QueryBuilder<BUser> query = DaoCore.daoSession.queryBuilder(BUser.class);

        query.where(BUserDao.Properties.Online.isNotNull(), BUserDao.Properties.Online.eq(true));

        onlineUsers = query.list();


        if (onlineUsers == null)
            return new ArrayList<>();

        // Removing the current user if exist in the list.
        BUser u, curUser = currentUserModel();
        for (Iterator<BUser> iterator = onlineUsers.iterator(); iterator.hasNext();)
        {
            u = iterator.next();

            if (u.getEntityID().equals(curUser.getEntityID()))
                iterator.remove();
        }

        Collections.sort(onlineUsers, new UsersSorter());

        return onlineUsers;
    }

    @NotNull
    @Override
    public List<BUser> friends() {
        return currentUserModel().connectionsWithType(BUserConnection.Type.Friend);
    }

    @NotNull
    @Override
    public List<BUser> blockedUsers() {
        return currentUserModel().connectionsWithType(BUserConnection.Type.Blocked);
    }

    @NotNull
    @Override
    public List<BUser> followers() {
        return currentUserModel().connectionsWithType(BUserConnection.Type.Follower);
    }

    @NotNull
    @Override
    public  Promise<Void, BError, Void> addFriends(BUser user) {
        return currentUser().addFriend(user);
    }

    @NotNull
    @Override
    public  Promise<Void, BError, Void> removeFriend(BUser user) {
        return currentUser().removeFriend(user);
    }

    @NotNull
    @Override
    public Promise<Void, BError, Void> blockUser(BUser user) {
        return currentUser().blockUser(user);
    }

    @NotNull
    @Override
    public Promise<Void, BError, Void> unblockUser(BUser user) {
        return currentUser().unblockUser(user);
    }


}
