package co.chatsdk.firebase.api.realtime;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import co.chatsdk.core.dao.User;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseReferenceManager;
import co.chatsdk.firebase.api.AbstractFirebaseAdapter;
import co.chatsdk.firebase.api.Path;
import co.chatsdk.firebase.api.Snapshot;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class RealtimeFirebaseAdapter extends AbstractFirebaseAdapter {

    public Observable<Snapshot> pathOn (Path path) {
        return Observable.create((ObservableOnSubscribe<Snapshot>) emitter -> {
            DatabaseReference ref = RealtimeRef.fromPath(path);



        }).subscribeOn(Schedulers.single());
    }

    public void pathOff (Path path) {
        DatabaseReference ref = RealtimeRef.fromPath(path);
        FirebaseReferenceManager.shared().removeListeners(ref);
    }


    public Observable<User> metaOn() {
        return Observable.create((ObservableOnSubscribe<User>) e -> {

            metaOff();

            final DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());

            if(FirebaseReferenceManager.shared().isOn(userMetaRef)) {
                e.onNext(model);
            }

            ValueEventListener listener = userMetaRef.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if (hasValue && snapshot.getValue() instanceof Map) {
                    deserializeMeta((Map<String, Object>) snapshot.getValue());
                    e.onNext(model);
                }
            }));

            FirebaseReferenceManager.shared().addRef(userMetaRef, listener);



        }).subscribeOn(Schedulers.single());
    }


}
