package co.chatsdk.firebase.update;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

import co.chatsdk.core.dao.User;

import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;

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
        return Single.create((SingleOnSubscribe<DatabaseReference>)emitter -> ref().setValue(data, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                emitter.onSuccess(databaseReference);
            } else {
                emitter.onError(databaseError.toException());
            }
        })).subscribeOn(Schedulers.io());
    }

    public Single<DatabaseReference> update (HashMap<String, Object> data) {
        return Single.create((SingleOnSubscribe<DatabaseReference>) emitter -> ref().updateChildren(data, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                emitter.onSuccess(databaseReference);
            } else {
                emitter.onError(databaseError.toException());
            }
        })).subscribeOn(Schedulers.io());
    }

    protected DatabaseReference ref () {
        return FirebasePaths.firebaseRawRef();
    }
}
