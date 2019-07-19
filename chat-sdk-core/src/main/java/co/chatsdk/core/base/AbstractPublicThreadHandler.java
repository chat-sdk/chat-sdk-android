package co.chatsdk.core.base;

import java.util.HashMap;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.PublicThreadHandler;
import io.reactivex.Single;

public abstract class AbstractPublicThreadHandler implements PublicThreadHandler {

    public Single<Thread> createPublicThreadWithName(final String name) {
        return createPublicThreadWithName(name, null, null);
    }

    public Single<Thread> createPublicThreadWithName(final String name, HashMap<String, String> meta) {
        return createPublicThreadWithName(name, null, meta);
    }

    public Single<Thread> createPublicThreadWithName(final String name, final String entityID) {
        return createPublicThreadWithName(name, entityID, null);
    }

    public Single<Thread> createPublicThreadWithName(final String name, final String entityID, HashMap<String, String> meta) {
        return createPublicThreadWithName(name, entityID, meta, null);
    }

}
