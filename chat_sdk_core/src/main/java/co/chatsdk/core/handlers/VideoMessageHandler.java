package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.CustomMessageHandler;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface VideoMessageHandler extends CustomMessageHandler {

    /**
     * Send a video message
     */
    Observable<MessageSendProgress> sendMessageWithVideo(final String videoPath, final Thread thread);

}
