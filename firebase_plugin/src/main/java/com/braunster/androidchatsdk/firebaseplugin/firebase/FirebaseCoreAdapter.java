/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.content.Context;

import com.braunster.androidchatsdk.firebaseplugin.R;
import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.BBackendlessHandler;
import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.ChatSDKReceiver;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.FollowerLink;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterDeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.abstracthandlers.CoreManager;

import static com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors.getFirebaseError;
import static com.braunster.chatsdk.dao.core.ProcessForQueryHandler.processForQuery;
import static com.braunster.chatsdk.network.BDefines.Keys;

public class FirebaseCoreAdapter extends CoreManager {

    private static final String TAG = FirebaseCoreAdapter.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    public FirebaseCoreAdapter(Context context){
        super(context);

        // Adding the manager that will handle all the incoming events.
        FirebaseEventsManager eventManager = FirebaseEventsManager.getInstance();
        setEventManager(eventManager);

        // Setting the upload handler
        setUploadHandler(new BFirebaseUploadHandler());

        // Setting the push handler
        BBackendlessHandler backendlessPushHandler = new BBackendlessHandler();
        backendlessPushHandler.setContext(context);
        setPushHandler(backendlessPushHandler);

        // Parse init
        /*Parse.initialize(context, context.getString(R.string.parse_app_id), context.getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();*/

        backendlessPushHandler.initWithAppKey(context.getString(R.string.backendless_app_id),
                            context.getString(R.string.backendless_secret_key), context.getString(R.string.backendless_app_version));
    }


    @Override
    public String getServerURL() {
        return BDefines.ServerUrl;
    }


    @Override
    /** Unlike the iOS code the current user need to be saved before you call this method.*/
    public Promise<BUser, BError, Void> pushUser() {
        return currentUser().push();
    }

    public BUserWrapper currentUser(){
        return BUserWrapper.initWithModel(currentUserModel());
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

        Query query = FirebasePaths.indexRef().orderByChild(index).startAt(
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
            public void onCancelled(DatabaseError firebaseError) {
                deferred.reject(FirebaseErrors.getFirebaseError(firebaseError));
            }
        });

        return deferred.promise();
    }

    @Override
    public void setUserOnline() {
        BUser current = BNetworkManager.getCoreInterface().currentUserModel();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            BUserWrapper.initWithModel(currentUserModel()).goOnline();
        }
    }

    @Override
    public void setUserOffline() {
        BUser current = BNetworkManager.getCoreInterface().currentUserModel();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            BUserWrapper.initWithModel(currentUserModel()).goOffline();
            updateLastOnline();
        }

    }

    @Override
    public void goOffline() {
        DatabaseReference.goOffline();

        setUserOffline();
    }

    @Override
    public void goOnline() {
        DatabaseReference.goOnline();

        setUserOnline();
    }

    public Promise<Boolean, BError, Void> isOnline(){

        final Deferred<Boolean, BError, Void> deferred = new DeferredObject<>();

        if (BNetworkManager.getCoreInterface().currentUserModel() == null)
        {
            return  deferred.reject(BError.getError(BError.Code.NULL, "Current user is null"));
        }

        FirebasePaths.userOnlineRef(BNetworkManager.getCoreInterface().currentUserModel().getEntityID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                updateLastOnline();

                deferred.resolve((Boolean) snapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                deferred.reject(getFirebaseError(firebaseError));
            }
        });

        return deferred.promise();
    }

    @Override
    public Promise<Void, BError, Void> followUser(final BUser userToFollow) {

        if (!BDefines.EnableFollowers)
            throw new IllegalStateException("You need to enable followers in defines before you can use this method.");

        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        final BUser user = BNetworkManager.getCoreInterface().currentUserModel();

        // Add the current user to the userToFollow "followers" path
        DatabaseReference userToFollowRef = FirebasePaths.userRef(userToFollow.getEntityID())
                .child(BFirebaseDefines.Path.FollowerLinks)
                .child(user.getEntityID());
        if (DEBUG) Timber.d("followUser, userToFollowRef: ", userToFollowRef.toString());

        userToFollowRef.setValue("null", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError!=null)
                {
                    deferred.reject(getFirebaseError(firebaseError));
                }
                else
                {
                    FollowerLink follows = user.fetchOrCreateFollower(userToFollow, FollowerLink.Type.FOLLOWS);

                    user.addContact(userToFollow);

                    // Add the user to follow to the current user follow
                    DatabaseReference curUserFollowsRef = FirebasePaths.firebaseRef().child(follows.getBPath().getPath());
                    if (DEBUG) Timber.d("followUser, curUserFollowsRef: %s", curUserFollowsRef.toString());
                    curUserFollowsRef.setValue("null", new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {

                            // Send a push to the user that is now followed.
                            if (DEBUG) Timber.v("pushutils sendfollowpush");
                            JSONObject data = new JSONObject();
                            try {
                                data.put(BDefines.Keys.ACTION, ChatSDKReceiver.ACTION_FOLLOWER_ADDED);
                                data.put(BDefines.Keys.CONTENT, user.getMetaName() + " " + BNetworkManager.getAppContext().getString(R.string.not_follower_content));
                                // For iOS
                                data.put(BDefines.Keys.BADGE, BDefines.Keys.INCREMENT);
                                data.put(BDefines.Keys.ALERT, user.getMetaName() + " " + BNetworkManager.getAppContext().getString(R.string.not_follower_content));
                                // For making sound in iOS
                                data.put(BDefines.Keys.SOUND, BDefines.Keys.Default);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            List<String> channels = new ArrayList<String>();
                            channels.add(userToFollow.getPushChannel());
                            BNetworkManager.getCoreInterface().getPushHandler().pushToChannels(channels, data);

                            deferred.resolve(null);
                        }
                    });
                }
            }
        });

        return deferred.promise();
    }

    @Override
    public void unFollowUser(BUser userToUnfollow) {
        if (!BDefines.EnableFollowers)
            throw new IllegalStateException("You need to enable followers in defines before you can use this method.");


        final BUser user = BNetworkManager.getCoreInterface().currentUserModel();

        // Remove the current user to the userToFollow "followers" path
        DatabaseReference userToFollowRef = FirebasePaths.userRef(userToUnfollow.getEntityID())
                .child(BFirebaseDefines.Path.FollowerLinks)
                .child(user.getEntityID());

        userToFollowRef.removeValue();

        FollowerLink follows = user.fetchOrCreateFollower(userToUnfollow, FollowerLink.Type.FOLLOWS);

        // Add the user to follow to the current user follow
        DatabaseReference curUserFollowsRef = FirebasePaths.firebaseRef().child(follows.getBPath().getPath());

        curUserFollowsRef.removeValue();

        DaoCore.deleteEntity(follows);
    }

    @Override
    public Promise<List<BUser>, BError, Void> getFollowers(String entityId){
        if (DEBUG) Timber.v("getFollowers, Id: %s", entityId);

        final Deferred<List<BUser>, BError, Void> deferred = new DeferredObject<>();

        if (StringUtils.isEmpty(entityId))
        {
            return deferred.reject(BError.getError(BError.Code.NULL, "Entity id is empty"));
        }

        final BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);

        DatabaseReference followersRef = FirebasePaths.userFollowersRef(entityId);

        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                final List<BUser> followers = new ArrayList<BUser>();

                for (DataSnapshot snap : snapshot.getChildren())
                {
                    String followingUserID = snap.getKey();

                    if (StringUtils.isNotEmpty(followingUserID))
                    {
                        BUser follwer = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followingUserID);

                        FollowerLink f = user.fetchOrCreateFollower(follwer, FollowerLink.Type.FOLLOWER);

                        followers.add(follwer);
                    } else if (DEBUG) Timber.e("Follower id is empty");
                }

                Promise[] promises= new Promise[followers.size()];

                int count = 0;
                for (BUser u : followers)
                {
                    promises[count] = BUserWrapper.initWithModel(u).once();

                    count++;
                }

                MasterDeferredObject masterDeferredObject = new MasterDeferredObject(promises);

                masterDeferredObject.progress(new ProgressCallback<MasterProgress>() {
                    @Override
                    public void onProgress(MasterProgress masterProgress) {

                        if (DEBUG) Timber.d("MasterDeferredProgress, done: %s, failed: %s, total: %s", masterProgress.getDone(), masterProgress.getFail(), masterProgress.getTotal());

                        // Reject the promise if all promisses failed.
                        if (masterProgress.getFail() == masterProgress.getTotal())
                        {
                            deferred.reject(BError.getError(BError.Code.OPERATION_FAILED, "All promises failed"));
                        }
                        else
                            deferred.resolve(followers);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                deferred.reject(getFirebaseError(firebaseError));
            }
        });


        return deferred.promise();
    }

    @Override
    public Promise<List<BUser>, BError, Void>  getFollows(String entityId){
        if (DEBUG) Timber.v("getFollowers, Id: %s", entityId);

        final Deferred<List<BUser>, BError, Void> deferred = new DeferredObject<>();

        if (StringUtils.isEmpty(entityId))
        {
            return deferred.reject(BError.getError(BError.Code.NULL, "Entity id is empty"));
        }

        final BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);

        DatabaseReference followersRef = FirebasePaths.userFollowsRef(entityId);

        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final List<BUser> followers = new ArrayList<BUser>();

                for (DataSnapshot snap : snapshot.getChildren())
                {
                    String followingUserID = snap.getKey();

                    if (StringUtils.isNotEmpty(followingUserID))
                    {
                        BUser follwer = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followingUserID);

                        FollowerLink f = user.fetchOrCreateFollower(follwer, FollowerLink.Type.FOLLOWS);

                        followers.add(follwer);
                    }
                }

                Promise[] promises= new Promise[followers.size()];

                int count = 0;
                for (BUser u : followers)
                {
                    promises[count] = BUserWrapper.initWithModel(u).once();

                    count++;
                }

                MasterDeferredObject masterDeferredObject = new MasterDeferredObject(promises);

                masterDeferredObject.progress(new ProgressCallback<MasterProgress>() {
                    @Override
                    public void onProgress(MasterProgress masterProgress) {

                        if (DEBUG) Timber.d("MasterDeferredProgress, done: %s, failed: %s, total: %s", masterProgress.getDone(), masterProgress.getFail(), masterProgress.getTotal());

                        // Reject the promise if all promisses failed.
                        if (masterProgress.getFail() == masterProgress.getTotal())
                        {
                            deferred.reject(BError.getError(BError.Code.OPERATION_FAILED, "All promises failed"));
                        }
                        else
                            deferred.resolve(followers);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                deferred.reject(getFirebaseError(firebaseError));
            }
        });

        return deferred.promise();
    }


    @Override
    public void setLastOnline(Date lastOnline) {
        BUser currentUser  = BNetworkManager.getCoreInterface().currentUserModel();
        currentUser.setLastOnline(lastOnline);
        DaoCore.updateEntity(currentUser);

        BNetworkManager.getCoreInterface().pushUser();
    }

    public void updateLastOnline(){
        // FIXME to implement?

    }

}
