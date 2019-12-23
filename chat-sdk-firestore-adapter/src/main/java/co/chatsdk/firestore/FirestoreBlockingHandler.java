package co.chatsdk.firestore;

import co.chatsdk.core.handlers.BlockingHandler;
import io.reactivex.Completable;
import sdk.chat.micro.Fireflyy;
import sdk.chat.micro.User;
import sdk.chat.micro.namespace.Fire;
import sdk.chat.micro.namespace.Fly;

public class FirestoreBlockingHandler implements BlockingHandler {

    @Override
    public Completable blockUser(String userEntityID) {
        return Fly.y.block(new User(userEntityID));
    }

    @Override
    public Completable unblockUser(String userEntityID) {
        return Fly.y.unblock(new User(userEntityID));
    }

    @Override
    public Boolean isBlocked(String userEntityID) {
        return Fly.y.isBlocked(new User(userEntityID));
    }

    @Override
    public boolean blockingSupported() {
        return true;
    }
}
