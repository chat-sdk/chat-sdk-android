package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.User;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface SearchHandler {

    Observable<User> usersForIndex(final String index, final String value);

}
