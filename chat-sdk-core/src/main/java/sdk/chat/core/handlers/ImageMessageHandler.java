package sdk.chat.core.handlers;

import java.io.File;

import sdk.chat.core.dao.Thread;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ImageMessageHandler extends MessageHandler {
    /**
     * Preparing an image text,
     * This is only the build part of the send from here the text will passed to "sendMessage" Method.
     * From there the text will be uploaded to the server if the upload fails the text will be deleted from the local db.
     * If the upload is successful we will update the text entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param imageFile is a file that contain the image. For now the file will be decoded to a Base64 image representation.
     * @param thread   thread that the text is sent to.
     */
    Completable sendMessageWithImage(File imageFile, Thread thread);

}
