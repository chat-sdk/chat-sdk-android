package co.chatsdk.core.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.session.ChatSDK;
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
