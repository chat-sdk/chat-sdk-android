/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.R;
import co.chatsdk.firebase.utils.FirebaseRX;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MessageWrapper  {

    private Message model;

    public MessageWrapper(Message model){
        this.model = model;
    }

    public MessageWrapper(DataSnapshot snapshot){
        this.model = ChatSDK.db().fetchOrCreateEntityWithEntityID(Message.class, snapshot.getKey());
        deserialize(snapshot);
    }

    Map<String, Object> serialize() {
        Map<String, Object> values = new HashMap<String, Object>();

        values.put(Keys.JSON, model.getMetaValuesAsMap());
        values.put(Keys.Meta, model.getMetaValuesAsMap());
        values.put(Keys.Date, ServerValue.TIMESTAMP);
        values.put(Keys.Type, model.getType());
        values.put(Keys.UserFirebaseId, model.getSender().getEntityID());
        values.put(FirebasePaths.ReadPath, initialReadReceipts());
        values.put(Keys.To, getTo());
        values.put(Keys.From, ChatSDK.currentUserID());

        return values;
    }

    protected ArrayList<String> getTo () {
        ArrayList<String> users = new ArrayList<>();
        for (User user : model.getThread().getUsers()) {
            if(!user.isMe()) {
                users.add(user.getEntityID());
            }
        }
        return users;
    }

    private HashMap<String, HashMap<String, Integer>> initialReadReceipts () {
        HashMap<String, HashMap<String, Integer>> map = new HashMap<>();

        for(User u : getModel().getThread().getUsers()) {
            ReadStatus readStatus = u.isMe() ? ReadStatus.read() : ReadStatus.none();

            HashMap<String, Integer> status = new HashMap<>();
            status.put(Keys.Status, readStatus.getValue());
            map.put(u.getEntityID(), status);
        }

        return map;
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

    private HashMap<String, Object> map (Map<String, Object> value, String key) {
        if(contains(value, key)) {
            if (value.get(key) instanceof HashMap) {
                return (HashMap<String, Object>) value.get(key);
            }
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
        //if (DEBUG) Timber.v("deserialize, Value: %s", value);
        if (value == null) return;

        Object json = snapshot.child(Keys.JSON).getValue();

        if (json != null && json instanceof HashMap) {
            model.setMetaValues((HashMap) json);
        }
        else {
            model.setText("");
        }

        Long type = long_(value, Keys.Type);
        if(type != null) {
            model.setType(type.intValue());
        }

        Long date = long_(value, Keys.Date);
        if(date != null) {
            // If the server time of the message is too different to local time
            // set the status to none, which causes the message to be refreshed
            // in the chat view.
            if (this.getModel().getDate() == null || Math.abs(this.getModel().getDate().toDate().getTime() - date) > 1000) {
                model.setMessageStatus(MessageSendStatus.None);
            }
            model.setDate(new DateTime(date));
        }

        String senderID = string(value, Keys.UserFirebaseId);
        if(senderID != null) {
            User user = DaoCore.fetchEntityWithEntityID(User.class, senderID);
            if (user == null)
            {
                user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, senderID);
                UserWrapper.initWithModel(user).once();
            }

            model.setSender(user);

        }

        // Get read information
        HashMap<String, Object> readMap = map(value, Keys.Read);
        if (readMap != null) {
            updateReadReceipts(readMap);
        }

        model.update();
    }

    public void updateReadReceipts (HashMap<String, Object> map) {
        for(String key : map.keySet()) {

            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, key);

            Object innerMap = map.get(key);

            if(innerMap != null && innerMap instanceof HashMap) {

                Map<String, Object> statusMap = (Map) innerMap;
                long status = ReadStatus.None;
                long date = 0;

                if(statusMap.get(Keys.Status) instanceof Long) {
                    status = (Long) statusMap.get(Keys.Status);
                }
                if(statusMap.get(Keys.Date) instanceof Long) {
                    date = (Long) statusMap.get(Keys.Date);
                }

                model.setUserReadStatus(user, new ReadStatus((int) status), new DateTime(date));
            }
        }
    }

    public Completable push() {
        return Completable.create(e -> {

            // Getting the message ref. Will be created if not exist.
            final DatabaseReference ref = ref();
            model.setEntityID(ref.getKey());
            DaoCore.updateEntity(model);

            ref.setValue(serialize(), ServerValue.TIMESTAMP, (firebaseError, firebase) -> {
                if (firebaseError == null) {
                    e.onComplete();
                } else {
                    e.onError(firebaseError.toException());
                }
            });
        });
    }
    
    public Completable send() {
        if (model.getThread() != null) {
            return push().concatWith(Completable.defer(() -> new ThreadWrapper(model.getThread()).pushLastMessage(lastMessageData()))).doOnComplete(() -> {
                FirebaseEntity.pushThreadMessagesUpdated(model.getThread().getEntityID());

                model.setMessageStatus(MessageSendStatus.Sent);
                ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(model)));

            }).doOnError(throwable -> {
                model.setMessageStatus(MessageSendStatus.Failed);
                ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(model)));
            }).subscribeOn(Schedulers.single());
        } else {
            return Completable.error(new Throwable(ChatSDK.shared().context().getString(R.string.message_doesnt_have_a_thread)));
        }
    }

    public HashMap<String, Object> lastMessageData () {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Keys.Type, model.getType());
        map.put(Keys.Date, ServerValue.TIMESTAMP);
        map.put(Keys.UserFirebaseId, model.getSender().getEntityID());
        map.put(Keys.From, model.getSender().getEntityID());
        map.put(Keys.UserName, model.getSender().getName());
        map.put(Keys.JSON, model.getMetaValuesAsMap());
        map.put(Keys.Meta, model.getMetaValuesAsMap());
        return map;
    }

    public Completable delete () {
        return Completable.create(e -> {
            DatabaseReference ref = FirebasePaths.threadMessagesRef(model.getThread().getEntityID()).child(model.getEntityID());
            ref.removeValue((databaseError, databaseReference) -> {
                if (databaseError == null) {
                    e.onComplete();
                }
                else {
                    e.onError(new Throwable(databaseError.getMessage()));
                }
            });
        });
    }
    
    private DatabaseReference ref() {
        if (StringUtils.isNotEmpty(model.getEntityID())) {
            return FirebasePaths.threadMessagesRef(model.getThread().getEntityID()).child(model.getEntityID());
        }
        else {
            return FirebasePaths.threadMessagesRef(model.getThread().getEntityID()).push();
        }
    }

    public Message getModel() {
        return model;
    }

    public Completable markAsReceived () {
        return setReadStatus(ReadStatus.delivered());
    }

    public Completable setReadStatus (ReadStatus status) {
        return Completable.create(emitter -> {

            if (model.getSender().isMe()) {
                emitter.onComplete();
                return;
            }

            if (!model.getThread().containsUser(ChatSDK.currentUser())) {
                return;
            }

            String entityID = ChatSDK.currentUserID();

            ReadStatus currentStatus = model.getReadStatus();

            if (currentStatus.getValue() >= status.getValue()) {
                emitter.onComplete();
                return;
            }

            model.setUserReadStatus(ChatSDK.currentUser(), status, new DateTime());

            HashMap<String, Object> map = new HashMap<>();
            map.put(Keys.Status, status.getValue());
            map.put(Keys.Date, ServerValue.TIMESTAMP);

            DatabaseReference ref = FirebasePaths.threadMessagesReadRef(model.getThread().getEntityID(), model.getEntityID());
            ref.child(entityID).setValue(map, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    emitter.onComplete();
                } else {
                    emitter.onError(databaseError.toException());
                }
            });

        });
    }
}
