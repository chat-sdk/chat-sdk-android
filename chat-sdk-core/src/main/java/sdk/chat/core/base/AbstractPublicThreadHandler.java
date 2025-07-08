package sdk.chat.core.base;

import java.util.Map;

import io.reactivex.Single;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.handlers.PublicThreadHandler;

public abstract class AbstractPublicThreadHandler implements PublicThreadHandler {

    public Single<ThreadX> createPublicThreadWithName(final String name) {
        return createPublicThreadWithName(name, null, null);
    }

    public Single<ThreadX> createPublicThreadWithName(final String name, Map<String, Object> meta) {
        return createPublicThreadWithName(name, null, meta);
    }

    public Single<ThreadX> createPublicThreadWithName(final String name, final String entityID) {
        return createPublicThreadWithName(name, entityID, null);
    }

    public Single<ThreadX> createPublicThreadWithName(final String name, final String entityID, Map<String, Object> meta) {
        return createPublicThreadWithName(name, entityID, meta, null);
    }

}
