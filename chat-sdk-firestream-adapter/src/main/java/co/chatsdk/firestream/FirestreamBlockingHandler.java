package co.chatsdk.firestream;

import co.chatsdk.core.handlers.BlockingHandler;
import firestream.chat.chat.User;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;

public class FirestreamBlockingHandler implements BlockingHandler {

    @Override
    public Completable blockUser(String userEntityID) {
        return Fire.Stream.block(new User(userEntityID));
    }

    @Override
    public Completable unblockUser(String userEntityID) {
        return Fire.Stream.unblock(new User(userEntityID));
    }

    @Override
    public Boolean isBlocked(String userEntityID) {
        return Fire.Stream.isBlocked(new User(userEntityID));
    }

    @Override
    public boolean blockingSupported() {
        return true;
    }
}
