/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseNetworkAdapter;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebasePaths;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.Map;

public class EntityWrapper<E>{
    
    protected E model;
    protected String entityId;
    protected String path;

    public E getModel() {
        return model;
    }

    public void setModel(E model) {
        this.model = model;
    }

    public String getEntityId() {
        return entityId;
    }
    
    protected AbstractNetworkAdapter getNetworkAdapter(){
        return BNetworkManager.sharedManager().getNetworkAdapter();
    }
    
    protected BError getFirebaseError(FirebaseError firebaseError){
        return BFirebaseNetworkAdapter.getFirebaseError(firebaseError);
        
    }

    protected Firebase ref(){
        return FirebasePaths.entityRef(path, entityId);
    }

    protected Firebase keyRef(String key){
        return FirebasePaths.entityKeyRef(path, entityId, key);
    }

    protected Firebase stateRef(String key){
        return FirebasePaths.entityStateRef(path, entityId, key);
    }

/*    Map<String, Object> state(){
        return null;
    }*/

    void setState(Map<String, Object> state){

    }

/*    Double timestampForKey(String key){
        return null;
    }

    void setTimestamp(String key, Double timestamp){

    }*/

    public Promise<Void, BError, Void> updateStateWithKey(String key){
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

        Firebase ref = stateRef(key);

        ref.setValue(ServerValue.TIMESTAMP, new Firebase.CompletionListener() {
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
