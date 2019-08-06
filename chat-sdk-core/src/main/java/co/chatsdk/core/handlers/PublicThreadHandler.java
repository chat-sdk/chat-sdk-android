package co.chatsdk.core.handlers;

import java.util.HashMap;

import co.chatsdk.core.dao.Thread;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface PublicThreadHandler {

    /**
     * Create a public group thread with a name. This can be used for group discussion
     */
    Single<Thread> createPublicThreadWithName(final String name);
    Single<Thread> createPublicThreadWithName(final String name, final String entityID);
    Single<Thread> createPublicThreadWithName(final String name, HashMap<String, String> meta);
    Single<Thread> createPublicThreadWithName(final String name, final String entityID, HashMap<String, String> meta);
    Single<Thread> createPublicThreadWithName(final String name, final String entityID, HashMap<String, String> meta, String imageURL);
}
