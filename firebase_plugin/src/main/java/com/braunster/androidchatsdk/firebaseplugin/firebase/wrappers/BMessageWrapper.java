/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebasePaths;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.object.BError;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class BMessageWrapper extends EntityWrapper<BMessage> {

    private static boolean DEBUG = true;
    
    public static BMessageWrapper initWithEntityId(String entityId){
        return new BMessageWrapper((BMessage) DaoCore.fetchOrCreateEntityWithEntityID(BMessage.class, entityId));
    }

    public static BMessageWrapper initWithModel(BMessage model){
        return new BMessageWrapper(model);
    }

    public static BMessageWrapper initWithSnapshot(DataSnapshot snapshot){
        return new BMessageWrapper(snapshot);
    }


    public BMessageWrapper(BMessage model){
        this.model = model;
        this.entityId = model.getEntityID();
    }

    public BMessageWrapper(DataSnapshot snapshot){
        this.model = DaoCore.fetchOrCreateEntityWithEntityID(BMessage.class, snapshot.getKey());
        this.entityId = snapshot.getKey();
        
        deserialize((Map<String, Object>) snapshot.getValue());
    }

    
    
    Map<String, Object> serialize(){
        Map<String, Object> values = new HashMap<String, Object>();

        values.put(BDefines.Keys.BText, model.getText());
        values.put(BDefines.Keys.BTime, ServerValue.TIMESTAMP);
        values.put(BDefines.Keys.BType, model.getTypeSafely());
        values.put(BDefines.Keys.BUID, model.getSender().getEntityID());
        values.put(BDefines.Keys.BRID, model.getThread().getEntityID());

        return values;
    }

    @SuppressWarnings("all") void deserialize(Map<String, Object> value){
        
        if (DEBUG) Timber.v("deserialize, Value: %s", value);
        
        if (value.containsKey(BDefines.Keys.BText) &&
                StringUtils.isNotBlank((CharSequence) value.get(BDefines.Keys.BText)))
        {
            model.setText((String) value.get(BDefines.Keys.BText));
        }

        if (value.containsKey(BDefines.Keys.BType) && !value.get(BDefines.Keys.BType).equals(""))
        {
            if (value.get(BDefines.Keys.BType) instanceof Integer)
                model.setType((Integer) value.get(BDefines.Keys.BType));
            else
                if (value.get(BDefines.Keys.BType) instanceof Long)
                    model.setType( ((Long) value.get(BDefines.Keys.BType)).intValue() );
        }

        if (value.containsKey(BDefines.Keys.BTime) && !value.get(BDefines.Keys.BTime).equals(""))
            model.setDate( new Date( (Long) value.get(BDefines.Keys.BTime) ) );

        if (value.containsKey(BDefines.Keys.BUID)
                && StringUtils.isNotBlank((CharSequence) value.get(BDefines.Keys.BUID)))
        {
            String userEntityId = (String) value.get(BDefines.Keys.BUID);

            BUser user = DaoCore.fetchEntityWithEntityID(BUser.class, userEntityId);

            // If there is no user saved in the db for this entity id,
            // Create a new one and do a once on it to get all the details.
            if (user == null)
            {
                user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userEntityId);
            }

            model.setSender(user);
        }
                
        // Updating the db
        DaoCore.updateEntity(model);
    }

    public Promise<BMessage, BError, BMessage>  push(){
        if (DEBUG) Timber.v("push");

        final Deferred<BMessage, BError, BMessage> deferred = new DeferredObject<>();
        
        // Getting the message ref. Will be created if not exist.
        Firebase ref = ref();
        model.setEntityID(ref.getKey());

        DaoCore.updateEntity(model);

        ref.setValue(serialize(), ServerValue.TIMESTAMP, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                if (DEBUG) Timber.v("push message, onDone");

                if (firebaseError == null) {
                    deferred.resolve(BMessageWrapper.this.model);
                } else {
                    deferred.reject(getFirebaseError(firebaseError));
                }
            }
        });
        
        return deferred.promise();
    }
    
    public Promise<BMessage, BError, BMessage> send(){
        if (DEBUG) Timber.v("send");
        
        if (model.getThread() != null)
        {
            return push();
        }else
        {
            final Deferred<BMessage, BError, BMessage> deferred = new DeferredObject<>();
            deferred.reject(null);
            return deferred.promise();
        }           
    }
    
    /**
     * The message model will be updated after this call.
     **/
    public void setDelivered(int delivered){
        model.setDelivered(delivered);
    }
    
    public Firebase ref(){
        if (StringUtils.isNotEmpty(model.getEntityID()))
        {
            return FirebasePaths.threadMessagesRef(model.getThread().getEntityID()).child(model.getEntityID());
        }
        else
        {
            return FirebasePaths.threadMessagesRef(model.getThread().getEntityID()).push();
        }
    }
    

}
