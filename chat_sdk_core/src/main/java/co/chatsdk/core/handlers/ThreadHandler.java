package co.chatsdk.core.handlers;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

public interface ThreadHandler {

    /**
     * Create thread for given users.
     * When the thread is added to the server the "onMainFinished" will be invoked,
     * If an error occurred the error object would not be null.
     * For each user that was succesfully added the "onItem" method will be called,
     * For any item adding failure the "onItemFailed will be called.
     * If the main task will fail the error object in the "onMainFinished" method will be called.
     */
    Single<BThread> createThread(String name, List<BUser> users);
    Single<BThread> createThread(List<BUser> users);
    Single<BThread> createThread(String name, BUser... users);

    /**
     * Remove users from a thread
     */
    Flowable<BUser> removeUsersFromThread(BThread thread, List<BUser> users);
    Flowable<BUser> removeUsersFromThread(BThread thread, BUser... users);
    /**
     * Add users to a thread
     */
    Flowable<BUser> addUsersToThread(BThread thread, List<BUser> users);
    Flowable<BUser> addUsersToThread(BThread thread, BUser... users);
    /**
     * Lazy loading of messages this method will load
     * that are not already in memory
     */
    Single<List<BMessage>> loadMoreMessagesForThread(BThread thread);

    /**
     * This method deletes an existing thread. It deletes the thread from memory
     * and removes the user from the thread so the user no longer recieves notifications
     * from the thread
     */
    Completable deleteThread(BThread thread);
    Completable leaveThread (BThread thread);
    Completable joinThread (BThread thread);

    /**
     * Send different types of message to a particular thread
     */
    Completable sendMessageWithText(String text, BThread thread);
    Observable<ImageUploadResult> sendMessageWithLocation(String filePath, LatLng location, BThread thread);
    Observable<ImageUploadResult> sendMessageWithImage(String filePath, BThread thread);
    /**
     * Send a message object
     */
    Completable sendMessage(BMessage message);

    int getUnreadMessagesAmount(boolean onePerThread);

    // TODO: Consider making this a PThread for consistency
    /**
     * Get the messages for a particular thread
     */
    //List<BMessage> messagesForThread (String threadID, boolean ascending);

    /**
     * Get a list of all threads
     */
    List<BThread> getThreads (int type, boolean allowDeleted);
    List<BThread> getThreads (int type);

    void sendLocalSystemMessage(String text, BThread thread);
    void sendLocalSystemMessage(String text, CoreHandler.bSystemMessageType type, BThread thread);

    Completable pushThread(BThread thread);
}
