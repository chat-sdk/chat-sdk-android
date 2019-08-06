package co.chatsdk.core.handlers;

import java.util.List;

import co.chatsdk.core.dao.User;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface SearchHandler {

    // Checks all indexes
    Observable<User> usersForIndex(final String value);
    Observable<User> usersForIndex(final String value, int limit);

    // Checks a particular index
    Observable<User> usersForIndex(final String value, final String index);

    Observable<User> usersForIndex(final String value, int limit, final String index);
    Observable<User> usersForIndexes(final String value, List<String> indexes);

    // Checks a list of indexes
    Observable<User> usersForIndexes(final String value, final String... indexes);

    Observable<User> usersForIndexes(final String value, int limit, final String... indexes);
    Observable<User> usersForIndexes(String value, int limit, List<String> indexes);
}
