package co.chatsdk.core.base;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BThreadDao;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.UserThreadLink;
import co.chatsdk.core.dao.sorter.ThreadsSorter;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.ImageUploadResult;
import co.chatsdk.core.utils.volley.ImageUtils;
import co.chatsdk.core.utils.volley.VolleyUtils;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public abstract class AbstractThreadHandler implements ThreadHandler {

    /**
     * Preparing a text message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * The message will be received before sending in the onMainFinished Callback with a Status that its in the sending process.
     * When the message is fully sent the status will be changed and the onItem callback will be invoked.
     * When done or when an error occurred the calling method will be notified.
     */
    public Completable sendMessageWithText(String text, BThread thread) {

        final BMessage message = new BMessage();
        message.setText(text);
        //message.setThreadId(threadId);
        message.setThread(thread);
        message.setType(BMessage.Type.TEXT);
        message.setSender(NM.currentUser());
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
     * @param location     is the Latitude and Longitude of the picked location.
     * @param thread       the thread that the message is sent to.
     */
    public Observable<ImageUploadResult> sendMessageWithLocation(final String filePath, final LatLng location, BThread thread) {

        final BMessage message = new BMessage();
        message.setThread(thread);
        message.setType(BMessage.Type.LOCATION);
        message.setStatus(BMessage.Status.SENDING);
        message.setDelivered(BMessage.Delivered.No);
        message.setSender(NM.currentUser());
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

        return NM.upload().uploadImage(image, thumbnail).doOnNext(new Consumer<ImageUploadResult>() {
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
     * @param thread   thread that the message is sent to.
     */
    public Observable<ImageUploadResult> sendMessageWithImage(final String filePath, BThread thread) {

        final BMessage message = new BMessage();
        message.setThread(thread);
        message.setType(BMessage.Type.IMAGE);
        message.setSender(NM.currentUser());
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

        return NM.upload().uploadImage(image, thumbnail).doOnNext(new Consumer<ImageUploadResult>() {
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

    public int getUnreadMessagesAmount(boolean onePerThread){
        List<BThread> threads = getThreads(ThreadType.Private, false);

        int count = 0;
        for (BThread t : threads)
        {
            if (onePerThread)
            {
                if(!t.isLastMessageWasRead())
                {
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

    public Single<BThread> createThread(String name, BUser... users) {
        return createThread(name, Arrays.asList(users));
    }

    public Completable addUsersToThread(BThread thread, BUser... users) {
        return addUsersToThread(thread, Arrays.asList(users));
    }

    public Completable removeUsersFromThread(BThread thread, BUser... users) {
        return removeUsersFromThread(thread, Arrays.asList(users));
    }

    public List<BThread> getThreads(int type) {
        return getThreads(type, false);
    }

    public List<BThread> getThreads(int type, boolean allowDeleted){
        if(ThreadType.isPublic(type)) {
            return DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, type);
        }

        // Freshen up the data by calling reset before getting the list
        List<UserThreadLink> links = DaoCore.fetchEntitiesOfClass(UserThreadLink.class);

        // In case the list is empty
        if (links == null) return null;

        List<BThread> threads = new ArrayList<>();
        BUser currentUser = NM.currentUser();

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

    public void sendLocalSystemMessage(String text, BThread thread) {

    }

    public void sendLocalSystemMessage(String text, CoreHandler.bSystemMessageType type, BThread thread) {

    }


}
