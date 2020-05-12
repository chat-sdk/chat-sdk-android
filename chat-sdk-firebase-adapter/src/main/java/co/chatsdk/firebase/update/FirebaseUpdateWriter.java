package co.chatsdk.firebase.update;

import com.google.firebase.database.DatabaseReference;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import sdk.guru.common.RX;
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
