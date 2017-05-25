package tk.wanderingdevelopment.chatsdk.core.abstracthandlers;

import android.graphics.Bitmap;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.dao.core.BMessage;
import co.chatsdk.core.dao.core.BThread;
import co.chatsdk.core.dao.core.BThreadDao;
import co.chatsdk.core.dao.core.BUser;
import co.chatsdk.core.dao.core.DaoCore;
import co.chatsdk.core.dao.core.UserThreadLink;
import co.chatsdk.core.dao.core.sorter.ThreadsSorter;
import co.chatsdk.core.defines.Debug;


import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.utils.volley.ImageUtils;
import co.chatsdk.core.utils.volley.VolleyUtils;

import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.ChatError;
import com.google.android.gms.maps.model.LatLng;

import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.interfaces.ThreadsInterface;

/**
 * Created by KyleKrueger on 10.04.2017.
 */

public abstract class ThreadsManager implements ThreadsInterface {

    protected boolean DEBUG = Debug.ThreadsManager;

    public abstract Completable sendMessage(BMessage message);

    /**
     * Preparing a text message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * The message will be received before sending in the onMainFinished Callback with a Status that its in the sending process.
     * When the message is fully sent the status will be changed and the onItem callback will be invoked.
     * When done or when an error occurred the calling method will be notified.
     */
    public Completable  sendMessageWithText(String text, long threadId) {

        final BMessage message = new BMessage();
        message.setText(text);
        message.setThreadId(threadId);
        message.setType(BMessage.Type.TEXT);
        message.setSender(NetworkManager.shared().a.core.currentUserModel());
        message.setStatus(BMessage.Status.SENDING);
        message.setDelivered(BMessage.Delivered.No);

        DaoCore.createEntity(message);

        // Setting the temporary time of the message to be just after the last message that
        // was added to the thread.
        // Using this method we are avoiding time differences between the server time and the
        // device local time.
        Date date = message.getThread().getLastMessageAdded();
        if (date == null)
            date = new Date();

        message.setDate( new DateTime(date.getTime() + 1) );

        DaoCore.updateEntity(message);

        return implSendMessage(message);

    }

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
    public Observable<ImageUploadResult> sendMessageWithLocation(final String filePath, final LatLng location, long threadId) {

        final Deferred<BMessage, ChatError, BMessage> deferred = new DeferredObject<>();

        final BMessage message = new BMessage();
        message.setThreadId(threadId);
        message.setType(BMessage.Type.LOCATION);
        message.setStatus(BMessage.Status.SENDING);
        message.setDelivered(BMessage.Delivered.No);
        message.setSender(NetworkManager.shared().a.core.currentUserModel());
        message.setResourcesPath(filePath);

        DaoCore.createEntity(message);

        // Setting the temporary time of the message to be just after the last message that
        // was added to the thread.
        // Using this method we are avoiding time differences between the server time and the
        // device local time.
        Date date = message.getThread().getLastMessageAdded();
        if (date == null)
            date = new Date();

        message.setDate( new DateTime(date.getTime() + 1) );

        DaoCore.updateEntity(message);

        Bitmap image = ImageUtils.getCompressed(message.getResourcesPath());

        Bitmap thumbnail = ImageUtils.getCompressed(message.getResourcesPath(),
                Defines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE,
                Defines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);

        message.setImageDimensions(ImageUtils.getDimensionAsString(image));

        return NetworkManager.shared().a.upload.uploadImage(image, thumbnail).doOnNext(new Consumer<ImageUploadResult>() {
            @Override
            public void accept(ImageUploadResult result) throws Exception {
                if(result.isComplete()) {
                    // Add the LatLng data to the message and the image url and thumbnail url
                    message.setText(String.valueOf(location.latitude)
                            + Defines.DIVIDER
                            + String.valueOf(location.longitude)
                            + Defines.DIVIDER + result.imageURL
                            + Defines.DIVIDER + result.thumbnailURL
                            + Defines.DIVIDER + message.getImageDimensions());
                }
            }
        }).doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                // Sending the message, After it was uploaded to the server we can delte the file.
                implSendMessage(message).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        DaoCore.updateEntity(message);
                    }
                }).subscribe();

            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                new File(filePath).delete();
            }
        });

    }

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
    public Observable<ImageUploadResult> sendMessageWithImage(final String filePath, long threadId) {

        final Deferred<BMessage, ChatError, BMessage> deferred = new DeferredObject<>();

        final BMessage message = new BMessage();
        message.setThreadId(threadId);
        message.setType(BMessage.Type.IMAGE);
        message.setSender(NetworkManager.shared().a.core.currentUserModel());
        message.setStatus(BMessage.Status.SENDING);
        message.setDelivered(BMessage.Delivered.No);

        DaoCore.createEntity(message);

        // Setting the temporary time of the message to be just after the last message that
        // was added to the thread.
        // Using this method we are avoiding time differences between the server time and the
        // device local time.
        Date date = message.getThread().getLastMessageAdded();
        if (date == null)
            date = new Date();

        message.setDate(new DateTime(date.getTime() + 1));

        message.setResourcesPath(filePath);

        DaoCore.updateEntity(message);

        Bitmap image = ImageUtils.getCompressed(message.getResourcesPath());

        Bitmap thumbnail = ImageUtils.getCompressed(message.getResourcesPath(),
                Defines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE,
                Defines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);

        message.setImageDimensions(ImageUtils.getDimensionAsString(image));

        VolleyUtils.getBitmapCache().put(
                VolleyUtils.BitmapCache.getCacheKey(message.getResourcesPath()),
                thumbnail);

        return NetworkManager.shared().a.upload.uploadImage(image, thumbnail).doOnNext(new Consumer<ImageUploadResult>() {
            @Override
            public void accept(ImageUploadResult result) throws Exception {
                if(result.isComplete()) {
                    message.setText(result.imageURL + Defines.DIVIDER + result.thumbnailURL + Defines.DIVIDER + message.getImageDimensions());
                    DaoCore.updateEntity(message);
                }
            }
        }).doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                implSendMessage(message);
            }
        });

    }

    private Completable implSendMessage(final BMessage message) {
        return sendMessage(message).doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                message.setStatus(BMessage.Status.SENT);
                DaoCore.updateEntity(message);
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                message.setStatus(BMessage.Status.FAILED);
            }
        });
    }

    public abstract Single<List<BMessage>> loadMoreMessagesForThread(BThread thread);

    public int getUnreadMessagesAmount(boolean onePerThread){
        List<BThread> threads = BNetworkManager.getThreadsInterface().getThreads(ThreadType.Private);

        int count = 0;
        for (BThread t : threads)
        {
            if (onePerThread)
            {
                if(!t.isLastMessageWasRead())
                {
                    if (DEBUG) Timber.d("HasUnread, ThreadName: %s", t.displayName());
                    count++;
                }
            }
            else
            {
                count += t.getUnreadMessagesAmount();
            }
        }

        return count;
    }



    /**
     * Create thread for given users.
     * When the thread is added to the server the "onMainFinished" will be invoked,
     * If an error occurred the error object would not be null.
     * For each user that was succesfully added the "onItem" method will be called,
     * For any item adding failure the "onItemFailed will be called.
     * If the main task will fail the error object in the "onMainFinished" method will be called.
     */
    public abstract Single<BThread> createThreadWithUsers(String name, List<BUser> users);

    public Single<BThread> createThreadWithUsers(String name, BUser... users) {
        return createThreadWithUsers(name, Arrays.asList(users));
    }

    public abstract Completable deleteThreadWithEntityID(String entityID);

    public Completable deleteThread(BThread thread){
        return deleteThreadWithEntityID(thread.getEntityID());
    }

    // TODO: Why is this never used?
//
//    public List<BThread> threadsWithType(int type) {
//
//        // Get the thread list ordered desc by the last message added date.
//        List<BThread> allThreads;
//        BUser currentUser = NetworkManager.shared().a.core.currentUserModel();
//        BUser threadCreator;
//
//        // Get all the threads - if they are private we get the thread links
//        if (ThreadType.isPrivate(type)) {
//            if (DEBUG) Timber.d("threadItemsWithType, loading private.");
//            allThreads = getThreads(ThreadType.Private);
//        }
//        else {
//            allThreads = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, type);
//        }
//
//        List<BThread> threads = new ArrayList<BThread>();
//
//        if (type == BThread.Type.Public)
//        {
//            for (BThread thread : allThreads)
//                if (thread.getTypeSafely() == BThread.Type.Public)
//                    threads.add(thread);
//        }
//        else {
//            for (BThread thread : allThreads) {
//                if (DEBUG) Timber.i("threadItemsWithType, ThreadID: %s, Deleted: %s", thread.getId(), thread.getDeleted());
//
//                if (thread.isDeleted())
//                    continue;
//
//                if (thread.getMessagesWithOrder(DaoCore.ORDER_DESC).size() > 0)
//                {
//                    threads.add(thread);
//                    continue;
//                }
//
//                if (StringUtils.isNotBlank(thread.getCreatorEntityId()) && thread.getEntityID().equals(currentUser.getEntityID()))
//                {
//                    threads.add(thread);
//                }
//                else
//                {
//                    threadCreator = thread.getCreator();
//                    if (threadCreator != null )
//                    {
//                        if (threadCreator.equals(currentUser) && thread.hasUser(currentUser))
//                        {
//                            threads.add(thread);
//                        }
//                    }
//                }
//            }
//        }
//
//        if (DEBUG) Timber.d("threadsWithType, Type: %s, Found on db: %s, Threads List Size: %s" + type, threads.size(), threads.size());
//
//        Collections.sort(threads, new ThreadsSorter());
//
//        return threads;
//    }

    /**
     * Add given users list to the given thread.
     */
    public abstract Flowable<BUser> addUsersToThread(BThread thread, List<BUser> users);

    /**
     * Add given users list to the given thread.
     */
    public Flowable<BUser> addUsersToThread(BThread thread, BUser... users) {
        return addUsersToThread(thread, Arrays.asList(users));
    }

    /**
     * Remove given users list to the given thread.
     */
    public abstract Flowable<BUser> removeUsersFromThread(BThread thread, List<BUser> users);

    /**
     * Remove given users list to the given thread.
     */
    public Flowable<BUser> removeUsersFromThread(BThread thread, BUser... users) {
        return removeUsersFromThread(thread, Arrays.asList(users));
    }

    public abstract Completable pushThread(BThread thread);

//    public List<BThread> getThreads(){
//        return getThreads(-1);
//    }

    @Deprecated
    public List<BThread> getThreads(int type){
        return getThreads(type, false);
    }

    /**
     * Method updated by Kyle
     *
     * @param type the type of the threads to get, Pass -1 to get all types.
     * @param allowDeleted if true deleted threads will be included in the result list
     * @return a list with all the threads.
     ** */
    // TODO: This gets all threads - not just for the current user. See the get threads with type method
    @Deprecated
    public List<BThread> getThreads(int type, boolean allowDeleted){
        if(ThreadType.isPublic(type)) {
            return DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, type);
        }

        // Freshen up the data by calling reset before getting the list
        List<UserThreadLink> links = DaoCore.fetchEntitiesOfClass(UserThreadLink.class);

        // In case the list is empty
        if (links == null) return null;

        List<BThread> threads = new ArrayList<>();
        BUser currentUser = NetworkManager.shared().a.core.currentUserModel();

        // Pull the threads out of the link object . . . if only gDao supported manyToMany . . .
        for (UserThreadLink link : links ){
            BThread thread = link.getBThread();
            BUser user = link.getBUser();

            if(user == null || !user.equals(currentUser)) {
                continue;
            }

            if (thread == null || (thread.isDeleted() && !allowDeleted) || !thread.typeIs(type)) {
                continue;
            }

            threads.add(link.getBThread());
        }

        // Sort the threads list before returning
        Collections.sort(threads, new ThreadsSorter());

        return threads;
    }

}
