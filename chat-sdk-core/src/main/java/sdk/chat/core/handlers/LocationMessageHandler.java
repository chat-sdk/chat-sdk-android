package sdk.chat.core.handlers;

import android.location.Location;

import io.reactivex.Completable;
import sdk.chat.core.dao.ThreadX;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */
public interface LocationMessageHandler extends MessageHandler {

    /**
     * Preparing a location text,
     * This is only the build part of the send from here the text will passed to "sendMessage" Method.
     * From there the text will be uploaded to the server if the upload fails the text will be deleted from the local db.
     * If the upload is successful we will update the text entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param filePath     is a String representation of a bitmap that contain the image of the location wanted.
     * @param location     is the Latitude and Longitude of the picked location.
     * @param thread       the thread that the text is sent to.
     */
    Completable sendMessageWithLocation(final String filePath, final Location location, final ThreadX thread);


}
