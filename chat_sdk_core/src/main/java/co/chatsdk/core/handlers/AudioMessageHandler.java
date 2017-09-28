package co.chatsdk.core.handlers;

import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AudioMessageHandler {

    /**
     * @brief Send an audio message
     */
    Completable sendMessage (double seconds, String threadEntityID);
}
