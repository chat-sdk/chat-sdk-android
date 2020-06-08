package sdk.chat.firebase.adapter.update;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

import sdk.chat.firebase.adapter.FirebasePaths;
import io.reactivex.Completable;
import sdk.guru.realtime.RXRealtime;

public class FirebaseUpdateWriter {

    ArrayList<FirebaseUpdate> updates = new ArrayList<>();

    public void add (FirebaseUpdate update) {
        updates.add(update);
    }

    public Completable execute () {
        return Completable.defer(() -> {
            HashMap<String, Object> data = new HashMap<>();
            for (FirebaseUpdate update : updates) {
                data.put(update.path(), update.value);
            }
            RXRealtime realtime = new RXRealtime();
            return realtime.update(ref(), data);
        });
    }

    protected DatabaseReference ref () {
        return FirebasePaths.firebaseRawRef();
    }
}
