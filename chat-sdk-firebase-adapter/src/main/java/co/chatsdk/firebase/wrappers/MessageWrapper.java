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

import io.reactivex.CompletableSource;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ReadReceiptUserLink;
import sdk.chat.core.dao.User;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.R;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.utils.Generic;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import sdk.guru.realtime.RXRealtime;

public class MessageWrapper  {

    private Message model;

    public MessageWrapper(Message model) {
        this.model = model;
    }

    public MessageWrapper(DataSnapshot snapshot) {
        this.model = ChatSDK.db().fetchOrCreateEntityWithEntityID(Message.class, snapshot.getKey());
        deserialize(snapshot);
    }

    public MessageWrapper(String entityID) {
        this.model = ChatSDK.db().fetchOrCreateEntityWithEntityID(Message.class, entityID);
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

        if (FirebaseModule.config().enableCompatibilityWithV4) {
            values.put("user-firebase-id", model.getSender().getEntityID());
            values.put("json_v2", model.getMetaValuesAsMap());
        }

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

    private void deserialize(DataSnapshot snapshot) {

        if (snapshot.getValue() == null) {
            return;
        }

        if (snapshot.hasChild(Keys.Meta)) {
            model.setMetaValues(snapshot.child(Keys.Meta).getValue(Generic.mapStringObject()));
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
                ChatSDK.core().userOn(user).subscribe(ChatSDK.events());
            }

            model.setSender(user);
        }

        Map<String, Map<String, Long>> readMap = snapshot.child(Keys.Read).getValue(Generic.readReceiptHashMap());
        if (readMap != null) {
            updateReadReceipts(readMap);
        }

        model.update();
    }

    public void updateReadReceipts (Map<String, Map<String, Long>> map) {
        for(String key : map.keySet()) {

            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, key);

            Map<String, Long> statusMap = map.get(key);

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
                return Completable.error(ChatSDK.getException(R.string.message_doesnt_have_a_thread));
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
        return Completable.defer(() -> {
            if (model.getSender().isMe()) {
                return Completable.complete();
            }

            User currentUser = ChatSDK.currentUser();

            if (!model.getThread().containsUser(currentUser)) {
                return Completable.complete();
            }

            if(model.setUserReadStatus(ChatSDK.currentUser(), status, new DateTime())) {

                Map<String, Object> map = new HashMap<String, Object>() {{
                    put(Keys.Status, status.getValue());
                    put(Keys.Date, ServerValue.TIMESTAMP);
                }};

                DatabaseReference ref = FirebasePaths.threadMessagesReadRef(model.getThread().getEntityID(), model.getEntityID()).child(currentUser.getEntityID());
                RXRealtime realtime = new RXRealtime();
                Completable completable = realtime.set(ref, map);
                realtime.addToReferenceManager();

                return completable;
            }
            return Completable.complete();

        }).subscribeOn(Schedulers.io());
    }
}
