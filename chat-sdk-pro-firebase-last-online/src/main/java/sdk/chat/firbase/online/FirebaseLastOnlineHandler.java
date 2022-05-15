package sdk.chat.firbase.online;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.LastOnlineHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.guru.common.Optional;
import sdk.guru.realtime.RealtimeEventListener;

public class FirebaseLastOnlineHandler implements LastOnlineHandler {

    public FirebaseLastOnlineHandler() {
        ChatSDK.appBackgroundMonitor().addListener(new AppBackgroundMonitor.Listener() {
            @Override
            public void didStart() {
            }

            @Override
            public void didStop() {
                updateLastOnline().subscribe();
            }
        });
    }

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
    public Completable updateLastOnline() {
        return Completable.create(emitter -> {
            User user = ChatSDK.currentUser();
            if (user != null && ChatSDK.auth() != null && ChatSDK.auth().isAuthenticatedThisSession()) {
                ref(user).setValue(ServerValue.TIMESTAMP, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(databaseError.toException());
                    }
                });
            }
            else {
                emitter.onComplete();
            }
        });
    }

    protected DatabaseReference ref (User user) {
        return FirebasePaths.userRef(user.getEntityID()).child(Keys.LastOnline);
    }
}
