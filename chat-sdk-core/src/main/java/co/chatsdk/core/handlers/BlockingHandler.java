package co.chatsdk.core.handlers;

import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface BlockingHandler {

    Completable blockUser (String userEntityID);
    Completable unblockUser (String userEntityID);
    Boolean isBlocked (String userEntityID);
    boolean blockingSupported ();

}
