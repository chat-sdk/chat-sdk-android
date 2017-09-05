package co.chatsdk.core.base;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import co.chatsdk.core.NM;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.UserThreadLink;
import co.chatsdk.core.dao.UserThreadLinkDao;
import co.chatsdk.core.dao.sorter.ThreadsSorter;
import co.chatsdk.core.defines.FirebaseDefines;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.MessageUploadResult;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.core.utils.ImageUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public abstract class AbstractThreadHandler implements ThreadHandler {

    public Single<List<Message>> loadMoreMessagesForThread(final Message fromMessage, final Thread thread) {
        return Single.create(new SingleOnSubscribe<List<Message>>() {
            @Override
            public void subscribe(final SingleEmitter<List<Message>> e) throws Exception {

                Date messageDate = fromMessage != null ? fromMessage.getDate().toDate() : new Date();

                // First try to load the messages from the database
                List<Message> list = StorageManager.shared().fetchMessagesForThreadWithID(thread.getId(), FirebaseDefines.NumberOfMessagesPerBatch + 1, messageDate);
                e.onSuccess(list);
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * Preparing a text message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * The message will be received before sending in the onMainFinished Callback with a Status that its in the sending process.
     * When the message is fully sent the status will be changed and the onItem callback will be invoked.
     * When done or when an error occurred the calling method will be notified.
     */
    public Completable sendMessageWithText(final String text, final Thread thread) {
        return Single.create(new SingleOnSubscribe<Message>() {
            @Override
            public void subscribe(SingleEmitter<Message> e) throws Exception {
                final Message message = newMessage();

//                java.lang.Thread.currentThread();

                message.setTextString(text);
                message.setType(Message.Type.TEXT);

                thread.addMessage(message);

                e.onSuccess(message);
            }
        }).flatMapCompletable(new Function<Message, Completable>() {
            @Override
            public Completable apply(final Message message) throws Exception {
            return implSendMessage(message).doOnComplete(new Action() {
                @Override
                public void run() throws Exception {
                    DaoCore.updateEntity(message);
                }
            });
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    private Message newMessage () {
        Message message = new Message();
        DaoCore.createEntity(message);
        message.setSender(NM.currentUser());
        message.setStatus(Message.Status.SENDING);
        message.setDelivered(Message.Delivered.No);
        message.setDate(new DateTime(System.currentTimeMillis()));
        message.setEntityID(UUID.randomUUID().toString());
        return message;
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
    public Completable sendMessageWithLocation(final String filePath, final LatLng location, final Thread thread) {
        return Single.create(new SingleOnSubscribe<Message>() {
            @Override
            public void subscribe(final SingleEmitter<Message> e) throws Exception {

                final Message message = newMessage();

                message.setType(Message.Type.LOCATION);
                message.setResourcesPath(filePath);

                thread.addMessage(message);

                int maxSize = Defines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE;
                String imageURL = GoogleUtils.getMapImageURL(location, maxSize, maxSize);

                message.setImageDimensions(ImageUtils.getDimensionAsString(maxSize, maxSize));

                // Add the LatLng data to the message and the image url and thumbnail url
                // TODO: Depricated
                message.setTextString(String.valueOf(location.latitude)
                        + Defines.DIVIDER
                        + String.valueOf(location.longitude)
                        + Defines.DIVIDER + imageURL
                        + Defines.DIVIDER + imageURL
                        + Defines.DIVIDER + message.getImageDimensions());

                message.setValueForKey(location.longitude, Keys.MessageLongitude);
                message.setValueForKey(location.latitude, Keys.MessageLatitude);
                message.setValueForKey(maxSize, Keys.MessageImageWidth);
                message.setValueForKey(maxSize, Keys.MessageImageHeight);
                message.setValueForKey(imageURL, Keys.MessageImageURL);
                message.setValueForKey(imageURL, Keys.MessageThumbnailURL);

                e.onSuccess(message);
            }
        }).flatMapCompletable(new Function<Message, Completable>() {
            @Override
            public Completable apply(final Message message) throws Exception {
                return implSendMessage(message).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                    }
                });
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
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
    public Observable<MessageUploadResult> sendMessageWithImage(final String filePath, final Thread thread) {
        return Observable.create(new ObservableOnSubscribe<MessageUploadResult>() {
            @Override
            public void subscribe(final ObservableEmitter<MessageUploadResult> e) throws Exception {

                final Message message = newMessage();

                message.setType(Message.Type.IMAGE);
                message.setResourcesPath(filePath);

                thread.addMessage(message);

                final Bitmap image = ImageUtils.getCompressed(message.getResourcesPath());

                message.setImageDimensions(ImageUtils.getDimensionAsString(image));

                // First pass back an empty result so that we add the cell to the table view
                MessageUploadResult r = new MessageUploadResult("", "");
                r.message = message;
                e.onNext(r);

                NM.upload().uploadImage(image).doOnNext(new Consumer<MessageUploadResult>() {
                    @Override
                    public void accept(MessageUploadResult result) throws Exception {

                        if(result.isComplete())  {

                            message.setTextString(result.imageURL + Defines.DIVIDER + result.thumbnailURL + Defines.DIVIDER + message.getImageDimensions());

                            message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
                            message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);
                            message.setValueForKey(result.imageURL, Keys.MessageImageURL);
                            message.setValueForKey(result.thumbnailURL, Keys.MessageThumbnailURL);

                        }

                        result.message = message;
                        e.onNext(result);

                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        implSendMessage(message).subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                e.onComplete();
                            }
                        });
                    }
                }).subscribe();
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());

    }

    private Completable implSendMessage(final Message message) {
        return Single.create(new SingleOnSubscribe<Message>() {
            @Override
            public void subscribe(SingleEmitter<Message> e) throws Exception {
                // Set default message properties
                e.onSuccess(message);
            }
        }).flatMapCompletable(new Function<Message, Completable>() {
            @Override
            public Completable apply(Message message) throws Exception {
                return sendMessage(message);
            }
        }).doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                message.setStatus(Message.Status.SENT);
                DaoCore.updateEntity(message);
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                message.setStatus(Message.Status.FAILED);
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public int getUnreadMessagesAmount(boolean onePerThread){
        List<Thread> threads = getThreads(ThreadType.Private, false);

        int count = 0;
        for (Thread t : threads)
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

    public Single<Thread> createThread(String name, User... users) {
        return createThread(name, Arrays.asList(users));
    }

    public Single<Thread> createThread(List<User> users) {
        return createThread(null, users);
    }

    public Completable addUsersToThread(Thread thread, User... users) {
        return addUsersToThread(thread, Arrays.asList(users));
    }

    public Completable removeUsersFromThread(Thread thread, User... users) {
        return removeUsersFromThread(thread, Arrays.asList(users));
    }

    public List<Thread> getThreads(int type) {
        return getThreads(type, false);
    }

    public List<Thread> getThreads(int type, boolean allowDeleted){

        if(ThreadType.isPublic(type)) {
            return StorageManager.shared().fetchThreadsWithType(ThreadType.PublicGroup);
        }

        // We may access this method post authentication
        if(NM.currentUser() == null) {
            return new ArrayList<>();
        }

        List<UserThreadLink> links = DaoCore.fetchEntitiesWithProperty(UserThreadLink.class, UserThreadLinkDao.Properties.UserId, NM.currentUser().getId());

        List<Thread> threads = new ArrayList<>();

        // Pull the threads out of the link object . . . if only gDao supported manyToMany . . .
        for (UserThreadLink link : links) {
            if(link.getThread().typeIs(type)) {
                threads.add(link.getThread());
            }
        }

        // Sort the threads list before returning
        Collections.sort(threads, new ThreadsSorter());

        return threads;
    }

    public void sendLocalSystemMessage(String text, Thread thread) {

    }

    public void sendLocalSystemMessage(String text, CoreHandler.bSystemMessageType type, Thread thread) {

    }


}
