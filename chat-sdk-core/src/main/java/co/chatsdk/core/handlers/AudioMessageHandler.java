package co.chatsdk.core.handlers;

import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.dao.Thread;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AudioMessageHandler extends MessageHandler {

    /**
     * Send an audio text
     */
    Completable sendMessage (final Recording recording, final Thread thread);

}
