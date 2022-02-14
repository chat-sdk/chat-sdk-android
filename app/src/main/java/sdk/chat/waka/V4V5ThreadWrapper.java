package sdk.chat.waka;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.dao.Thread;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;

public class V4V5ThreadWrapper extends ThreadWrapper {

    public V4V5ThreadWrapper(Thread thread) {
        super(thread);
    }

    public V4V5ThreadWrapper(String entityId) {
        super(entityId);
    }

    protected Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();

        data.put("creator-entity-id", data.get("creator"));
        data.put("type_v4", data.get("type"));

        return data;
    }

    @Override
    public Completable push() {
        return super.push().andThen(Completable.create(e -> {

            final Map<String, Object> data = serialize();
            data.putAll(model.metaMap());

            DatabaseReference detailsRef = FirebasePaths.threadRef(model.getEntityID()).child("details");

            detailsRef.updateChildren(data, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    e.onComplete();
                }
                else {
                    e.onError(databaseError.toException());
                }
            });

        }));
    }


    protected void deserialize(DataSnapshot snapshot) {
        super.deserialize(snapshot);
    }

}
