package firefly.sdk.chat.firebase.firestore;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import firefly.sdk.chat.firebase.rx.Optional;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import firefly.sdk.chat.R;
import firefly.sdk.chat.namespace.Fl;

public class RXFirestore implements Action {

    protected ListenerRegistration lr;

    public Observable<DocumentSnapshot> on(DocumentReference ref) {
        return Observable.create((ObservableOnSubscribe<DocumentSnapshot>) emitter -> {
            lr = ref.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    emitter.onError(e);
                } else if (snapshot != null) {
                    emitter.onNext(snapshot);
                }
            });
        }).doOnDispose(this);
    }

    public Observable<DocumentChange> on(Query ref) {
        return Observable.create((ObservableOnSubscribe<DocumentChange>) emitter -> {
            lr = ref.addSnapshotListener((snapshot, e) -> {
                if (snapshot != null) {
                    for (DocumentChange dc : snapshot.getDocumentChanges()) {
                        emitter.onNext(dc);
                    }
                }
            });
        }).doOnDispose(this);
    }

    public Single<DocumentReference> add(CollectionReference ref, Object data) {
        return Single.create((SingleOnSubscribe<DocumentReference>) emitter -> ref.add(data)
                .addOnSuccessListener(emitter::onSuccess)
                .addOnFailureListener(emitter::onError)).doOnDispose(this);
    }

    public Completable delete(DocumentReference ref) {
        return Completable.create(emitter -> ref.delete().addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError));
    }

    public Completable set(DocumentReference ref, Object data) {
        return Completable.create(emitter -> ref.set(data).addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError));
    }

    public Single<Optional<QuerySnapshot>> get(Query ref) {
        return Single.create(emitter -> ref.get().addOnSuccessListener(snapshots -> {
            if (snapshots != null) {
                emitter.onSuccess(new Optional<>(snapshots));
            } else {
                emitter.onSuccess(new Optional<>());
            }
        }).addOnFailureListener(emitter::onError));
    }

    public Single<Optional<DocumentSnapshot>> get(DocumentReference ref) {
        return Single.create(emitter -> ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists() && snapshot.getData() != null) {
                emitter.onSuccess(new Optional<>(snapshot));
            } else {
                emitter.onSuccess(new Optional<>());
            }
        }).addOnFailureListener(emitter::onError));
    }

    @Override
    public void run() throws Exception {
        if (lr != null) {
            lr.remove();
        }
    }
}
