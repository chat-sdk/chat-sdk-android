package tk.wanderingdevelopment.chatsdk.core.interfaces;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import co.chatsdk.core.dao.core.BMessage;
import co.chatsdk.core.dao.core.BThread;
import co.chatsdk.core.dao.core.BUser;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by KyleKrueger on 10.04.2017.
 */

public interface ThreadsInterface {


    //List<BThread> getThreads();

    List<BThread> getThreads(int type);

    /**
     * Method updated by Kyle
     *
     * @param type the type of the threads to get, Pass -1 to get all types.
     * @param allowDeleted if true deleted threads will be included in the result list
     * @return a list with all the threads.
     ** */
    List<BThread> getThreads(int type, boolean allowDeleted);


    Completable sendMessage(BMessage messages);

    /**
     * Preparing a text message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * The message will be received before sending in the onMainFinished Callback with a Status that its in the sending process.
     * When the message is fully sent the status will be changed and the onItem callback will be invoked.
     * When done or when an error occurred the calling method will be notified.
     */
    Completable sendMessageWithText(String text, long threadId);

    /**
     * Preparing a location message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param filePath     is a String representation of a bitmap that contain the image of the location wanted.
     * @param location       is the Latitude and Longitude of the picked location.
     * @param threadID the id of the thread that the message is sent to.
     */
     Observable<ImageUploadResult> sendMessageWithLocation(final String filePath, final LatLng location, long threadID);

    /**
     * Preparing an image message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param filePath is a file that contain the image. For now the file will be decoded to a Base64 image representation.
     * @param threadID the id of the thread that the message is sent to.
     */
    Observable<ImageUploadResult> sendMessageWithImage(final String filePath, long threadID);

    //Deferred<BMessage, ChatError, BMessage> sendMessage(final BMessage message, final Deferred<BMessage, ChatError, BMessage> deferred);

    Single<List<BMessage>> loadMoreMessagesForThread(BThread thread);

    int getUnreadMessagesAmount(boolean onePerThread);



    /**
     * Create thread for given users.
     * When the thread is added to the server the "onMainFinished" will be invoked,
     * If an error occurred the error object would not be null.
     * For each user that was succesfully added the "onItem" method will be called,
     * For any item adding failure the "onItemFailed will be called.
     * If the main task will fail the error object in the "onMainFinished" method will be called.
     */
    Single<BThread> createThreadWithUsers(String name, List<BUser> users);

    Single<BThread> createThreadWithUsers(String name, BUser... users);


    Completable deleteThreadWithEntityID(String entityID);

    Completable deleteThread(BThread thread);


    /**
     * Add given users list to the given thread.
     */
    Flowable<BUser> addUsersToThread(BThread thread, List<BUser> users);

    /**
     * Add given users list to the given thread.
     */
    Flowable<BUser> addUsersToThread(BThread thread, BUser... users);
    /**
     * Remove given users list to the given thread.
     */
    Flowable<BUser> removeUsersFromThread(BThread thread, List<BUser> users);

    /**
     * Remove given users list to the given thread.
     */
    Flowable<BUser> removeUsersFromThread(BThread thread, BUser... users);


    Completable pushThread(BThread thread);



}
