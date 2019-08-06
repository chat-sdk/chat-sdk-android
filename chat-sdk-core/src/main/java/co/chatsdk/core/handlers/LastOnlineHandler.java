package co.chatsdk.core.handlers;

import java.util.Date;

import co.chatsdk.core.dao.User;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface LastOnlineHandler {

    Single<Date> getLastOnline (User user);
    Completable setLastOnline (User user);

}
