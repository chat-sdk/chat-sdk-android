package sdk.chat.waka;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.wrappers.MessageWrapper;

public class V4V5MessageWrapper extends MessageWrapper {

    public static final String UserFirebaseId = "user-firebase-id";
    public static final String UserName = "userName";
    public static final String JSON = "json_v2";

    public V4V5MessageWrapper(Message model) {
        super(model);
    }

    public V4V5MessageWrapper(DataSnapshot snapshot) {
        super(snapshot);
    }

    public V4V5MessageWrapper(String entityID) {
        super(entityID);
    }

    protected Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();

        data.put(JSON, data.get(Keys.Meta));
        data.put(UserFirebaseId, data.get(Keys.From));

        return data;
    }

    @Override
    public Completable send() {
        return super.send().concatWith(pushLastMessage());
    }

    public HashMap<String, Object> lastMessageData() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Keys.Type, model.getType());
        map.put(Keys.Date, ServerValue.TIMESTAMP);
        map.put(UserFirebaseId, model.getSender().getEntityID());
        map.put(Keys.From, model.getSender().getEntityID());
        map.put(UserName, model.getSender().getName());
        map.put(JSON, model.getMetaValuesAsMap());
        map.put(Keys.Meta, model.getMetaValuesAsMap());
        return map;
    }

    public Completable pushLastMessage () {
        return Completable.create(e -> FirebasePaths.threadRef(model.getThread().getEntityID()).child(FirebasePaths.LastMessagePath).setValue(lastMessageData(), (databaseError, databaseReference) -> {
            if(databaseError == null) {
                e.onComplete();
            }
            else {
                e.onError(databaseError.toException());
            }
        })).subscribeOn(Schedulers.single());
    }


    protected void deserialize(DataSnapshot snapshot) {
        super.deserialize(snapshot);
    }
}