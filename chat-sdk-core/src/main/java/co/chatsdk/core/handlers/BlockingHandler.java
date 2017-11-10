package co.chatsdk.core.handlers;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface BlockingHandler {

    Completable blockUser (String userEntityID);
    Completable unblockUser (String userEntityID);
    Single<Boolean> isBlocked (String userEntityID);
    boolean blockingSupported ();

}
