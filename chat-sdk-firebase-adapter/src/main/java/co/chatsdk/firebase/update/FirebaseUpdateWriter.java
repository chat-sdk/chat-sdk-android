package co.chatsdk.firebase.update;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;

public class FirebaseUpdateWriter {

    ArrayList<FirebaseUpdate> updates = new ArrayList<>();
    Type type;

    public enum Type {
        Set,
        Update
    }

    public FirebaseUpdateWriter (Type type) {
        this.type = type;
    }

    public void add (FirebaseUpdate update) {
        updates.add(update);
    }

    public Single<DatabaseReference> execute () {
        HashMap<String, Object> data = new HashMap<>();
        for (FirebaseUpdate update : updates) {
            data.put(update.path(), update.value);
        }

        switch (type) {
            case Set:
                return set(data);
            case Update:
                return update(data);
            default:
                return null;
        }
    }

    public Single<DatabaseReference> set (HashMap<String, Object> data) {
        return Single.create(emitter -> ref().setValue(data, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                emitter.onSuccess(databaseReference);
            } else {
                emitter.onError(databaseError.toException());
            }
        }));
    }

    public Single<DatabaseReference> update (HashMap<String, Object> data) {
        return Single.create(emitter -> ref().updateChildren(data, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                emitter.onSuccess(databaseReference);
            } else {
                emitter.onError(databaseError.toException());
            }
        }));
    }

    protected DatabaseReference ref () {
        return FirebasePaths.firebaseRawRef();
    }
}
