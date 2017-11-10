package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Thread;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface PublicThreadHandler {

    /**
     * Create a public group thread with a name. This can be used for group discussion
     */
    public Single<Thread> createPublicThreadWithName(final String name);
    public Single<Thread> createPublicThreadWithName(final String name, final String entityID);
}
