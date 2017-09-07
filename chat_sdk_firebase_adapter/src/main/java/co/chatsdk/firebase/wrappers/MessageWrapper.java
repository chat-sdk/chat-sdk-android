/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.firebase.FirebasePaths;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Message;
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

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MessageWrapper  {

    private final String TAG = this.getClass().getSimpleName();
    private static boolean DEBUG = true;
    private ChildEventListener readReceiptListener;
    private Message model;

    public MessageWrapper(Message model){
        this.model = model;
    }

    public MessageWrapper(DataSnapshot snapshot){
        this.model = StorageManager.shared().fetchOrCreateEntityWithEntityID(Message.class, snapshot.getKey());
        deserialize(snapshot);
    }

    Map<String, Object> serialize(){
        Map<String, Object> values = new HashMap<String, Object>();

        values.put(Keys.Payload, model.getTextString());
        values.put(Keys.JSON, model.getRawJSONPayload());
        values.put(Keys.Date, ServerValue.TIMESTAMP);
        values.put(Keys.Type, model.getType());
        values.put(Keys.UserFirebaseId, model.getSender().getEntityID());


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

        String json = string(value, Keys.JSON);

        if(json != null) {
            model.setRawJSONPayload(json);
        }
        else {
            String text = string(value, Keys.Payload);
            if(text != null) {
                model.setTextString(text);
            }
            else {
                model.setTextString("");
            }
        }

        Long type = long_(value, Keys.Type);
        if(type != null) {
            model.setType(type.intValue());
        }

        Long date = long_(value, Keys.Date);
        if(date != null) {
            model.setDate(new DateTime(date));
        }

        String senderID = string(value, Keys.UserFirebaseId);
        if(senderID != null) {
            User user = DaoCore.fetchEntityWithEntityID(User.class, senderID);
            if (user == null)
            {
                user = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, senderID);
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
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                if(model.getThread() != null) {

                    push().doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            e.onError(throwable);
                        }
                    }).subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            e.onComplete();
                        }
                    });
                }
                else {
                    // TODO: Localize
                    e.onError(new Throwable("Message doesn't have a thread"));
                }
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());

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

    public Message getModel() {
        return model;
    }

}
