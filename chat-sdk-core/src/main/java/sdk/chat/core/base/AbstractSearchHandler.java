package sdk.chat.core.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Maybe;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.SearchHandler;
import sdk.chat.core.session.ChatSDK;
import io.reactivex.Observable;

public abstract class AbstractSearchHandler implements SearchHandler {

    public Observable<User> usersForIndex(final String value) {
        return usersForIndex(value, ChatSDK.config().userSearchLimit);
    }

    public Observable<User> usersForIndexes(final String value, final String... indexes) {
        return usersForIndexes(value, ChatSDK.config().userSearchLimit, indexes);
    }

    @Override
    public Maybe<User> userForIndex(String value, String index) {
        return usersForIndex(value, 1, index).firstElement();
    }

    public Observable<User> usersForIndexes(final String value, List<String> indexes) {
        return usersForIndexes(value, ChatSDK.config().userSearchLimit, indexes);
    }

    public Observable<User> usersForIndex(final String value, final String index) {
        return usersForIndex(value, ChatSDK.config().userSearchLimit, index);
    }

    public Observable<User> usersForIndexes(String value, int limit, List<String> indexes) {
        ArrayList<Observable<User>> observables = new ArrayList<>();
        for (String index : indexes) {
            observables.add(usersForIndex(value, limit, index));
        }
        return Observable.merge(observables);
    }

    public Observable<User> usersForIndexes(String value, int limit, String... indexes) {
        return usersForIndexes(value, limit, Arrays.asList(indexes));
    }

    public boolean canAddUserById() {
        return false;
    }

}
