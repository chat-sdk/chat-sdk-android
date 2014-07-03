package com.braunster.chatsdk.network;


import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListener;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.object.BError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.braunster.chatsdk.dao.BMessage.Type.bImage;
import static com.braunster.chatsdk.dao.BMessage.Type.bLocation;
import static com.braunster.chatsdk.dao.BMessage.Type.bText;
import static com.braunster.chatsdk.network.BDefines.BAccountType.*;
import static com.braunster.chatsdk.network.BDefines.Prefs.AuthenticationID;

/**
 * Created by braunster on 23/06/14.
 */
public abstract class AbstractNetworkAdapter {

    //Note maybe catch the error if occur in the send message of some kind and try to fix them here before returning the error to the caller.
    private static final String TAG = AbstractNetworkAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;

    private boolean authenticated = false;

    public boolean accountTypeEnabled(int type) {
        switch (type) {
            case Anonymous:
                return BDefines.AnonymuosLoginEnabled;

            case Facebook:
                return !BDefines.FacebookAppId.equals("");

            case Google:
                return !BDefines.GoogleAppId.equals("");

            case Password:
            case Register:
                return true;

            case Twitter:
                return !BDefines.TwitterApiKey.equals("");

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
    public abstract void checkUserAuthenticatedWithCallback(CompletionListenerWithDataAndError<BUser, Object> listener);

    public abstract void pushUserWithCallback(CompletionListener listener);

    public abstract void logout();

    //TODO make an object that obtain the error message. need to see some errors before it.
    public abstract void getUserFacebookFriendsToAppWithComplition(CompletionListenerWithData<List<BUser>> listener);

    public abstract void getUserFacebookFriendsWithCallback(CompletionListenerWithData listener);

    public abstract BUser currentUser();
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
        message.setType(bText.ordinal());
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
        message.setType(bImage.ordinal());
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

    /*"http://developer.android.com/guide/topics/location/strategies.html"*/
    /**
     * Preparing a location message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * When done or when an error occurred the calling method will be notified.
     *
     * @param base64File     is a String representation of a bitmap that contain the image of the location wanted.
     * @param location       is the Latitude and Longitude of the picked location.
     * @param threadEntityId the id of the thread that the message is sent to.
     */
    public void sendMessageWithLocation(String base64File, LatLng location, long threadEntityId, final CompletionListenerWithData<BMessage> listener) {
        /* Prepare the message object for sending, after ready send it using send message abstract method.*/
        final BMessage message = new BMessage();
        message.setOwnerThread(threadEntityId);
        message.setType(bLocation.ordinal());
        message.setDate(new Date());
        message.setBUserSender(currentUser());

        // Add the LatLng data to the message and the base64 picture of the message if has any.
        message.setText(String.valueOf(location.latitude) + "&" + String.valueOf(location.longitude) + (base64File != null ? "&" + base64File : ""));

        DaoCore.createEntity(message);

        sendMessage(message, new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage bMessage) {
                if (DEBUG)
                    Log.v(TAG, "sendMessageWithLocation, onDone. Message ID: " + bMessage.getEntityID());
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

    // TODO need to support progress feedback for dialog.
    public abstract void sendMessage(BMessage messages, CompletionListenerWithData<BMessage> listener);

    public abstract void loadMoreMessagesForThread(BThread thread, CompletionListenerWithData<List<BMessage>> listener);
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

        // Get the thread list ordered desc by the last message added date.
        List<BThread> threadsFromDB = DaoCore.fetchEntitiesWithPropertiesAndOrder(BThread.class,
                BThreadDao.Properties.LastMessageAdded, DaoCore.ORDER_DESC, BThreadDao.Properties.Type, threadType);

        if (DEBUG) Log.v(TAG, "threadsWithType, Type: " + threadType +", Found on db: " + threadsFromDB.size());
        List<BThread> threads = new ArrayList<BThread>();

        if (currentUser() == null) {
            if (DEBUG) Log.e(TAG, "threadsWithType, Current user is null");
            return null;
        }

        for (BThread thread : threadsFromDB) {
            if (thread.getType() == BThread.Type.Public)
            {
                threads.add(thread);
                continue;
            }

            if (thread.getCreator() != null )
            {
                if (DEBUG) Log.d(TAG, "thread has creator. Entity ID: " + thread.getEntityID());
                if (thread.getCreator().equals(currentUser())&& thread.getUsers().contains(currentUser()));
                {
                    Log.d(TAG, "Current user is the creator.");
                    threads.add(thread);
                    continue;
                }
            }

            if (thread.getMessagesWithOrder(DaoCore.ORDER_DESC).size() > 0)
                threads.add(thread);
            else if (DEBUG) Log.e(TAG, "threadsWithType, Thread has no messages.");
        }

        return threads;
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

    // TODO add order veriable for the data. - Change method to DaoCore.fetchEntitiesWithPropertiesAndOrder()

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
            else Log.e(TAG, "Cant add this --> " + values.get(s) + " to the prefs");
        }

        keyValuesEditor.commit();
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
