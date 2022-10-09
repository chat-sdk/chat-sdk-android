package sdk.chat.core.handlers;

import android.app.Activity;

import java.io.File;

import sdk.chat.core.dao.Thread;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface VideoMessageHandler extends MessageHandler {

    /**
     * Send a video text
     */
    Completable sendMessageWithVideo(File videoFile, Thread thread);

    void startPlayVideoActivity(Activity activity, String path);

}
