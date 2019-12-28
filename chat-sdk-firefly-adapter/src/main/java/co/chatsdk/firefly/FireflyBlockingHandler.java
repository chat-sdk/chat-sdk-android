package co.chatsdk.firefly;

import co.chatsdk.core.handlers.BlockingHandler;
import firefly.sdk.chat.chat.User;
import io.reactivex.Completable;
import firefly.sdk.chat.namespace.Fl;

public class FireflyBlockingHandler implements BlockingHandler {

    @Override
    public Completable blockUser(String userEntityID) {
        return Fl.y.block(new User(userEntityID));
    }

    @Override
    public Completable unblockUser(String userEntityID) {
        return Fl.y.unblock(new User(userEntityID));
    }

    @Override
    public Boolean isBlocked(String userEntityID) {
        return Fl.y.isBlocked(new User(userEntityID));
    }

    @Override
    public boolean blockingSupported() {
        return true;
    }
}
