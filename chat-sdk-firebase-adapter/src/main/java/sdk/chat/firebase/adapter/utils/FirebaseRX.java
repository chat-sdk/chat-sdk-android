package sdk.chat.firebase.adapter.utils;

import com.google.firebase.database.DatabaseReference;

import io.reactivex.Completable;
import sdk.guru.common.RX;

/**
 * Created by ben on 9/27/17.
 */

public class FirebaseRX {

    public static Completable remove (final DatabaseReference ref) {
        return Completable.create(e -> ref.removeValue((databaseError, databaseReference) -> {
            if(databaseError != null) {
                e.onError(new Throwable(databaseError.getMessage()));
            }
            else {
                e.onComplete();
            }
        })).subscribeOn(RX.io());
    }

    public static Completable set (final DatabaseReference ref, final Object value) {
        return set(ref, value, false);
    }

    public static Completable set (final DatabaseReference ref, final Object value, final boolean onDisconnectRemoveValue) {
        return Completable.create(e -> {
            ref.setValue(value, (databaseError, databaseReference) -> {
                if(databaseError != null) {
                    e.onError(new Throwable(databaseError.getMessage()));
                }
                else {
                    e.onComplete();
                }
            });
            if(onDisconnectRemoveValue) {
                ref.onDisconnect().removeValue();
            }
        }).subscribeOn(RX.io());
    }
}
