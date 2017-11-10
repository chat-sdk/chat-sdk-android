package co.chatsdk.core.handlers;

import com.google.android.gms.maps.model.LatLng;

import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Observable;
import co.chatsdk.core.dao.Thread;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */
public interface LocationMessageHandler {

    /**
     * Preparing a location message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param filePath     is a String representation of a bitmap that contain the image of the location wanted.
     * @param location     is the Latitude and Longitude of the picked location.
     * @param thread       the thread that the message is sent to.
     */
    Observable<MessageSendProgress> sendMessageWithLocation(final String filePath, final LatLng location, final Thread thread);
}
