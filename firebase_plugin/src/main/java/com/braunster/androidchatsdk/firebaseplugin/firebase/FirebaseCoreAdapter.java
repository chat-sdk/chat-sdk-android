/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.content.Context;

import com.braunster.androidchatsdk.firebaseplugin.R;
import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.ChatSDKReceiver;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.core.BUser;
import co.chatsdk.core.dao.core.DaoDefines;
import co.chatsdk.core.dao.core.FollowerLink;
import co.chatsdk.core.types.Defines;
import co.chatsdk.firebase.wrappers.UserWrapper;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.core.DaoCore;
import co.chatsdk.core.defines.FirebaseDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.ChatError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.abstracthandlers.CoreManager;

import static com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors.getFirebaseError;

public class FirebaseCoreAdapter extends CoreManager {

    private static final String TAG = FirebaseCoreAdapter.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    public FirebaseCoreAdapter(Context context){
        super(context);

        // Adding the manager that will handle all the incoming events.
//        FirebaseEventsManager eventManager = FirebaseEventsManager.getInstance();
//        setEventManager(eventManager);

    }








    @Override
    public Completable followUser(final BUser userToFollow) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

//                if (!DaoDefines.EnableFollowers)
//                    throw new IllegalStateException("You need to enable followers in defines before you can use this method.");
//
//                final BUser user = NetworkManager.shared().a.core.currentUserModel();
//
//                // Add the current user to the userToFollow "followers" path
//                final DatabaseReference userToFollowingRef = FirebasePaths.userFollowingRef(userToFollow.getEntityID())
//                        .child(user.getEntityID());
//
//                if (DEBUG) Timber.d("followUser, userToFollowRef: ", userToFollowingRef.toString());
//
//                userToFollowingRef.setValue("null", new DatabaseReference.CompletionListener() {
//                    @Override
//                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
//                        if (firebaseError!=null)
//                        {
//                            e.onError(getFirebaseError(firebaseError));
//                        }
//                        else
//                        {
//                            FollowerLink follows = user.fetchOrCreateFollower(userToFollow, FollowerLink.Type.FOLLOWS);
//
//                            user.addContact(userToFollow);
//
//                            // Add the user to follow to the current user follow
//                            DatabaseReference curUserFollowsRef = FirebasePaths.firebaseRef().child(FirebasePaths.FollowingPath);
//                            if (DEBUG) Timber.d("followUser, curUserFollowsRef: %s", curUserFollowsRef.toString());
//                            curUserFollowsRef.setValue("null", new DatabaseReference.CompletionListener() {
//                                @Override
//                                public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
//
//                                    // Send a push to the user that is now followed.
//                                    if (DEBUG) Timber.v("pushutils sendfollowpush");
//                                    JSONObject data = new JSONObject();
//                                    try {
//                                        data.put(DaoDefines.Keys.ACTION, ChatSDKReceiver.ACTION_FOLLOWER_ADDED);
//                                        data.put(DaoDefines.Keys.CONTENT, user.getMetaName() + " " + BNetworkManager.getAppContext().getString(R.string.not_follower_content));
//                                        // For iOS
//                                        data.put(DaoDefines.Keys.BADGE, DaoDefines.Keys.INCREMENT);
//                                        data.put(DaoDefines.Keys.ALERT, user.getMetaName() + " " + BNetworkManager.getAppContext().getString(R.string.not_follower_content));
//                                        // For making sound in iOS
//                                        data.put(DaoDefines.Keys.SOUND, DaoDefines.Keys.Default);
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    List<String> channels = new ArrayList<String>();
//                                    channels.add(userToFollow.getPushChannel());
//                                    NetworkManager.shared().a.push.pushToChannels(channels, data);
//
//                                    e.onComplete();
//                                }
//                            });
//                        }
//                    }
//                });
            }
        });
    }

    @Override
    public void unFollowUser(BUser userToUnfollow) {
//        if (!DaoDefines.EnableFollowers)
//            throw new IllegalStateException("You need to enable followers in defines before you can use this method.");
//
//
//        final BUser user = NetworkManager.shared().a.core.currentUserModel();
//
//        // Remove the current user to the userToFollow "followers" path
//        DatabaseReference userToFollowRef = FirebasePaths.userRef(userToUnfollow.getEntityID())
//                .child(FirebasePaths.FollowersPath)
//                .child(user.getEntityID());
//
//        userToFollowRef.removeValue();
//
//        FollowerLink follows = user.fetchOrCreateFollower(userToUnfollow, FollowerLink.Type.FOLLOWS);
//
//        // Add the user to follow to the current user follow
//        DatabaseReference curUserFollowsRef = FirebasePaths.firebaseRef().child(follows.getBPath().getPath());
//
//        curUserFollowsRef.removeValue();
//
//        DaoCore.deleteEntity(follows);
    }

    @Override
    public Observable<BUser> getFollowers(final String entityId){
        return Observable.create(new ObservableOnSubscribe<BUser>() {
            @Override
            public void subscribe(final ObservableEmitter<BUser> e) throws Exception {
//                if (StringUtils.isEmpty(entityId)) {
//                    e.onError(ChatError.getError(ChatError.Code.NULL, "CoreEntity id is empty"));
//                    return;
//                }
//
//                final BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);
//
//                DatabaseReference followersRef = FirebasePaths.userFollowersRef(entityId);
//
//                followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot snapshot) {
//
//                        final List<BUser> followers = new ArrayList<BUser>();
//
//                        for (DataSnapshot snap : snapshot.getChildren())
//                        {
//                            String followingUserID = snap.getKey();
//
//                            if (StringUtils.isNotEmpty(followingUserID))
//                            {
//                                BUser follwer = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followingUserID);
//
//                                FollowerLink f = user.fetchOrCreateFollower(follwer, FollowerLink.Type.FOLLOWER);
//
//                                followers.add(follwer);
//                            } else if (DEBUG) Timber.e("Follower id is empty");
//                        }
//
//                        ArrayList<Completable> completables = new ArrayList<>();
//
//                        for (final BUser u : followers)
//                        {
//                            completables.add(UserWrapper.initWithModel(u).once().doOnComplete(new Action() {
//                                @Override
//                                public void run() throws Exception {
//                                    e.onNext(u);
//                                }
//                            }));
//                        }
//                        Completable.merge(completables).doOnComplete(new Action() {
//                            @Override
//                            public void run() throws Exception {
//                                e.onComplete();
//                            }
//                        }).subscribe();
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError firebaseError) {
//                        e.onError(firebaseError.toException());
//                    }
//                });
            }
        });
    }

    @Override
    public Observable<BUser>  getFollows(final String entityId){
        return Observable.create(new ObservableOnSubscribe<BUser>() {
            @Override
            public void subscribe(final ObservableEmitter<BUser> e) throws Exception {
//                if (DEBUG) Timber.v("getFollowers, Id: %s", entityId);
//
//                final Deferred<List<BUser>, ChatError, Void> deferred = new DeferredObject<>();
//
//                if (StringUtils.isEmpty(entityId))
//                {
//                    e.onError(ChatError.getError(ChatError.Code.NULL, "CoreEntity id is empty"));
//                    return;
//                }
//
//                final BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);
//
//                DatabaseReference followersRef = FirebasePaths.userFollowingRef(entityId);
//
//                followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot snapshot) {
//                        final List<BUser> followers = new ArrayList<BUser>();
//
//                        for (DataSnapshot snap : snapshot.getChildren())
//                        {
//                            String followingUserID = snap.getKey();
//
//                            if (StringUtils.isNotEmpty(followingUserID))
//                            {
//                                BUser follower = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followingUserID);
//
//                                FollowerLink f = user.fetchOrCreateFollower(follower, FollowerLink.Type.FOLLOWS);
//
//                                followers.add(follower);
//                            }
//                        }
//
//                        ArrayList<Completable> completables = new ArrayList<>();
//
//                        for (final BUser u : followers)
//                        {
//                            completables.add(UserWrapper.initWithModel(u).once().doOnComplete(new Action() {
//                                @Override
//                                public void run() throws Exception {
//                                    e.onNext(u);
//                                }
//                            }));
//                        }
//                        Completable.merge(completables).doOnComplete(new Action() {
//                            @Override
//                            public void run() throws Exception {
//                                e.onComplete();
//                            }
//                        }).subscribe();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError firebaseError) {
//                        e.onError(firebaseError.toException());
//                    }
//                });
            }
        });
    }



}
