package co.chatsdk.firestore;

import co.chatsdk.core.handlers.BlockingHandler;
import io.reactivex.Completable;
import sdk.chat.micro.Fireflyy;
import sdk.chat.micro.User;
import sdk.chat.micro.namespace.Fire;

public class FirestoreBlockingHandler implements BlockingHandler {

    @Override
    public Completable blockUser(String userEntityID) {
        return Fire.flyy.block(new User(userEntityID));
    }

    @Override
    public Completable unblockUser(String userEntityID) {
        return Fire.flyy.unblock(new User(userEntityID));
    }

    @Override
    public Boolean isBlocked(String userEntityID) {
        return Fire.flyy.isBlocked(new User(userEntityID));
    }

    @Override
    public boolean blockingSupported() {
        return true;
    }
}
