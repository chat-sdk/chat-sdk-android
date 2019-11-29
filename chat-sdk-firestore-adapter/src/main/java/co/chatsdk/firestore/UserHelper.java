package co.chatsdk.firestore;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

public class UserHelper {

    public static Single<User> fetchUser (String entityId) {
        return Single.create(emitter -> {
            User user = ChatSDK.db().fetchUserWithEntityID(entityId);
            if (user != null) {
                emitter.onSuccess(user);
            } else {
                User finalUser = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityId);
                Disposable d1 = ChatSDK.core().userOn(user).subscribe(() -> emitter.onSuccess(finalUser), Throwable::printStackTrace);
            }

        });
    }

}
