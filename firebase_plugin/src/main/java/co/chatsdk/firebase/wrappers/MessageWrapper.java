/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import co.chatsdk.firebase.FirebasePaths;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoCore;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ChildEventListener;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.dao.DaoDefines;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import timber.log.Timber;

public class MessageWrapper  {

    private final String TAG = this.getClass().getSimpleName();
    private static boolean DEBUG = true;
    private ChildEventListener readReceiptListener;
    private BMessage model;

    public MessageWrapper(BMessage model){
        this.model = model;
    }

    public MessageWrapper(DataSnapshot snapshot){
        this.model = StorageManager.shared().fetchOrCreateEntityWithEntityID(BMessage.class, snapshot.getKey());
        deserialize(snapshot);
    }

    Map<String, Object> serialize(){
        Map<String, Object> values = new HashMap<String, Object>();

        values.put(DaoDefines.Keys.Payload, model.getTextString());
        values.put(DaoDefines.Keys.JSON, model.getRawJSONPayload());
        values.put(DaoDefines.Keys.Date, ServerValue.TIMESTAMP);
        values.put(DaoDefines.Keys.Type, model.getType());
        values.put(DaoDefines.Keys.UserFirebaseId, model.getSender().getEntityID());


        return values;
    }

    private boolean contains (Map<String, Object> value, String key) {
        return value.containsKey(key) && !value.get(key).equals("");
    }

    private String string (Map<String, Object> value, String key) {
        if(contains(value, key)) {
            return (String) value.get(key);
        }
        return null;
    }

    private Long long_ (Map<String, Object> value, String key) {
        if(contains(value, key)) {
            Object o = value.get(key);
            if(o instanceof Long) {
                return (Long) value.get(key);
            }
            if(o instanceof Integer) {
                return ((Integer) value.get(key)).longValue();
            }
        }
        return null;
    }

    @SuppressWarnings("all") void deserialize(DataSnapshot snapshot) {

        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
        if (DEBUG) Timber.v("deserialize, Value: %s", value);
        if (value == null) return;

        String json = string(value, DaoDefines.Keys.JSON);

        if(json != null) {
            model.setRawJSONPayload(json);
        }
        else {
            String text = string(value, DaoDefines.Keys.Payload);
            if(text != null) {
                model.setTextString(text);
            }
            else {
                model.setTextString("");
            }
        }

        Long type = long_(value, DaoDefines.Keys.Type);
        if(type != null) {
            model.setType(type.intValue());
        }

        Long date = long_(value, DaoDefines.Keys.Date);
        if(date != null) {
            model.setDate(new DateTime(date));
        }

        String senderID = string(value, DaoDefines.Keys.UserFirebaseId);
        if(senderID != null) {
            BUser user = DaoCore.fetchEntityWithEntityID(BUser.class, senderID);
            if (user == null)
            {
                user = StorageManager.shared().fetchOrCreateEntityWithEntityID(BUser.class, senderID);
                UserWrapper.initWithModel(user).once();
            }

            model.setSender(user);

        }

        DaoCore.updateEntity(model);
    }

    public Completable push() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                // Getting the message ref. Will be created if not exist.
                final DatabaseReference ref = ref();
                model.setEntityID(ref.getKey());

                DaoCore.updateEntity(model);

                ref.setValue(serialize(), ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError == null) {
                            e.onComplete();
                        } else {
                            e.onError(firebaseError.toException());
                        }
                    }
                });
            }
        });
    }
    
    public Completable send() {
        if(model.getThread() != null) {
            return push();
        }
        else {
            return null;
        }
    }
    
    /**
     * The message model will be updated after this call.
     **/
    public void setDelivered(int delivered){
        model.setDelivered(delivered);
    }
    
    private DatabaseReference ref(){
        if (StringUtils.isNotEmpty(model.getEntityID()))
        {
            return FirebasePaths.threadMessagesRef(model.getThread().getEntityID()).child(model.getEntityID());
        }
        else
        {
            return FirebasePaths.threadMessagesRef(model.getThread().getEntityID()).push();
        }
    }

    public BMessage getModel() {
        return model;
    }

}
