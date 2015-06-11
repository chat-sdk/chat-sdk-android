/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseNetworkAdapter;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebasePaths;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.BUserAccount;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BMessageEntity;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.TwitterManager;
import com.braunster.chatsdk.object.BError;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class BUserWrapper extends EntityWrapper<BUser> {

    private static final boolean DEBUG = Debug.BUser;
    
    private static final String USER_PREFIX = "user";

    public static BUserWrapper initWithAuthData(AuthData authData){
        return new BUserWrapper(authData);
    }

    public static BUserWrapper initWithModel(BUser user){
        return new BUserWrapper(user);
    }

    @SuppressWarnings("all")
    public static BUserWrapper initWithSnapshot(DataSnapshot snapshot){
        BUser model = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, snapshot.getKey());
        BUserWrapper userWrapper = new BUserWrapper(model);

        if (snapshot.getValue() instanceof Map)
            userWrapper.deserialize((Map<String, Object>) snapshot.getValue());

        return userWrapper;
    }

    public static BUserWrapper initWithEntityId(String entityId){
        BUser model = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);
        return initWithModel(model);
    }
    
    private BUserWrapper(AuthData authData){
        model = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, authData.getUid());

        entityId = model.getEntityID();

        initPath();

        updateUserFromAuthData(authData);
    }

    private BUserWrapper(BUser model) {
        this.model = model;
        entityId = model.getEntityID();

        initPath();
    }
    
    private BUserWrapper(DataSnapshot snapshot){
        model = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, snapshot.getKey());
        entityId = model.getEntityID();

        initPath();

        deserialize((Map<String, Object>) snapshot.getValue());
    }

    private void initPath(){
        path = BFirebaseDefines.Path.BUsers;
    }

    /**
     * Note - Change was removing of online values as set online and online time.
     * * * * */
    public void updateUserFromAuthData(AuthData authData){
        Timber.v("updateUserFromAuthData");

        model.setEntityID(authData.getUid());
       
        Map<String, Object> thirdPartyData = authData.getProviderData();
        String name = (String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.DisplayName);;
        String email = (String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.EMail);;
        BUserAccount linkedAccount;
        
        switch (FirebasePaths.providerToInt(authData.getProvider()))
        {
            case BDefines.ProviderInt.Facebook:
                // Setting the name.
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getName()))
                {
                    model.setName(name);
                }

                // Setting the email.//
                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getEmail()))
                {
                    model.setEmail(email);
                }

                linkedAccount = model.getAccountWithType(BUserAccount.Type.FACEBOOK);
                if (linkedAccount == null)
                {
                    linkedAccount = new BUserAccount();
                    linkedAccount.setType(BUserAccount.Type.FACEBOOK);
                    linkedAccount.setUser(model.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.AccessToken));

                break;

            case BDefines.ProviderInt.Twitter:
                // Setting the name
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getName()))
                    model.setName(name);

                // Setting the email.//
                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getEmail()))
                {
                    model.setEmail(email);
                }

                TwitterManager.userId = Long.parseLong((String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.ID));

                linkedAccount = model.getAccountWithType(BUserAccount.Type.TWITTER);
                if (linkedAccount == null)
                {
                    linkedAccount = new BUserAccount();
                    linkedAccount.setType(BUserAccount.Type.TWITTER);
                    linkedAccount.setUser(model.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.AccessToken));

                break;

            case BDefines.ProviderInt.Password:
                // Setting the name
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getName()))
                    model.setName(name);

                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getEmail()))
                {
                    model.setEmail(email);
                }
                break;

            default: break;
        }

        // Message Color.
        if (StringUtils.isEmpty(model.getMessageColor()))
        {
            if (StringUtils.isNotEmpty(BDefines.Defaults.MessageColor))
            {
                model.setMessageColor(BDefines.Defaults.MessageColor);
            }
            else model.setMessageColor( BMessageEntity.colorToString(BMessageEntity.randomColor()) );
        }

        if (StringUtils.isEmpty(model.getName()))
        {
            model.setName(BDefines.getDefaultUserName());
        }
        
        // Save the data
        model = DaoCore.updateEntity(model);
    }

    public Promise<BUser, BError, Void> once(){

        final Deferred<DataSnapshot, BError, Void> deferred = new DeferredObject<>();

        final Deferred<BUser, BError, Void> promise = new DeferredObject<>();

        Firebase ref = ref();

        if (DEBUG) Timber.v("once, EntityID: %s, Ref Path: %s", entityId, ref.getPath());
        
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (DEBUG) Timber.v("once, onDataChange");
                deferred.resolve(snapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                if (DEBUG) Timber.v("once, onCancelled");
                deferred.reject(getFirebaseError(firebaseError));
            }
        });

        deferred.done(new DoneCallback<DataSnapshot>() {
            @Override
            public void onDone(DataSnapshot snapshot) {
                deserialize((Map<String, Object>) snapshot.getValue());
                
                promise.resolve(model);
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {
                promise.reject(bError);
            }
        });
        
        return promise;
    }

    public Promise<BUser, BError, Void> metaOnce(){
        if (DEBUG) Timber.v("push");

        final Deferred<BUser, BError, Void> deferred = new DeferredObject<>();


        final Deferred<DataSnapshot, BError, Void> snapshotDef = new DeferredObject<>();

        Firebase ref = FirebasePaths.userMetaRef(model.getEntityID());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                snapshotDef.resolve(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                snapshotDef.reject(getFirebaseError(firebaseError));
            }
        });

        snapshotDef.then(
                new DoneCallback<DataSnapshot>() {
                    @Override
                    public void onDone(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null)
                            deserialize((Map<String, Object>) dataSnapshot.getValue());

                        deferred.resolve(model);
                    }
                },
                new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        deferred.reject(error);
                    }
                });

        return deferred.promise();
    }


    public void metaOff(){
        getNetworkAdapter().getEventManager().userMetaOff(entityId);
    }

    public Promise<Void, Void, Void> metaOn(){
        final Deferred<Void, Void, Void> deferred = new DeferredObject<>();

        getNetworkAdapter().getEventManager().userMetaOn(entityId, deferred);
        
        return deferred;
    }
    
    void deserialize(Map<String, Object> value){
        if (DEBUG) Timber.v("deserialize, Value: %s", value);
        
        if (value != null)
        {
            String uid = (String) value.get(BDefines.Keys.BUID);
            if (StringUtils.isNoneBlank(uid))
                model.setEntityID(uid);

            if (value.containsKey(BDefines.Keys.BColor) && StringUtils.isNotBlank((CharSequence) value.get(BDefines.Keys.BColor))) {
                model.setMessageColor((String) value.get(BDefines.Keys.BColor));
            }

            // Updating the metadata
            Map<String, Object> oldData = model.metaMap();
            Map<String, Object> newData = value;

            if (DEBUG) Timber.v("deserialize, EntityId: %s, OldDataMap: %s, NewMetaMap: %s",entityId, oldData, newData);

            // Updating the old data
            for (String key : newData.keySet())
            {
                if (DEBUG) Timber.d("key: %s, Value: %s", key, newData.get(key));

                if (oldData.get(key) == null || !oldData.get(key).equals(newData.get(key)))
                {
                    if (DEBUG) Timber.d("Updating meta, Key: %s, Value: %s", key, newData.get(key));
                    oldData.put(key, newData.get(key));
                }
            }

            model.setMetaMap(oldData);


            model = DaoCore.updateEntity(model);
        }
    }

    Map<String, Object> serialize(){
        Map<String, Object> values = new HashMap<String, Object>();


        Map<String , Object> meta = model.metaMap();
        meta.put(BDefines.Keys.BColor, StringUtils.isEmpty(model.getMessageColor()) ? "" : model.getMessageColor());
        meta.put(BDefines.Keys.BUID, entityId);

        values.put(BDefines.Keys.BMeta, meta);

        return values;
    }

    public Promise<BUser, BError, Void> pushMeta(){
        if (DEBUG) Timber.v("push");

        final Deferred<BUser, BError, Void> deferred = new DeferredObject<>();

        ref().updateChildren(serialize(), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    {
                        deferred.resolve(model);
                        updateStateWithKey(BFirebaseDefines.Path.BMeta);
                    }
                } else deferred.reject(getFirebaseError(firebaseError));
            }
        });

        return deferred.promise();
    }

    public Firebase ref(){
        return FirebasePaths.userRef(entityId);
    }



    private Firebase imageRef(){
        return ref().child(BFirebaseDefines.Path.BImage);
    }

    private Firebase thumbnailRef(){
        return ref().child(BFirebaseDefines.Path.BThumbnail);
    }

    private Firebase metaRef(){
        return ref().child(BFirebaseDefines.Path.BMeta);
    }
    
    public String pushChannel(){
        return model.pushChannel();
    }
    
    public Promise<BUserWrapper, FirebaseError, Void> addThreadWithEntityId(String entityId){

        final Deferred<BUserWrapper, FirebaseError, Void> deferred = new DeferredObject<>();

        // Getting the user ref.
        Firebase userThreadRef = ref();

        userThreadRef = userThreadRef.child(BFirebaseDefines.Path.BThread).child(entityId);

        userThreadRef.child(BDefines.Keys.BNull).setValue("", new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null)
                {
                    deferred.resolve(BUserWrapper.this);
                }
                else
                {
                    deferred.reject(firebaseError);
                }
            }
        });
        
        return deferred.promise();
    }
    
    public Promise<BUserWrapper, FirebaseError, Void> removeThreadWithEntityId(String entityId){

        final Deferred<BUserWrapper, FirebaseError, Void> deferred = new DeferredObject<>();

        Firebase userThreadRef = FirebasePaths.userRef(entityId).child(BFirebaseDefines.Path.BThread).child(entityId);

        userThreadRef.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    deferred.resolve(BUserWrapper.this);
                } else {
                    deferred.reject(firebaseError);
                }
            }
        });
        
        return deferred.promise();

    }
    
    public Promise<Void, BError, Void> updateIndex(){

        final Deferred<Void, BError, Void> deferred = new DeferredObject();

        Map<String, String> values = new HashMap<String, String>();
        
        String name = model.getName();
        String email = model.getEmail();
        String phoneNumber = model.metaStringForKey(BDefines.Keys.BPhone);
        
        values.put(BDefines.Keys.BName, StringUtils.isNotEmpty(name) ? AbstractNetworkAdapter.processForQuery(name) : "");
        values.put(BDefines.Keys.BEmail, StringUtils.isNotEmpty(email) ? AbstractNetworkAdapter.processForQuery(email) : "");

        if (BDefines.IndexUserPhoneNumber && StringUtils.isNotBlank(phoneNumber))
            values.put(BDefines.Keys.BPhone, AbstractNetworkAdapter.processForQuery(phoneNumber));


        Firebase ref = FirebasePaths.searchIndexRef().child(entityId);

        ref.setValue(values, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    deferred.resolve(null);
                } else {
                    deferred.reject(getFirebaseError(firebaseError));
                }
            }
        });
        
        
        return deferred.promise();
    }



    /**
     * Set the user online value to false.
     **/
    public void goOffline(){
        Firebase userOnlineRef = FirebasePaths.userOnlineRef(entityId);
        userOnlineRef.removeValue();
    }
    
    /**
     * Set the user online value to true.
     * 
     * When firebase disconnect this will be auto change to false.
     **/
    public void goOnline(){
        Firebase userOnlineRef = FirebasePaths.userOnlineRef(entityId);

        // Set the current state of the user as online.
        // And add a listener so when the user log off from firebase he will be set as disconnected.

        Map<String, Object> data = new HashMap<>();
        data.put(BDefines.Keys.BUID, model.getEntityID());
        data.put(BDefines.Keys.BTime, ServerValue.TIMESTAMP);

        userOnlineRef.setValue(data);
        userOnlineRef.onDisconnect().removeValue();
    }



    public Promise<Void, BError, Void> addFriend(final BUser user){

        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        final Deferred<Void, BError, Void> addFollowerDeferred = new DeferredObject<>();


        final Firebase firebase = FirebasePaths.userFriendsRef(model.getEntityID(), user.getEntityID());

        Map<String, Object> data = new HashMap<>();

        data.put(BDefines.Keys.BUID, user.getEntityID());
        firebase.setValue(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null)
                    deferred.resolve(null);
                else
                    deferred.reject(BFirebaseNetworkAdapter.getFirebaseError(firebaseError));
            }
        });

        deferred.then(
                new DoneCallback<Void>() {
                    @Override
                    public void onDone(Void aVoid) {

                        Firebase followerRef = FirebasePaths.userFollowersRef(user.getEntityID(), model.getEntityID());
                        Map<String, Object> data = new HashMap<>();

                        data.put(BDefines.Keys.BUID, model.getEntityID());

                        followerRef.setValue(data, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError == null)
                                    addFollowerDeferred.resolve(null);
                                else
                                    addFollowerDeferred.reject(getFirebaseError(firebaseError));
                            }
                        });
                    }
                },
                new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        addFollowerDeferred.reject(error);
                    }
                });

        return addFollowerDeferred.promise();
    }

    public Promise<Void, BError, Void> removeFriend(final BUser user){
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        final Deferred<Void, BError, Void> addFollowerDeferred = new DeferredObject<>();

        final Firebase firebase = FirebasePaths.userFriendsRef(model.getEntityID(), user.getEntityID());

        firebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null)
                    deferred.resolve(null);
                else
                    deferred.reject(getFirebaseError(firebaseError));
            }
        });

        deferred.then(
                new DoneCallback<Void>() {
                    @Override
                    public void onDone(Void aVoid) {

                        Firebase followerRef = FirebasePaths.userFollowersRef(user.getEntityID(), model.getEntityID());

                        followerRef.removeValue(new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError == null)
                                    addFollowerDeferred.resolve(null);
                                else
                                    addFollowerDeferred.reject(getFirebaseError(firebaseError));
                            }
                        });
                    }
                },
                new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        addFollowerDeferred.reject(error);
                    }
                });

        return addFollowerDeferred.promise();
    }

    public Promise<Void, BError, Void> blockUser(final BUser user){

        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        Firebase firebase = FirebasePaths.userBlockedRef(model.getEntityID(), user.getEntityID());

        Map<String, Object> data = new HashMap<>();
        data.put(BDefines.Keys.BUID, user.getEntityID());

        firebase.setValue(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null)
                    deferred.resolve(null);
                else
                    deferred.reject(getFirebaseError(firebaseError));
            }
        });

        return deferred.promise();
    }

    public Promise<Void, BError, Void> unblockUser(final BUser user){
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        Firebase firebase = FirebasePaths.userBlockedRef(model.getEntityID(), user.getEntityID());

        firebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null)
                    deferred.resolve(null);
                else
                    deferred.reject(getFirebaseError(firebaseError));
            }
        });

        return deferred.promise();
    }

}
