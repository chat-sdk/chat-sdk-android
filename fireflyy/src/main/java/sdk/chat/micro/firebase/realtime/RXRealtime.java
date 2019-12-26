package sdk.chat.micro.firebase.realtime;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import sdk.chat.micro.events.EventType;

public class RXRealtime implements Action {

    protected ChildEventListener listener;
    protected Query ref;

    public static class DocumentChange {
        DataSnapshot snapshot;
        EventType type;

        public DocumentChange(DataSnapshot snapshot, EventType type) {
            this.snapshot = snapshot;
            this.type = type;
        }
    }

    public Observable<DocumentChange> on(Query ref) {
        return Observable.create((ObservableOnSubscribe<DocumentChange>) emitter -> {
            RXRealtime.this.ref = ref;
            listener = ref.addChildEventListener(new RealtimeEventListener().onChildAdded((snapshot, s, hasValue) -> {
                if (hasValue) {
                    emitter.onNext(new DocumentChange(snapshot, EventType.Added));
                }
//                else {
//                    emitter.onError(new Exception(Fly.y.context().getString(R.string.error_null_snapshot)));
//                }
            }).onChildRemoved((snapshot, hasValue) -> {
                if (hasValue) {
                    emitter.onNext(new DocumentChange(snapshot, EventType.Removed));
                }
//                else {
//                    emitter.onError(new Exception(Fly.y.context().getString(R.string.error_null_snapshot)));
//                }
            }).onChildChanged((snapshot, s, hasValue) -> {
                if (hasValue) {
                    emitter.onNext(new DocumentChange(snapshot, EventType.Modified));
                }
//                else {
//                    emitter.onError(new Exception(Fly.y.context().getString(R.string.error_null_snapshot)));
//                }
            }).onCancelled(error -> emitter.onError(error.toException())));
        }).doOnDispose(this);
    }

    public Single<String> add(DatabaseReference ref, Object data) {
        return Single.create(emitter -> {
            DatabaseReference childRef = ref.push();
            final String id = childRef.getKey();
            childRef.setValue(data).addOnSuccessListener(aVoid -> emitter.onSuccess(id)).addOnFailureListener(emitter::onError);
        });
    }

    public Completable delete(DatabaseReference ref) {
        return Completable.create(emitter -> ref.removeValue((databaseError, databaseReference) -> {
            if (databaseError != null) {
                emitter.onError(databaseError.toException());
            } else {
                emitter.onComplete();
            }
        }));
    }

    public Completable set(DatabaseReference ref, Object data) {
        return Completable.create(emitter -> ref.setValue(data, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                emitter.onError(databaseError.toException());
            } else {
                emitter.onComplete();
            }
        }));
    }

    public Single<DataSnapshot> get(Query ref) {
        return Single.create(emitter -> ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emitter.onSuccess(dataSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                emitter.onError(databaseError.toException());
            }
        }));
    }

    @Override
    public void run() throws Exception {
        if (listener != null && ref != null) {
            ref.removeEventListener(listener);
        }
    }
}
