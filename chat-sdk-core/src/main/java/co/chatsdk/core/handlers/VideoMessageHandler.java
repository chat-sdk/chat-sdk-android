package co.chatsdk.core.handlers;

import java.io.File;

import co.chatsdk.core.dao.Thread;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface VideoMessageHandler extends MessageHandler {

    /**
     * Send a video text
     */
    Completable sendMessageWithVideo(File videoFile, Thread thread);

}
