/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package sdk.chat.firebase.adapter.wrappers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ReadReceiptUserLink;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.R;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.utils.Generic;
import sdk.guru.common.RX;
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
        if (!map.isEmpty()) {
            values.put(FirebasePaths.ReadPath, map);
        }
        if (model.getThread().typeIs(ThreadType.Private)) {
            values.put(Keys.To, getTo());
        }

        values.put(Keys.From, model.getSender().getEntityID());

        if (FirebaseModule.config().enableCompatibilityWithV4) {
            values.put("user-firebase-id", model.getSender().getEntityID());
        }

        return values;
    }

    protected ArrayList<String> getTo() {
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
            model.setDate(new Date(date));
        } else {
            Logger.debug("No Date");
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
            updateReadReceipts(readMap).doOnSuccess(aBoolean -> {
                if (aBoolean) {
                    ChatSDK.events().source().accept(NetworkEvent.messageReadReceiptUpdated(model));
                }
            }).ignoreElement().subscribe(ChatSDK.events());
        }

        model.update();
    }

    public Single<Boolean> updateReadReceipts(Map<String, Map<String, Long>> map) {
        return Single.defer((Callable<SingleSource<Boolean>>) () -> {

            final List<Single<Boolean>> singles = new ArrayList<>();

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

                    singles.add(model.setUserReadStatusAsync(user, new ReadStatus(status.intValue()), new Date(date), false));

                }
            }

            final List<Boolean> outcomes = new ArrayList<>();
            return Single.merge(singles).doOnNext(outcomes::add).ignoreElements().toSingle((Callable<Boolean>) () -> {
                for (Boolean b: outcomes) {
                    if (b) {
                        return true;
                    }
                }
                return false;
            });
        }).subscribeOn(RX.computation());
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
        }).subscribeOn(RX.io());
    }
    
    public Completable send() {
        return Completable.defer(() -> {
            if (model.getThread() != null) {
                return push();
            } else {
                return Completable.error(ChatSDK.getException(R.string.message_doesnt_have_a_thread));
            }
        }).subscribeOn(RX.io());
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
        }).subscribeOn(RX.io());
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

    public Completable markAsReceived() {
        return setReadStatus(ReadStatus.delivered());
    }

    public Completable setReadStatus (ReadStatus status) {
        return Completable.defer(() -> {
            if (model.getSender().isMe() || model.getThread().typeIs(ThreadType.Public) || !ChatSDK.thread().hasVoice(model.getThread(), ChatSDK.currentUser())) {
                return Completable.complete();
            }

            User currentUser = ChatSDK.currentUser();

            if (!model.getThread().containsUser(currentUser)) {
                return Completable.complete();
            }

            return model.setUserReadStatusAsync(ChatSDK.currentUser(), status, new Date(), true).flatMapCompletable(aBoolean -> {
                if (aBoolean) {
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
            });

            // Do this to stop duplication of links
        }).subscribeOn(RX.single());
    }
}
