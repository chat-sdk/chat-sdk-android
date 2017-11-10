package co.chatsdk.firebase.utils;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 9/27/17.
 */

public class FirebaseRX {

    public static Completable remove (final DatabaseReference ref) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                ref.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError != null) {
                            e.onError(new Throwable(databaseError.getMessage()));
                        }
                        else {
                            e.onComplete();
                        }
                    }
                });
            }
        }).subscribeOn(Schedulers.single());
    }

    public static Completable set (final DatabaseReference ref, final Object value) {
        return set(ref, value, false);
    }

    public static Completable set (final DatabaseReference ref, final Object value, final boolean onDisconnectRemoveValue) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                ref.setValue(value, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError != null) {
                            e.onError(new Throwable(databaseError.getMessage()));
                        }
                        else {
                            e.onComplete();
                        }
                    }
                });
                if(onDisconnectRemoveValue) {
                    ref.onDisconnect().removeValue();
                }
            }
        }).subscribeOn(Schedulers.single());
    }
}
