package tk.wanderingdevelopment.chatsdk.core.interfaces;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.BPushHandler;
import com.braunster.chatsdk.interfaces.BUploadHandler;
import com.braunster.chatsdk.object.BError;
import com.google.android.gms.maps.model.LatLng;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;

import java.util.List;

/**
 * Created by KyleKrueger on 10.04.2017.
 */

public interface ThreadsInterface {


    List<BThread> getThreads();

    List<BThread> getThreads(int type);

    /**
     * Method updated by Kyle
     *
     * @param type the type of the threads to get, Pass -1 to get all types.
     * @param allowDeleted if true deleted threads will be included in the result list
     * @return a list with all the threads.
     ** */
    List<BThread> getThreads(int type, boolean allowDeleted);


    Promise<BMessage, BError, BMessage> sendMessage(BMessage messages);

    /**
     * Preparing a text message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * The message will be received before sending in the onMainFinished Callback with a Status that its in the sending process.
     * When the message is fully sent the status will be changed and the onItem callback will be invoked.
     * When done or when an error occurred the calling method will be notified.
     */
    Promise<BMessage, BError, BMessage>  sendMessageWithText(String text, long threadId);

    /**
     * Preparing a location message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param filePath     is a String representation of a bitmap that contain the image of the location wanted.
     * @param location       is the Latitude and Longitude of the picked location.
     * @param threadId the id of the thread that the message is sent to.
     */
     Promise<BMessage, BError, BMessage> sendMessageWithLocation(final String filePath, final LatLng location, long threadId);

    /**
     * Preparing an image message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param filePath is a file that contain the image. For now the file will be decoded to a Base64 image representation.
     * @param threadId the id of the thread that the message is sent to.
     */
    Promise<BMessage, BError, BMessage>  sendMessageWithImage(final String filePath, long threadId);

    Deferred<BMessage, BError, BMessage> sendMessage(final BMessage message, final Deferred<BMessage, BError, BMessage> deferred);

    Promise<List<BMessage>, Void, Void> loadMoreMessagesForThread(BThread thread);

    int getUnreadMessagesAmount(boolean onePerThread);



    /**
     * Create thread for given users.
     * When the thread is added to the server the "onMainFinished" will be invoked,
     * If an error occurred the error object would not be null.
     * For each user that was succesfully added the "onItem" method will be called,
     * For any item adding failure the "onItemFailed will be called.
     * If the main task will fail the error object in the "onMainFinished" method will be called.
     */
    Promise<BThread, BError, Void> createThreadWithUsers(String name, List<BUser> users);

    Promise<BThread, BError, Void> createThreadWithUsers(String name, BUser... users);

    Promise<BThread, BError, Void> createPublicThreadWithName(String name);


    Promise<Void, BError, Void> deleteThreadWithEntityID(String entityID);

    Promise<Void, BError, Void> deleteThread(BThread thread);


    /**
     * Add given users list to the given thread.
     */
    Promise<BThread, BError, Void> addUsersToThread(BThread thread, List<BUser> users);

    /**
     * Add given users list to the given thread.
     */
    Promise<BThread, BError, Void> addUsersToThread(BThread thread, BUser... users);
    /**
     * Remove given users list to the given thread.
     */
    Promise<BThread, BError, Void> removeUsersFromThread(BThread thread, List<BUser> users);

    /**
     * Remove given users list to the given thread.
     */
    Promise<BThread, BError, Void> removeUsersFromThread(BThread thread, BUser... users);


    Promise<BThread, BError, Void> pushThread(BThread thread);



}
