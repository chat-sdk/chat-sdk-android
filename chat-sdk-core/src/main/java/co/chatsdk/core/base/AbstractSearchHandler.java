package co.chatsdk.core.base;

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

    public Observable<User> usersForIndex(final String value, final String index) {
        return usersForIndex(value, ChatSDK.config().contactsToLoadPerBatch, index);
    }

}
