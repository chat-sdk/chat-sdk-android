package co.chatsdk.core.handlers;

import java.io.File;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface VideoMessageHandler extends MessageHandler {

    /**
     * Send a video message
     */
    Completable sendMessageWithVideo(File videoFile, Thread thread);

}
