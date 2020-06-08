package sdk.guru.realtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.pmw.tinylog.Logger;

import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import sdk.guru.common.EventType;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;

public class RXRealtime implements Action, Consumer<Throwable> {

    public interface DatabaseErrorListener {
        void onError(Query ref, DatabaseError error);
    }

    protected ChildEventListener childListener;
    protected ValueEventListener valueListener;
    protected Query ref;
    protected DatabaseErrorListener errorListener;

    public RXRealtime() {
    }

    public RXRealtime(DatabaseErrorListener listener) {
        this.errorListener = listener;
    }

    public Observable<DocumentChange> on(Query ref) {
        return Observable.create((ObservableOnSubscribe<DocumentChange>) emitter -> {
            RXRealtime.this.ref = ref;
            valueListener = ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists() && snapshot.getValue() != null) {
                        emitter.onNext(new DocumentChange(snapshot));
//                    } else {
//                        emitter.onNext(new DocumentChange(null));
//                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    emitter.onError(databaseError.toException());
                    Logger.debug(databaseError.toString() + ", " + RXRealtime.this.ref.toString());
                    if (errorListener != null) {
                        errorListener.onError(ref, databaseError);
                    }
                }
            });
        }).doOnDispose(this).subscribeOn(RX.io()).observeOn(RX.db());
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
            }).onCancelled(error -> {
                Logger.debug(ref.toString());
                emitter.onError(error.toException());
                if (errorListener != null) {
                    errorListener.onError(ref, error);
                }
            }));
        }).doOnDispose(this).subscribeOn(RX.io()).observeOn(RX.db());
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
        }).subscribeOn(RX.io()).observeOn(RX.db());
    }

    public Completable delete(DatabaseReference ref) {
        return Completable.create(emitter -> ref.removeValue((databaseError, databaseReference) -> {
            if (databaseError != null) {
                emitter.onError(databaseError.toException());
            } else {
                emitter.onComplete();
            }
        })).subscribeOn(RX.io()).observeOn(RX.db());
    }

    public Completable set(DatabaseReference ref, Object data) {
        return Completable.create(emitter -> ref.setValue(data).addOnSuccessListener(aVoid -> {
            emitter.onComplete();
        }).addOnFailureListener(e -> {
            Logger.debug("Database Error type: " + ref.toString());
            emitter.onError(e);
        }).addOnCanceledListener(() -> {
            Logger.debug("Listener Cancelled: " + ref.toString());
            emitter.onError(new Exception("Write cancelled"));
        })).subscribeOn(RX.io()).observeOn(RX.db());
    }

    public Completable update(DatabaseReference ref, Map<String, Object> data) {
        return Completable.create(emitter -> ref.updateChildren(data, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                emitter.onError(databaseError.toException());
            } else {
                emitter.onComplete();
            }
        })).subscribeOn(RX.io()).observeOn(RX.db());
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
        }).subscribeOn(RX.io()).observeOn(RX.db());
    }

    @Override
    public void run() {
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
    public void accept(Throwable throwable) {
        throwable.printStackTrace();
    }

    public ChildEventListener getChildListener() {
        return childListener;
    }

    public ValueEventListener getValueListener() {
        return valueListener;
    }

    public Query getRef() {
        return ref;
    }

    public void addToReferenceManager() {
        if (childListener != null) {
            RealtimeReferenceManager.shared().addRef(ref, childListener);
        }
        if (valueListener != null) {
            RealtimeReferenceManager.shared().addRef(ref, valueListener);
        }
    }

}
