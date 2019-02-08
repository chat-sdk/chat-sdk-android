package co.chatsdk.firebase;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import io.reactivex.Completable;

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
        });
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
        });
    }

}
