package firestream.chat.firebase.realtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.Callable;

import firefly.sdk.chat.R;
import firestream.chat.firebase.rx.Optional;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import firestream.chat.events.EventType;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RXRealtime implements Action, Consumer<Throwable> {

    protected ChildEventListener childListener;
    protected ValueEventListener valueListener;
    protected Query ref;

    public static class DocumentChange {
        DataSnapshot snapshot;
        EventType type;

        public DocumentChange(DataSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        public DocumentChange(DataSnapshot snapshot, EventType type) {
            this.snapshot = snapshot;
            this.type = type;
        }
    }

    public Observable<DocumentChange> on(Query ref) {
        return Observable.create((ObservableOnSubscribe<DocumentChange>) emitter -> {
            RXRealtime.this.ref = ref;
            valueListener = ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        emitter.onNext(new DocumentChange(snapshot));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    emitter.onError(databaseError.toException());
                }
            });
        }).doOnDispose(this).subscribeOn(Schedulers.io());
    }

    public Observable<DocumentChange> childOn(Query ref) {
        return Observable.create((ObservableOnSubscribe<DocumentChange>) emitter -> {
            RXRealtime.this.ref = ref;
            childListener = ref.addChildEventListener(new RealtimeEventListener().onChildAdded((snapshot, s, hasValue) -> {
                if (hasValue) {
                    emitter.onNext(new DocumentChange(snapshot, EventType.Added));
                }
            }).onChildRemoved((snapshot, hasValue) -> {
                if (hasValue) {
                    emitter.onNext(new DocumentChange(snapshot, EventType.Removed));
                }
            }).onChildChanged((snapshot, s, hasValue) -> {
                if (hasValue) {
                    emitter.onNext(new DocumentChange(snapshot, EventType.Modified));
                }
            }).onCancelled(error -> emitter.onError(error.toException())));
        }).doOnDispose(this).subscribeOn(Schedulers.io());
    }

    public Single<String> add(DatabaseReference ref, Object data) {
        return add(ref, data, null);
    }

    public Single<String> add(DatabaseReference ref, Object data, @Nullable Object priority) {
        return add(ref, data, priority, null);
    }

    public Single<String> add(DatabaseReference ref, Object data, @Nullable Object priority, @Nullable Consumer<String> newId) {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            DatabaseReference childRef = ref.push();
            final String id = childRef.getKey();
            if (newId != null) {
                newId.accept(id);
            }
            Task<Void> task;
            if (priority != null) {
                task = childRef.setValue(data, priority);
            } else {
                task = childRef.setValue(data);
            }
            task.addOnSuccessListener(aVoid -> emitter.onSuccess(id)).addOnFailureListener(emitter::onError);
        }).subscribeOn(Schedulers.io());
    }

    public Completable delete(DatabaseReference ref) {
        return Completable.create(emitter -> ref.removeValue((databaseError, databaseReference) -> {
            if (databaseError != null) {
                emitter.onError(databaseError.toException());
            } else {
                emitter.onComplete();
            }
        })).subscribeOn(Schedulers.io());
    }

    public Completable set(DatabaseReference ref, Object data) {
        return Completable.create(emitter -> ref.setValue(data).addOnSuccessListener(aVoid -> {
            emitter.onComplete();
        }).addOnFailureListener(e -> {
            emitter.onError(e);
        }).addOnCanceledListener(() -> {
            emitter.onError(new Exception(Fire.internal().context().getString(R.string.error_write_cancelled)));
        })).subscribeOn(Schedulers.io());
    }

    public Completable update(DatabaseReference ref, HashMap<String, Object> data) {
        return Completable.create(emitter -> ref.updateChildren(data, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                emitter.onError(databaseError.toException());
            } else {
                emitter.onComplete();
            }
        })).subscribeOn(Schedulers.io());
    }
    public Single<Optional<DataSnapshot>> get(Query ref) {
        return Single.defer((Callable<SingleSource<? extends Optional<DataSnapshot>>>) () -> {
            ref.keepSynced(true);
            return Single.create(emitter -> ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        emitter.onSuccess(new Optional<>(snapshot));
                    } else {
                        emitter.onSuccess(new Optional<>());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    emitter.onError(databaseError.toException());
                }
            }));
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public void run() throws Exception {
        if (ref != null) {
            if (childListener != null) {
                ref.removeEventListener(childListener);
            }
            if (valueListener != null) {
                ref.removeEventListener(valueListener);
            }
        }
        ref = null;
        childListener = null;
        valueListener = null;
    }

    @Override
    public void accept(Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }
}
