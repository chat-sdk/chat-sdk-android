package co.chatsdk.firestore;

import co.chatsdk.core.handlers.BlockingHandler;
import io.reactivex.Completable;
import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.User;

public class FirestoreBlockingHandler implements BlockingHandler {

    @Override
    public Completable blockUser(String userEntityID) {
        return MicroChatSDK.shared().block(new User(userEntityID));
    }

    @Override
    public Completable unblockUser(String userEntityID) {
        return MicroChatSDK.shared().unblock(new User(userEntityID));
    }

    @Override
    public Boolean isBlocked(String userEntityID) {
        return MicroChatSDK.shared().isBlocked(new User(userEntityID));
    }

    @Override
    public boolean blockingSupported() {
        return true;
    }
}
