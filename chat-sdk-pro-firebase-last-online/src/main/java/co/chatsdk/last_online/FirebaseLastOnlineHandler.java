package co.chatsdk.last_online;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.Date;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.LastOnlineHandler;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Completable;
import io.reactivex.Single;

public class FirebaseLastOnlineHandler implements LastOnlineHandler {
    @Override
    public Single<Date> getLastOnline(User user) {
        return Single.create(emitter -> ref(user).addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
            if (hasValue && snapshot.getValue() instanceof Long) {
                Long timestamp = (Long) snapshot.getValue();
                emitter.onSuccess(new Date(timestamp));
            } else {
                emitter.onError(new Throwable());
            }
        })));
    }

    @Override
    public Completable setLastOnline(User user) {
        return Completable.create(emitter -> {
            if (user != null) {
                ref(user).setValue(ServerValue.TIMESTAMP, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(databaseError.toException());
                    }
                });
            }
            else {
                emitter.onError(new Throwable());
            }
        });
    }

    protected DatabaseReference ref (User user) {
        return FirebasePaths.userRef(user.getEntityID()).child(Keys.LastOnline);
    }
}
