/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.ReadReceiptUserLink;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.ReadStatus;

import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.R;
import co.chatsdk.firebase.utils.Generic;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
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
        Map<String, Object> values = new HashMap<>();

        values.put(Keys.Meta, model.getMetaValuesAsMap());
        values.put(Keys.Date, ServerValue.TIMESTAMP);
        values.put(Keys.Type, model.getType());

        HashMap<String, Map<String, Integer>> map = new HashMap<>();
        for (ReadReceiptUserLink link: getModel().getReadReceiptLinks()) {

            HashMap<String, Integer> status = new HashMap<>();
            status.put(Keys.Status, link.getStatus());
            map.put(link.getUser().getEntityID(), status);
        }

        values.put(FirebasePaths.ReadPath, map);

        values.put(Keys.To, getTo());
        values.put(Keys.From, model.getSender().getEntityID());

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

    private boolean contains (Map<String, Object> value, String key) {
        return value.containsKey(key) && !value.get(key).equals("");
    }

    private String string (Map<String, Object> value, String key) {
        if(contains(value, key)) {
            return (String) value.get(key);
        }
        return null;
    }

    void deserialize(DataSnapshot snapshot) {

        if (snapshot.getValue() == null) {
            return;
        }

        if (snapshot.hasChild(Keys.Meta)) {
            model.setMetaValues(snapshot.child(Keys.Meta).getValue(Generic.hashMapStringObject()));
        } else {
            Logger.debug("");
//            model.setText("");
        }

        if (snapshot.hasChild(Keys.Type)) {
            model.setType(snapshot.child(Keys.Type).getValue(Long.class).intValue());
        }

        if (snapshot.hasChild(Keys.Date)) {
            Long date = snapshot.child(Keys.Date).getValue(Long.class);

            // If the server time of the text is too different to local time
            // set the status to none, which causes the text to be refreshed
            // in the chat view.
//            if (this.getModel().getDate() == null || Math.abs(this.getModel().getDate().toDate().getTime() - date) > 1000) {
//                model.setMessageStatus(MessageSendStatus.None);
                Logger.debug("Do we need this");
//            }
            model.setDate(new DateTime(date));
        }

        if (snapshot.hasChild(Keys.From)) {
            String senderID = snapshot.child(Keys.From).getValue(String.class);
            User user = DaoCore.fetchEntityWithEntityID(User.class, senderID);
            if (user == null) {
                user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, senderID);
                UserWrapper.initWithModel(user).once();
            }

            model.setSender(user);
        }

        HashMap<String, HashMap<String, Long>> readMap = snapshot.child(Keys.Read).getValue(Generic.readReceiptHashMap());
        if (readMap != null) {
            updateReadReceipts(readMap);
        }

        model.update();
    }

    public void updateReadReceipts (HashMap<String, HashMap<String, Long>> map) {
        for(String key : map.keySet()) {

            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, key);

            HashMap<String, Long> statusMap = map.get(key);

            if (statusMap != null) {

                Long status = statusMap.get(Keys.Status);
                if (status == null) {
                    status = (long) ReadStatus.None;
                }

                Long date = statusMap.get(Keys.Date);
                if (date == null) {
                    date = 0L;
                }

                model.setUserReadStatus(user, new ReadStatus(status.intValue()), new DateTime(date));

            }
        }
    }

    public Completable push() {
        return Completable.create(e -> {

            // Getting the text ref. Will be created if not exist.
            final DatabaseReference ref = ref();
            model.setEntityID(ref.getKey());
            model.update();

            ref.setValue(serialize(), ServerValue.TIMESTAMP, (firebaseError, firebase) -> {
                if (firebaseError == null) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(HookEvent.Message, model);
                    ChatSDK.hook().executeHook(HookEvent.MessageSent, data).subscribe(ChatSDK.events());
                    e.onComplete();
                } else {
                    e.onError(firebaseError.toException());
                }
            });
        }).subscribeOn(Schedulers.io());
    }
    
    public Completable send() {
        return Completable.defer(() -> {
            if (model.getThread() != null) {
                return push();
            } else {
                return Completable.error(new Throwable(ChatSDK.shared().getString(R.string.message_doesnt_have_a_thread)));
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable delete() {
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
        }).subscribeOn(Schedulers.io());
    }
    
    private DatabaseReference ref() {
        if (model.getEntityID() != null && !model.getEntityID().isEmpty()) {
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

            ReadStatus currentStatus = model.readStatusForUser(ChatSDK.currentUser());

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

        }).subscribeOn(Schedulers.io());
    }
}
