package co.chatsdk.core.handlers;

import java.io.File;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Completable;
import io.reactivex.Observable;
import co.chatsdk.core.dao.Thread;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ImageMessageHandler extends MessageHandler {
    /**
     * Preparing an image message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param imageFile is a file that contain the image. For now the file will be decoded to a Base64 image representation.
     * @param thread   thread that the message is sent to.
     */
    Completable sendMessageWithImage(File imageFile, Thread thread);

}
