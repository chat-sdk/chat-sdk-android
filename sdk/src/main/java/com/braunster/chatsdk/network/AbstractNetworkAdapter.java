package com.braunster.chatsdk.network;


import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.util.TimingLogger;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.sorter.ThreadsItemSorter;
import com.braunster.chatsdk.Utils.sorter.ThreadsSorter;
import com.braunster.chatsdk.adapter.ThreadsListAdapter;
import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BMessageEntity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListener;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.parse.ParseUtils;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLoginCompletionHandler;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.LOCATION;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.TEXT;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Anonymous;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Facebook;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Google;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Password;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Register;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Twitter;
import static com.braunster.chatsdk.network.BDefines.Prefs.AuthenticationID;

/**
 * Created by braunster on 23/06/14.
 */
public abstract class AbstractNetworkAdapter {

    //Note maybe catch the error if occur in the send message of some kind and try to fix them here before returning the error to the caller.
    private static final String TAG = AbstractNetworkAdapter.class.getSimpleName();
    private static final boolean DEBUG = Debug.AbstractNetworkAdapter;

    private boolean authenticated = false;

    public boolean accountTypeEnabled(int type) {
        switch (type) {
            case Anonymous:
                return BDefines.AnonymuosLoginEnabled;

            case Facebook:
                return !BDefines.APIs.FacebookAppId.equals("");

            case Google:
                return !BDefines.APIs.GoogleAppId.equals("");

            case Password:
            case Register:
                return true;

            case Twitter:
                return !BDefines.APIs.TwitterConsumerKey.equals("");

            default:
                return false;
        }
    }

    // Note done!
    public abstract void authenticateWithMap(Map<String, Object> details, CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object> listener);

    /**
     * Due to the fact that the error can contain a FirebaseSimpleLoginError obj if the auth failed,
     * Or it can contain FirebaseError if after the auth something failed.
     * The return type of the listener must be Object and need to be cast.
     */
    public abstract void checkUserAuthenticatedWithCallback(AuthListener listener);

    public abstract void pushUserWithCallback(CompletionListener listener);

    public abstract void logout();

    //TODO make an object that obtain the error message. need to see some errors before it.
    public abstract void getUserFacebookFriendsToAppWithComplition(CompletionListenerWithData<List<BUser>> listener);

    public abstract void getUserFacebookFriendsWithCallback(CompletionListenerWithData listener);

    public abstract BUser currentUser();

    /*NOTE this was added due to the android system can kill the app while the app is in the background.
    *       After the app is killed the online status will be false,
    *       The app will check this status when resumed and if false the app will re-auth the user in the background.*/
    /*** Send a request to the server to get the online status of the user. */
    public abstract void isOnline(final CompletionListenerWithData<Boolean> listener);

    /** Send a password change request to the server.*/
    public abstract void changePassword(String email, String oldPassword, String newPassword, SimpleLoginCompletionHandler simpleLoginCompletionHandler);

    /** Send a reset email request to the server.*/
    public abstract void sendPasswordResetMail(String email, SimpleLoginCompletionHandler simpleLoginCompletionHandler);

    /*######################################################################################################*/
   /*Followers*/
    public abstract void getFollowers(String entityId, final RepetitiveCompletionListener<BUser> listener);

    public abstract void getFollows(String entityId, final RepetitiveCompletionListener<BUser> listener);

    public abstract void followUser(BUser userToFollow, CompletionListener listener);

    public abstract void unFollowUser(BUser userToUnfollow, CompletionListener listener);
/*######################################################################################################*/
    /*Messages*/
    /**
     * Preparing a text message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     */
    public void sendMessageWithText(String text, long threadEntityId, final CompletionListenerWithData<BMessage> listener) {
        if (DEBUG) Log.v(TAG, "sendMessageWithText");
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
        final BMessage message = new BMessage();
        message.setText(text);
        message.setOwnerThread(threadEntityId);
        message.setType(TEXT);
        message.setDate(new Date());
        message.setBUserSender(currentUser());
        final BMessage bMessage = DaoCore.createEntity(message);
        sendMessage(bMessage, new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage bMessage) {
                if (bMessage == null)
                    if (DEBUG) Log.e(TAG, "Message is null");

                DaoCore.updateEntity(bMessage);
                listener.onDone(bMessage);
            }

            @Override
            public void onDoneWithError(BError error) {
                DaoCore.deleteEntity(bMessage);
                listener.onDoneWithError(error);
            }
        });
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
    public void sendMessageWithText(String text, long threadEntityId, final RepetitiveCompletionListenerWithMainTaskAndError<BMessage, BMessage, BError> listener) {
        if (DEBUG) Log.v(TAG, "sendMessageWithText");
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
        final BMessage message = new BMessage();
        message.setText(text);
        message.setOwnerThread(threadEntityId);
        message.setType(TEXT);
        message.setDate(new Date());
        message.setBUserSender(currentUser());
        message.setStatus(BMessageEntity.Status.SENDING);
        final BMessage bMessage = DaoCore.createEntity(message);

        if (listener != null)
            listener.onMainFinised(bMessage, null);

        sendMessage(bMessage, new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage bMessage) {
                if (bMessage == null)
                {
                    if (DEBUG) Log.e(TAG, "Message is null");
                    return;
                }

                bMessage.setStatus(BMessageEntity.Status.SENT);
                bMessage = DaoCore.updateEntity(bMessage);

                if (listener != null)
                {
                    listener.onItem(bMessage);
                    listener.onDone();
                }
            }

            @Override
            public void onDoneWithError(BError error) {
                bMessage.setStatus(BMessageEntity.Status.SENT_FAILED);
                DaoCore.updateEntity(bMessage);

                if (listener != null) {
                    listener.onItemError(bMessage, error);
                }
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
     * @param image          is a file that contain the image. For now the file will be decoded to a Base64 image representation.
     * @param threadEntityId the id of the thread that the message is sent to.
     */
    public void sendMessageWithImage(File image, long threadEntityId, final CompletionListenerWithData<BMessage> listener) {
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/

        // http://stackoverflow.com/questions/13119306/base64-image-encoding-using-java

        final BMessage message = new BMessage();
        message.setOwnerThread(threadEntityId);
        message.setType(IMAGE);
        message.setDate(new Date());
        message.setBUserSender(currentUser());

        try {
            message.setText(Base64.encodeToString(FileUtils.readFileToByteArray(image), Base64.DEFAULT));
        } catch (IOException e) {
            e.printStackTrace();
            if (DEBUG) Log.e(TAG, "Error encoding file");
            listener.onDoneWithError(BError.getExceptionError(e, "Unable to encode file"));
            return;
        }

        DaoCore.createEntity(message);
        sendMessage(message, new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage bMessage) {
                if (DEBUG)
                    Log.v(TAG, "sendMessageWithImage, onDone. Message ID: " + bMessage.getEntityID());
                DaoCore.updateEntity(bMessage);
                listener.onDone(bMessage);
            }

            @Override
            public void onDoneWithError(BError error) {
                DaoCore.deleteEntity(message);
                listener.onDoneWithError(error);
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
     * @param threadEntityId the id of the thread that the message is sent to.
     */
    public void sendMessageWithImage(String filePath, long threadEntityId, final CompletionListenerWithData<BMessage> listener) {
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/

        // http://stackoverflow.com/questions/13119306/base64-image-encoding-using-java

        final BMessage message = new BMessage();
        message.setOwnerThread(threadEntityId);
        message.setType(IMAGE);
        message.setDate(new Date());
        message.setBUserSender(currentUser());

        ParseUtils.saveImageFileToParseWithThumbnail(filePath, BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE, new ParseUtils.MultiSaveCompletedListener() {
            @Override
            public void onSaved(ParseException exception, String... url) {
                if (exception == null) {
                    message.setText(url[0] + BDefines.DIVIDER + url[1] + BDefines.DIVIDER + url[2]);

                    DaoCore.createEntity(message);

                    sendMessage(message, new CompletionListenerWithData<BMessage>() {
                        @Override
                        public void onDone(BMessage bMessage) {
                            if (DEBUG)
                                Log.v(TAG, "sendMessageWithImage, onDone. Message ID: " + bMessage.getEntityID());
                            bMessage = DaoCore.updateEntity(bMessage);
                            listener.onDone(bMessage);
                        }

                        @Override
                        public void onDoneWithError(BError error) {
                            DaoCore.deleteEntity(message);
                            listener.onDoneWithError(error);
                        }
                    });
                }
                else {
                    listener.onDoneWithError(new BError(BError.Code.PARSE_EXCEPTION, exception));
                }
            }
        });
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
     * @param threadEntityId the id of the thread that the message is sent to.
     */
    public void sendMessageWithLocation(final String filePath, final LatLng location, long threadEntityId, final CompletionListenerWithData<BMessage> listener) {
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
        final BMessage message = new BMessage();
        message.setOwnerThread(threadEntityId);
        message.setType(LOCATION);
        message.setDate(new Date());
        message.setBUserSender(currentUser());

        ParseUtils.saveImageFileToParseWithThumbnail(filePath, BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE, new ParseUtils.MultiSaveCompletedListener() {
            @Override
            public void onSaved(ParseException exception, String... url) {
                if (exception == null) {
                    // Add the LatLng data to the message and the image url and thumbnail url
                    message.setText(String.valueOf(location.latitude) + BDefines.DIVIDER  + String.valueOf(location.longitude) + BDefines.DIVIDER  + url[0] + BDefines.DIVIDER + url[1] + BDefines.DIVIDER + url[2]);

                    DaoCore.createEntity(message);

                    sendMessage(message, new CompletionListenerWithData<BMessage>() {
                        @Override
                        public void onDone(BMessage bMessage) {
                            if (DEBUG)
                                Log.v(TAG, "sendMessageWithImage, onDone. Message ID: " + bMessage.getEntityID());
                            bMessage = DaoCore.updateEntity(bMessage);
                            listener.onDone(bMessage);
                        }

                        @Override
                        public void onDoneWithError(BError error) {
                            DaoCore.deleteEntity(message);
                            listener.onDoneWithError(error);
                        }
                    });
                }
                else {
                    listener.onDoneWithError(new BError(BError.Code.PARSE_EXCEPTION, exception));
                }

                new File(filePath).delete();
            }
        });
    }


    // TODO need to support progress feedback for dialog.
    public abstract void sendMessage(BMessage messages, CompletionListenerWithData<BMessage> listener);

    public abstract void loadMoreMessagesForThread(BThread thread, CompletionListenerWithData<BMessage[]> listener);

    public int getUnreadMessagesAmount(boolean onePerThread){
        List<BThread> threads = currentUser().getThreads();

        int count = 0;
        for (BThread t : threads)
        {
            if (onePerThread)
            {
                if(!t.isLastMessageWasRead())
                    count++;
            }
            else
            {
                count += t.getUnreadMessagesAmount();
            }
        }

        return count;
    }

    /*######################################################################################################*/
    /*Index*/
    public abstract void usersForIndex(String index, RepetitiveCompletionListener<BUser> listener);

    public abstract void removeUserFromIndex(BUser user, String index, CompletionListener listener);

    public abstract void addUserToIndex(BUser user, String index, CompletionListener listener);


/*######################################################################################################*/
    /*Thread*/
    /**
     * Create thread for given users.
     * When the thread is added to the server the "onMainFinished" will be invoked,
     * If an error occurred the error object would not be null.
     * For each user that was succesfully added the "onItem" method will be called,
     * For any item adding failure the "onItemFailed will be called.
     * If the main task will fail the error object in the "onMainFinished" method will be called.
     */
    public abstract void createThreadWithUsers(String name, List<BUser> users, RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object> listener);

    public void createThreadWithUsers(String name, RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object> listener, BUser... users) {
        createThreadWithUsers(name, Arrays.asList(users), listener);
    }

    public abstract void createPublicThreadWithName(String name, CompletionListenerWithDataAndError<BThread, Object> listener);

    public abstract void deleteThreadWithEntityID(String entityID, CompletionListener listener);

    public List<BThread> threadsWithType(int threadType) {

        if (currentUser() == null) {
            if (DEBUG) Log.e(TAG, "threadsWithType, Current user is null");
            return null;
        }

        TimingLogger timingLogger;
        if (DEBUG)
            timingLogger = new TimingLogger(TAG, "threadWithType, Type: " + threadType);

        BUser currentUser = currentUser(), threadCreator;

        // Get the thread list ordered desc by the last message added date.
        List<BThread> threadsFromDB;

        if (threadType == BThread.Type.Private)
            threadsFromDB = currentUser().getThreads();
        else threadsFromDB = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, threadType);

        List<BThread> threads = new ArrayList<BThread>();

        if (DEBUG)
            timingLogger.addSplit("Loading threads.");

        if (threadType == BThread.Type.Public)
        {
            for (BThread thread : threadsFromDB)
                if (thread.getType() == BThread.Type.Public)
                    threads.add(thread);
        }
        else {
            for (BThread thread : threadsFromDB) {
                if (DEBUG) Log.i(TAG, "threadsWithType, ThreadID: " + thread.getId());

                if (thread.getType() == null || thread.getType() == BThread.Type.Public)
                {
                    if (DEBUG) Log.e(TAG, "Thread has no type or is public, Thread ID: " + thread.getEntityID());
                    continue;
                }

                if (thread.getMessagesWithOrder(DaoCore.ORDER_DESC).size() > 0)
                {
                    threads.add(thread);
                    continue;
                }
                else if (DEBUG) Log.e(TAG, "threadsWithType, Thread has no messages.");

                threadCreator = thread.getCreator();
                if (threadCreator != null )
                {
                    if (DEBUG) Log.d(TAG, "thread has creator. Entity ID: " + thread.getEntityID());
                    if (threadCreator.equals(currentUser) && thread.hasUser(currentUser))
                    {
                        if (DEBUG) Log.d(TAG, "Current user is the creator.");
                        threads.add(thread);
                        continue;
                    }
                }
            }
        }

        if (DEBUG) Log.v(TAG, "threadsWithType, Type: " + threadType +", Found on db: " + threadsFromDB.size() + ", Threads List Size: " + threads.size());

        if (DEBUG)
            timingLogger.addSplit("Filtering threads.");

        Collections.sort(threads, new ThreadsSorter());
        if (DEBUG)
            timingLogger.addSplit("Ordering threads.");

        if (DEBUG)
            timingLogger.dumpToLog();

        return threads;
    }

    public List<ThreadsListAdapter.ThreadListItem> threadItemsWithType(int threadType) {
        if (DEBUG) Log.v(TAG, "threadItemsWithType, Type: " + threadType);
        if (currentUser() == null) {
            if (DEBUG) Log.e(TAG, "threadItemsWithType, Current user is null");
            return null;
        }

        TimingLogger timingLogger;
        if (DEBUG)
            timingLogger = new TimingLogger(TAG, "threadItemsWithType, Type: " + threadType);

        BUser currentUser = currentUser(), threadCreator;

        // Get the thread list ordered desc by the last message added date.
        List<BThread> threadsFromDB;

        if (threadType == BThread.Type.Private)
        {
            if (DEBUG) Log.v(TAG, "threadItemsWithType, loading private.");
            threadsFromDB = currentUser().getThreads(BThread.Type.Private);
        }
        else threadsFromDB = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, threadType);

        List<ThreadsListAdapter.ThreadListItem> threads = new ArrayList<ThreadsListAdapter.ThreadListItem>();
        if (DEBUG) Log.v(TAG, "threadItemsWithType, size: " + threadsFromDB.size());
        if (DEBUG)
            timingLogger.addSplit("Loading threads.");

        if (threadType == BThread.Type.Public)
        {
            for (BThread thread : threadsFromDB)
                if (thread.getType() == BThread.Type.Public)
                    threads.add(ThreadsListAdapter.ThreadListItem.fromBThread(thread));
        }
        else {
            for (BThread thread : threadsFromDB) {
                if (DEBUG) Log.i(TAG, "threadItemsWithType, ThreadID: " + thread.getId());

                if (thread.getMessagesWithOrder(DaoCore.ORDER_DESC).size() > 0)
                {
                    threads.add(ThreadsListAdapter.ThreadListItem.fromBThread(thread));
                    continue;
                }
                else if (DEBUG) Log.e(TAG, "threadItemsWithType, Thread has no messages.");

                threadCreator = thread.getCreator();
                if (threadCreator != null )
                {
                    if (DEBUG) Log.d(TAG, "thread has creator. Entity ID: " + thread.getEntityID());
                    if (threadCreator.equals(currentUser) && thread.hasUser(currentUser))
                    {
                        if (DEBUG) Log.d(TAG, "Current user is the creator.");
                        threads.add(ThreadsListAdapter.ThreadListItem.fromBThread(thread));
                        continue;
                    }
                }
            }
        }

        if (DEBUG) Log.v(TAG, "threadItemsWithType, Type: " + threadType +", Found on db: " + threadsFromDB.size() + ", Threads List Size: " + threads.size());

        if (DEBUG)
            timingLogger.addSplit("Filtering threads.");

        Collections.sort(threads, new ThreadsItemSorter());
        if (DEBUG)
            timingLogger.addSplit("Ordering threads.");

        if (DEBUG)
            timingLogger.dumpToLog();

        return threads;
    }

    private  class ThreadsItemsWithTypeCall implements Callable<List<ThreadsListAdapter.ThreadListItem>>{

        private int threadType;

        @Override
        public List<ThreadsListAdapter.ThreadListItem> call() throws Exception {
            if (DEBUG) Log.v(TAG, "threadItemsWithType, Type: " + threadType);

            BUser currentUser = currentUser(), threadCreator;

            if (currentUser == null) {
                if (DEBUG) Log.e(TAG, "threadItemsWithType, Current user is null");
                return null;
            }

            TimingLogger timingLogger;
            if (DEBUG)
                timingLogger = new TimingLogger(TAG, "threadItemsWithType, Type: " + threadType);



            // Get the thread list ordered desc by the last message added date.
            List<BThread> threadsFromDB;

            if (threadType == BThread.Type.Private)
            {
                if (DEBUG) Log.v(TAG, "threadItemsWithType, loading private.");
                threadsFromDB = currentUser.getThreads(BThread.Type.Private);
            }
            else threadsFromDB = DaoCore.fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, threadType);

            List<ThreadsListAdapter.ThreadListItem> threads = new ArrayList<ThreadsListAdapter.ThreadListItem>();
            if (DEBUG) Log.v(TAG, "threadItemsWithType, size: " + threadsFromDB.size());
            if (DEBUG)
                timingLogger.addSplit("Loading threads.");

            if (threadType == BThread.Type.Public)
            {
                for (BThread thread : threadsFromDB)
                    if (thread.getType() == BThread.Type.Public)
                        threads.add(ThreadsListAdapter.ThreadListItem.fromBThread(thread));
            }
            else {
                for (BThread thread : threadsFromDB) {
                    if (DEBUG) Log.i(TAG, "threadItemsWithType, ThreadID: " + thread.getId());

                    if (thread.getMessagesWithOrder(DaoCore.ORDER_DESC).size() > 0)
                    {
                        threads.add(ThreadsListAdapter.ThreadListItem.fromBThread(thread));
                        continue;
                    }
                    else if (DEBUG) Log.e(TAG, "threadItemsWithType, Thread has no messages.");

                    threadCreator = thread.getCreator();
                    if (threadCreator != null )
                    {
                        if (DEBUG) Log.d(TAG, "thread has creator. Entity ID: " + thread.getEntityID());
                        if (threadCreator.equals(currentUser) && thread.hasUser(currentUser))
                        {
                            if (DEBUG) Log.d(TAG, "Current user is the creator.");
                            threads.add(ThreadsListAdapter.ThreadListItem.fromBThread(thread));
                            continue;
                        }
                    }
                }
            }

            if (DEBUG) Log.v(TAG, "threadItemsWithType, Type: " + threadType +", Found on db: " + threadsFromDB.size() + ", Threads List Size: " + threads.size());

            if (DEBUG)
                timingLogger.addSplit("Filtering threads.");

            Collections.sort(threads, new ThreadsItemSorter());
            if (DEBUG)
                timingLogger.addSplit("Ordering threads.");

            if (DEBUG)
                timingLogger.dumpToLog();

            return threads;
        }
    }

    public abstract void deleteThread(BThread thread, CompletionListener listener);

    /**
     * Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was succesfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.
     */
    public abstract void addUsersToThread(BThread thread, List<BUser> users, RepetitiveCompletionListenerWithError<BUser, Object> listener);

    /**
     * Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.
     */
    public void addUsersToThread(BThread thread, final RepetitiveCompletionListenerWithError<BUser, Object> listener, BUser... users) {
        addUsersToThread(thread, Arrays.asList(users), listener);
    }

    /*######################################################################################################*/
    /*Getter And Setters*/
    public abstract void setLastOnline(Date date);

    public abstract String getServerURL();

    /**
     * Indicator that the current user in the adapter is authenticated.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Set the current status of the adapter to not authenticated.
     * The status can be retrieved by calling "isAuthenticated".
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public List<BLinkedContact> getContacs() {
        return currentUser().getBLinkedContacts();
    }

    /**
     * Get all messages for given thread id ordered Ascending/Descending
     */
    public List<BMessage> getMessagesForThreadForEntityID(Long id) {
        /* Get the messages by pre defined order*/
        //TODO add option to order the messages.
        return DaoCore.fetchEntitiesWithProperty(BMessage.class, BMessageDao.Properties.OwnerThread, id);
    }

    /**
     * @return the save auth id saved in the preference manager.
     * The preference manager is initialized when the BNetworkManager.Init(context) is called.
     */
    public String getCurrentUserAuthenticationId() {
        return BNetworkManager.preferences.getString(AuthenticationID, "");
    }

    /**
     * Currently supporting only string and integers. Long and other values can be added later on.
     */
    public void setLoginInfo(Map<String, Object> values) {

        SharedPreferences.Editor keyValuesEditor = BNetworkManager.preferences.edit();

        for (String s : values.keySet()) {
            if (values.get(s) instanceof Integer)
                keyValuesEditor.putInt(s, (Integer) values.get(s));
            else if (values.get(s) instanceof String)
                keyValuesEditor.putString(s, (String) values.get(s));
            else if (values.get(s) instanceof Boolean)
                keyValuesEditor.putBoolean(s, (Boolean) values.get(s));
            else Log.e(TAG, "Cant add this --> " + values.get(s) + " to the prefs");
        }

        keyValuesEditor.apply();
    }

    public void addLoginInfoData(String key, Object value){
        SharedPreferences.Editor keyValuesEditor = BNetworkManager.preferences.edit();
        if (value instanceof Integer)
            keyValuesEditor.putInt(key, (Integer) value);
        else if (value instanceof String)
            keyValuesEditor.putString(key, (String) value);
        else Log.e(TAG, "Cant add this --> " + value+ " to the prefs");

        keyValuesEditor.apply();
    }

    //http://stackoverflow.com/questions/8151523/how-to-store-and-retrieve-key-value-kind-of-data-using-saved-preferences-andro
    public Map<String, ?> getLoginInfo() {
        return BNetworkManager.preferences.getAll();
    }



/*######################################################################################################*/
    //TODO implement later on.
    /*// These are standard methods to register for push notifications
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    // This is an abstract method which must be overridden
    NSLog(@"application: didReceiveRemoteNotification: completion must be overridden");
    assert(1 == 2);
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    // This is an abstract method which must be overridden
    NSLog(@"application: withProgress: didRegisterForRemoteNotificationsWithDeviceToken must be overridden");
    assert(1 == 2);
}*/
}
