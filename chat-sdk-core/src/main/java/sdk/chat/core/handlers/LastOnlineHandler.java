package sdk.chat.core.handlers;

import java.util.Date;

import sdk.chat.core.dao.User;
import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.guru.common.Optional;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface LastOnlineHandler {

    Single<Optional<Date>> getLastOnline (User user);
    Completable setLastOnline (User user);

}
