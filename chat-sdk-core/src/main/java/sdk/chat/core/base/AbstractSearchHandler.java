package sdk.chat.core.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.SearchHandler;
import sdk.chat.core.session.ChatSDK;
import io.reactivex.Observable;

public abstract class AbstractSearchHandler implements SearchHandler {

    public Observable<User> usersForIndex(final String value) {
        return usersForIndex(value, ChatSDK.config().contactsToLoadPerBatch);
    }

    public Observable<User> usersForIndexes(final String value, final String... indexes) {
        return usersForIndexes(value, ChatSDK.config().contactsToLoadPerBatch, indexes);
    }

    public Observable<User> usersForIndexes(final String value, List<String> indexes) {
        return usersForIndexes(value, ChatSDK.config().contactsToLoadPerBatch, indexes);
    }

    public Observable<User> usersForIndex(final String value, final String index) {
        return usersForIndex(value, ChatSDK.config().contactsToLoadPerBatch, index);
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

}
