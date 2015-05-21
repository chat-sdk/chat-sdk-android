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
import com.braunster.chatsdk.dao.BLinkedAccount;
import com.braunster.chatsdk.dao.BUser;
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
    
    public static BUserWrapper initWithSnapshot(DataSnapshot snapshot){
        return new BUserWrapper(snapshot);
    }

    public static BUserWrapper initWithEntityId(String entityId){
        BUser model = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityId);
        return initWithModel(model);
    }
    
    private BUserWrapper(AuthData authData){
        model = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, authData.getUid());

        entityId = model.getEntityID();
        
        updateUserFromAuthData(authData);
    }

    private BUserWrapper(BUser model) {
        this.model = model;
        entityId = model.getEntityID();
    }
    
    private BUserWrapper(DataSnapshot snapshot){
        model = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, snapshot.getKey());
        entityId = model.getEntityID();
        
        deserialize((Map<String, Object>) snapshot.getValue());
    }
    
    /**
     * Note - Change was removing of online values as set online and online time.
     * * * * */
    private void updateUserFromAuthData(AuthData authData){
        Timber.v("updateUserFromAuthData");

        model.setAuthenticationType(FirebasePaths.providerToInt(authData.getProvider()));

        model.setEntityID(authData.getUid());
       
        Map<String, Object> thirdPartyData = authData.getProviderData();
        String name = (String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.DisplayName);;
        String email = (String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.EMail);;
        BLinkedAccount linkedAccount;
        
        switch (FirebasePaths.providerToInt(authData.getProvider()))
        {
            case BDefines.ProviderInt.Facebook:
                // Setting the name.
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getMetaName()))
                {
                    model.setMetaName(name);
                }

                // Setting the email.//
                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getMetaEmail()))
                {
                    model.setMetaEmail(email);
                }

                linkedAccount = model.getAccountWithType(BLinkedAccount.Type.FACEBOOK);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.FACEBOOK);
                    linkedAccount.setUser(model.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.AccessToken));

                break;

            case BDefines.ProviderInt.Twitter:
                // Setting the name
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getMetaName()))
                    model.setMetaName(name);

                // Setting the email.//
                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getMetaEmail()))
                {
                    model.setMetaEmail(email);
                }

                TwitterManager.userId = Long.parseLong((String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.ID));

                linkedAccount = model.getAccountWithType(BLinkedAccount.Type.TWITTER);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.TWITTER);
                    linkedAccount.setUser(model.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken((String) thirdPartyData.get(BDefines.Keys.ThirdPartyData.AccessToken));

                break;

            case BDefines.ProviderInt.Password:
                // Setting the name
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getMetaName()))
                    model.setMetaName(name);

                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getMetaEmail()))
                {
                    model.setMetaEmail(email);
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

        if (StringUtils.isEmpty(model.getMetaName()))
        {
            model.setMetaName(BDefines.getDefaultUserName());
        }
        
        // Save the data
        DaoCore.updateEntity(model);
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
                deferred.reject(BFirebaseNetworkAdapter.getFirebaseError(firebaseError));
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

    public void metaOff(){
        getNetworkAdapter().getEventManager().userMetaOff(entityId);
    }

    public Promise metaOn(){
        final Deferred<Void, Void, Void> deferred = new DeferredObject<>();

        getNetworkAdapter().getEventManager().userMetaOn(entityId, deferred);
        
        return deferred;
    }
    
    void deserialize(Map<String, Object> value){
        if (DEBUG) Timber.v("deserialize, Value is null? %s", value == null);
        
        if (value != null)
        {
            if (value.containsKey(BDefines.Keys.BOnline) && !value.get(BDefines.Keys.BOnline).equals(""))
                model.setOnline((Boolean) value.get(BDefines.Keys.BOnline));

            if (value.containsKey(BDefines.Keys.BColor) && !value.get(BDefines.Keys.BColor).equals("")) {
                model.setMessageColor((String) value.get(BDefines.Keys.BColor));
            }

            // The entity update is called in the deserializeMeta.
            deserializeMeta((Map<String, Object>) value.get(BFirebaseDefines.Path.BMetaPath));
        }
    }

    void deserializeMeta(Map<String, Object> value){
        if (DEBUG) Timber.v("deserializeMeta, Value: %s", value);
        
        if (value != null)
        {
            Map<String, Object> oldData = model.metaMap();
            Map<String, Object> newData = value;

            if (DEBUG) Timber.v("deserializeMeta, OldDataMap: %s", oldData);
            
            // Updating the old data
            for (String key : newData.keySet())
            {
                if (DEBUG) Timber.d("key: %s, Value: %s", key, newData.get(key));
                
                if (oldData.get(key) == null || !oldData.get(key).equals(newData.get(key)))
                    oldData.put(key, newData.get(key));
            }

            model.setMetaMap(oldData);

            model = DaoCore.updateEntity(model);
        }
    }

    Map<String, Object> serialize(){
        Map<String, Object> values = new HashMap<String, Object>();

        values.put(BDefines.Keys.BColor, StringUtils.isEmpty(model.getMessageColor()) ? "" : model.getMessageColor());
        values.put(BDefines.Keys.BMeta, model.metaMap());
        values.put(BDefines.Keys.BLastOnline, ServerValue.TIMESTAMP);

        return values;
    }
    
    public Promise<BUser, BError, Void> push(){
        if (DEBUG) Timber.v("push");
        
        final Deferred<BUser, BError, Void> deferred = new DeferredObject<>();
        
        ref().updateChildren(serialize(), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null)
                {
                    deferred.resolve(model);
                }
                else deferred.reject(getFirebaseError(firebaseError));
            }
        });
        
        return deferred.promise();
    }
    
    private Firebase ref(){
        return FirebasePaths.userRef(entityId);
    }

    private Firebase imageRef(){
        return ref().child(BFirebaseDefines.Path.BImage);
    }

    private Firebase thumbnailRef(){
        return ref().child(BFirebaseDefines.Path.BThumbnail);
    }

    private Firebase metaRef(){
        return ref().child(BFirebaseDefines.Path.BMetaPath);
    }
    
    public String pushChannel(){
        String channel = USER_PREFIX + (model.getEntityID().replace(":", "_"));
        
        if (channel.contains("%3A"))
            channel = channel.replace("%3A", "_");
        if (channel.contains("%253A"))
            channel = channel.replace("%253A", "_");
        
        return channel;
    }
    
    public Promise<BUserWrapper, FirebaseError, Void> addThreadWithEntityId(String entityId){

        final Deferred<BUserWrapper, FirebaseError, Void> deferred = new DeferredObject<>();

        // Getting the user ref.
        Firebase userThreadRef = ref();

        userThreadRef = userThreadRef.child(BFirebaseDefines.Path.BThreadPath).child(entityId);

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

        Firebase userThreadRef = FirebasePaths.userRef(entityId).child(BFirebaseDefines.Path.BThreadPath).child(entityId);

        userThreadRef.removeValue(new Firebase.CompletionListener() {
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
    
    public Promise<Void, BError, Void> updateIndex(){

        final Deferred<Void, BError, Void> deferred = new DeferredObject();

        Map<String, String> values = new HashMap<String, String>();
        
        String name = model.getMetaName();
        String email = model.getMetaEmail();
        String phoneNumber = model.metaStringForKey(BDefines.Keys.BPhone);
        
        values.put(BDefines.Keys.BName, StringUtils.isNotEmpty(name) ? AbstractNetworkAdapter.processForQuery(name) : "");
        values.put(BDefines.Keys.BEmail, StringUtils.isNotEmpty(email) ? AbstractNetworkAdapter.processForQuery(email) : "");

        if (BDefines.IndexUserPhoneNumber && StringUtils.isNotBlank(phoneNumber))
            values.put(BDefines.Keys.BPhone, AbstractNetworkAdapter.processForQuery(phoneNumber));


        FirebasePaths ref = FirebasePaths.indexRef().appendPathComponent(entityId);

        ref.setValue(values, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError==null)
                {
                    deferred.resolve(null);
                }
                else{
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
        FirebasePaths userOnlineRef = FirebasePaths.userOnlineRef(entityId);
        userOnlineRef.setValue(false);
    }
    
    /**
     * Set the user online value to true.
     * 
     * When firebase disconnect this will be auto change to false.
     **/
    public void goOnline(){
        FirebasePaths userOnlineRef = FirebasePaths.userOnlineRef(entityId);

        // Set the current state of the user as online.
        // And add a listener so when the user log off from firebase he will be set as disconnected.
        userOnlineRef.setValue(true);
        userOnlineRef.onDisconnect().setValue(false);
    }
}
