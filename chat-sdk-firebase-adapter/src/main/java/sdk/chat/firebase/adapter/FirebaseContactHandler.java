package sdk.chat.firebase.adapter;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import sdk.chat.core.base.BaseContactHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import io.reactivex.Completable;
import sdk.guru.common.RX;

public class FirebaseContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(User user, ConnectionType type) {
        return Completable.create(emitter -> {
            DatabaseReference ref = FirebasePaths.userContactsRef(ChatSDK.currentUserID()).child(user.getEntityID());
            HashMap<String, Integer> data = new HashMap<>();
            data.put(Keys.Type, type.ordinal());
            ref.setValue(data, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    emitter.onComplete();
                } else {
                    emitter.onError(databaseError.toException());
                }
            });
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable deleteContact(User user, ConnectionType type) {
        return Completable.create(emitter -> {
            DatabaseReference ref = FirebasePaths.userContactsRef(ChatSDK.currentUserID()).child(user.getEntityID());
            ref.removeValue((databaseError, databaseReference) -> {
                if (databaseError == null) {
                    emitter.onComplete();
                } else {
                    emitter.onError(databaseError.toException());
                }
            });
        }).subscribeOn(RX.io());
    }

}
