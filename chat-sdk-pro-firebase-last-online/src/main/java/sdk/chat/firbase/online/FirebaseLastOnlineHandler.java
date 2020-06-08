package sdk.chat.online;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.Date;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.LastOnlineHandler;
import sdk.guru.common.Optional;
import sdk.guru.realtime.RealtimeEventListener;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Completable;
import io.reactivex.Single;

public class FirebaseLastOnlineHandler implements LastOnlineHandler {
    @Override
    public Single<Optional<Date>> getLastOnline(User user) {
        return Single.create(emitter -> ref(user).addListenerForSingleValueEvent(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
            if (hasValue && snapshot.getValue() instanceof Long) {
                Long timestamp = (Long) snapshot.getValue();
                emitter.onSuccess(Optional.with(new Date(timestamp)));
            } else {
                emitter.onSuccess(Optional.empty());
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
