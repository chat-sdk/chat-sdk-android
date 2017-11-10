package co.chatsdk.core.handlers;

import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.CustomMessageHandler;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AudioMessageHandler extends CustomMessageHandler {

    /**
     * Send an audio message
     */
    Observable<MessageSendProgress> sendMessage (final Recording recording, final Thread thread);

}
