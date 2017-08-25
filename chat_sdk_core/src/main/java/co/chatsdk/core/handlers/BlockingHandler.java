package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.User;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface BlockingHandler {

    Completable blockUser (User user);
    Completable unblockUser (User user);
    Single<Boolean> isBlocked (User user);

}
