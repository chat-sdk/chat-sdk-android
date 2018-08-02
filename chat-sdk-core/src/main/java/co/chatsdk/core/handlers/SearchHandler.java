package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.User;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface SearchHandler {

    // Checks all indexes
    Observable<User> usersForIndex(final String value);

    // Checks a particular index
    Observable<User> usersForIndex(final String value, final String index);

    // Checks a list of indexes
    Observable<User> usersForIndexes(final String value, final String... indexes);

}
