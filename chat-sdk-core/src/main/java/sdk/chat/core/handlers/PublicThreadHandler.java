package sdk.chat.core.handlers;

import java.util.Map;

import io.reactivex.Single;
import sdk.chat.core.dao.ThreadX;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface PublicThreadHandler {

    /**
     * Create a public group thread with a name. This can be used for group discussion
     */
    Single<ThreadX> createPublicThreadWithName(final String name);
    Single<ThreadX> createPublicThreadWithName(final String name, final String entityID);
    Single<ThreadX> createPublicThreadWithName(final String name, Map<String, Object> meta);
    Single<ThreadX> createPublicThreadWithName(final String name, final String entityID, Map<String, Object> meta);
    Single<ThreadX> createPublicThreadWithName(final String name, final String entityID, Map<String, Object> meta, String imageURL);
}
