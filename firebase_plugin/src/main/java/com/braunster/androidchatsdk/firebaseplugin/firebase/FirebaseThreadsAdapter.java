package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.ChatSDKReceiver;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BMessageWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BThreadWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MasterDeferredObject;
import org.jdeferred.multiple.MasterProgress;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.abstracthandlers.ThreadsManager;

import static com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors.getFirebaseError;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.LOCATION;

/**
 * Created by KyleKrueger on 02.04.2017.
 */

public class FirebaseThreadsAdapter extends ThreadsManager {


    @Override
    public Promise<List<BMessage>, Void, Void> loadMoreMessagesForThread(BThread thread) {
        return new BThreadWrapper(thread).loadMoreMessages(BFirebaseDefines.NumberOfMessagesPerBatch);
    }

    @Override
    public Promise<BThread, BError, Void> createPublicThreadWithName(String name) {

        final Deferred<BThread, BError, Void> deferred = new DeferredObject<>();

        // Crating the new thread.
        // This thread would not be saved to the local db until it is successfully uploaded to the firebase server.
        final BThread thread = new BThread();

        BUser curUser = BNetworkManager.getCoreInterface().currentUserModel();
        thread.setCreator(curUser);
        thread.setCreatorEntityId(curUser.getEntityID());
        thread.setType(BThread.Type.Public);
        thread.setName(name);

        // Add the path and API key
        // This allows you to restrict public threads to a particular
        // API key or root key
        thread.setRootKey(BDefines.BRootPath);
        thread.setApiKey("");

        // Save the entity to the local db.
        DaoCore.createEntity(thread);

        BThreadWrapper wrapper = new BThreadWrapper(thread);

        wrapper.push()
                .done(new DoneCallback<BThread>() {
                    @Override
                    public void onDone(final BThread thread) {
                        DaoCore.updateEntity(thread);

                        if (DEBUG) Timber.d("public thread is pushed and saved.");

                        // Add the thread to the list of public threads
                        DatabaseReference publicThreadRef = FirebasePaths.publicThreadsRef()
                                .child(thread.getEntityID())
                                .child("null");

                        publicThreadRef.setValue("", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError error, DatabaseReference firebase) {
                                if (error == null)
                                    deferred.resolve(thread);
                                else {
                                    if (DEBUG)
                                        Timber.e("Unable to add thread to public thread ref.");
                                    DaoCore.deleteEntity(thread);
                                    deferred.reject(getFirebaseError(error));
                                }
                            }
                        });
                    }
                })
                .fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        if (DEBUG) Timber.e("Failed to push thread to ref.");
                        DaoCore.deleteEntity(thread);
                        deferred.reject(error);
                    }
                });

        return deferred.promise();
    }

    /** Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.*/
    @Override
    public Promise<BThread, BError, Void> addUsersToThread(final BThread thread, final List<BUser> users) {

        final Deferred<BThread, BError, Void>  deferred = new DeferredObject<>();

        if (thread == null)
        {
            if (DEBUG) Timber.e("addUsersToThread, Thread is null" );
            return deferred.reject(new BError(BError.Code.NULL, "Thread is null"));
        }

        if (DEBUG) Timber.d("Users Amount: %s", users.size());

        Promise[] promises = new Promise[users.size()];

        BThreadWrapper threadWrapper = new BThreadWrapper(thread);

        int count = 0;
        for (BUser user : users){

            // Add the user to the thread
            if (!user.hasThread(thread))
            {
                DaoCore.connectUserAndThread(user, thread);
            }

            promises[count] = threadWrapper.addUser(BUserWrapper.initWithModel(user));
            count++;
        }

        MasterDeferredObject masterDeferredObject = new MasterDeferredObject(promises);

        masterDeferredObject.progress(new ProgressCallback<MasterProgress>() {
            @Override
            public void onProgress(MasterProgress masterProgress) {
                if (masterProgress.getFail() + masterProgress.getDone() == masterProgress.getTotal())
                {
                    // Reject the promise if all promisses failed.
                    if (masterProgress.getFail() == masterProgress.getTotal())
                    {
                        deferred.reject(BError.getError(BError.Code.OPERATION_FAILED, "All promises failed"));
                    }
                    else
                        deferred.resolve(thread);
                }
            }
        });


        return deferred.promise();
    }

    @Override
    public Promise<BThread, BError, Void> removeUsersFromThread(final BThread thread, List<BUser> users) {
        final Deferred<BThread, BError, Void>  deferred = new DeferredObject<>();

        if (thread == null)
        {
            if (DEBUG) Timber.e("addUsersToThread, Thread is null" );
            return deferred.reject(new BError(BError.Code.NULL, "Thread is null"));
        }

        if (DEBUG) Timber.d("Users Amount: %s", users.size());

        Promise[] promises = new Promise[users.size()];

        BThreadWrapper threadWrapper = new BThreadWrapper(thread);

        int count = 0;
        for (BUser user : users){

            // Breaking the connection in the internal database between the thread and the user.
            DaoCore.breakUserAndThread(user, thread);

            promises[count] = threadWrapper.removeUser(BUserWrapper.initWithModel(user));
            count++;
        }

        MasterDeferredObject masterDeferredObject = new MasterDeferredObject(promises);

        masterDeferredObject.progress(new ProgressCallback<MasterProgress>() {
            @Override
            public void onProgress(MasterProgress masterProgress) {
                if (masterProgress.getFail() + masterProgress.getDone() == masterProgress.getTotal())
                {
                    // Reject the promise if all promisses failed.
                    if (masterProgress.getFail() == masterProgress.getTotal())
                    {
                        deferred.reject(null);
                    }
                    else
                        deferred.resolve(thread);
                }
            }
        });

        return deferred.promise();
    }

    @Override
    public Promise<BThread, BError, Void>  pushThread(BThread thread) {
        return new BThreadWrapper(thread).push();
    }




    /** Send a message,
     *  The message need to have a owner thread attached to it or it cant be added.
     *  If the destination thread is public the system will add the user to the message thread if needed.
     *  The uploading to the server part can bee seen her {@see FirebaseCoreAdapter#PushMessageWithComplition}.*/
    @Override
    public Promise<BMessage, BError, BMessage> sendMessage(final BMessage message){
        if (DEBUG) Timber.v("sendMessage");

        return new BMessageWrapper(message).send().done(new DoneCallback<BMessage>() {
            @Override
            public void onDone(BMessage message) {
                // Setting the time stamp for the last message added to the thread.
                DatabaseReference threadRef = FirebasePaths.threadRef(message.getThread().getEntityID()).child(BFirebaseDefines.Path.BDetailsPath);

                threadRef.updateChildren(FirebasePaths.getMap(new String[]{BDefines.Keys.BLastMessageAdded}, ServerValue.TIMESTAMP));

                // Pushing the message to all offline users. we cant push it before the message was
                // uploaded as the date is saved by the firebase server using the timestamp.
                pushForMessage(message);
            }
        });
    }


    /** Create thread for given users.
     *  When the thread is added to the server the "onMainFinished" will be invoked,
     *  If an error occurred the error object would not be null.
     *  For each user that was successfully added the "onItem" method will be called,
     *  For any item adding failure the "onItemFailed will be called.
     *   If the main task will fail the error object in the "onMainFinished" method will be called."*/
    @Override
    public Promise<BThread, BError, Void> createThreadWithUsers(String name, final List<BUser> users) {
        final Deferred<BThread, BError, Void> deferred = new DeferredObject<>();

        ThreadRecovery.checkForAndRecoverThreadWithUsers(users)
                .done(new DoneCallback<BThread>() {
                    @Override
                    public void onDone(final BThread thread) {
                        deferred.resolve(thread);
                    }
                })
                .fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        // Didn't find a new thread so we create a new.
                        final BThread thread = new BThread();
                        BUser currentUser = BNetworkManager.getCoreInterface().currentUserModel();
                        thread.setCreator(currentUser);
                        thread.setCreatorEntityId(currentUser.getEntityID());

                        // If we're assigning users then the thread is always going to be private
                        thread.setType(BThread.Type.Private);

                        // Save the thread to the database.
                        DaoCore.createEntity(thread);
                        DaoCore.connectUserAndThread(BNetworkManager.getCoreInterface().currentUserModel(),thread);

                        BNetworkManager.getCoreInterface().updateLastOnline();

                        new BThreadWrapper(thread).push()
                                .done(new DoneCallback<BThread>() {
                                    @Override
                                    public void onDone(BThread thread) {

                                        // Save the thread to the local db.
                                        DaoCore.updateEntity(thread);

                                        // Add users, For each added user the listener passed here will get a call.
                                        addUsersToThread(thread, users).done(new DoneCallback<BThread>() {
                                            @Override
                                            public void onDone(BThread thread) {
                                                deferred.resolve(thread);
                                            }
                                        })
                                                .fail(new FailCallback<BError>() {
                                                    @Override
                                                    public void onFail(BError error) {
                                                        deferred.reject(error);
                                                    }
                                                });
                                    }
                                })
                                .fail(new FailCallback<BError>() {
                                    @Override
                                    public void onFail(BError error) {
                                        // Delete the thread if failed to push
                                        DaoCore.deleteEntity(thread);

                                        deferred.reject(error);
                                    }
                                });
                    }
                });


        return deferred.promise();
    }



    @Override
    public Promise<Void, BError, Void> deleteThreadWithEntityID(final String entityID) {

        final BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, entityID);

        BNetworkManager.getCoreInterface().updateLastOnline();

        return new BThreadWrapper(thread).deleteThread();
    }



    protected void pushForMessage(final BMessage message){
        if (!BNetworkManager.getCoreInterface().backendlessEnabled())
            return;

        if (DEBUG) Timber.v("pushForMessage");
        if (message.getThread().getTypeSafely() == BThread.Type.Private) {

            // Loading the message from firebase to get the timestamp from server.
            DatabaseReference firebase = FirebasePaths.threadRef(message.getThread().getEntityID())
                    .child(BFirebaseDefines.Path.BMessagesPath)
                    .child(message.getEntityID());

            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Long date = null;
                    try {
                        date = (Long) snapshot.child(BDefines.Keys.BDate).getValue();
                    } catch (ClassCastException e) {
                        date = (((Double)snapshot.child(BDefines.Keys.BDate).getValue()).longValue());
                    }
                    finally {
                        if (date != null)
                        {
                            message.setDate(new DateTime(date));
                            DaoCore.updateEntity(message);
                        }
                    }

                    // If we failed to get date dont push.
                    if (message.getDate()==null)
                        return;

                    BUser currentUser = BNetworkManager.getCoreInterface().currentUserModel();
                    List<BUser> users = new ArrayList<BUser>();

                    for (BUser user : message.getThread().getUsers())
                        if (!user.equals(currentUser))
                            if (!user.equals(currentUser)) {
                                // Timber.v(user.getEntityID() + ", " + user.getOnline().toString());
                                // sends push notification regardless of receiver online status
                                // TODO: add observer to online status
                                // if (user.getOnline() == null || !user.getOnline())
                                users.add(user);
                            }

                    pushToUsers(message, users);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }

    protected void pushToUsers(BMessage message, List<BUser> users){
        if (DEBUG) Timber.v("pushToUsers");

        if (!BNetworkManager.getCoreInterface().backendlessEnabled() || users.size() == 0)
            return;

        // We're identifying each user using push channels. This means that
        // when a user signs up, they register with backendless on a particular
        // channel. In this case user_[user id] this means that we can
        // send a push to a specific user if we know their user id.
        List<String> channels = new ArrayList<String>();
        for (BUser user : users)
            channels.add(user.getPushChannel());

        if (DEBUG) Timber.v("pushutils sendmessage");
        String messageText = message.getText();

        if (message.getType() == LOCATION)
            messageText = "Location Message";
        else if (message.getType() == IMAGE)
            messageText = "Picture Message";

        String sender = message.getSender().getMetaName();
        String fullText = sender + " " + messageText;

        JSONObject data = new JSONObject();
        try {
            data.put(BDefines.Keys.ACTION, ChatSDKReceiver.ACTION_MESSAGE);

            data.put(BDefines.Keys.CONTENT, fullText);
            data.put(BDefines.Keys.MESSAGE_ENTITY_ID, message.getEntityID());
            data.put(BDefines.Keys.THREAD_ENTITY_ID, message.getThread().getEntityID());
            data.put(BDefines.Keys.MESSAGE_DATE, message.getDate().toDate().getTime());
            data.put(BDefines.Keys.MESSAGE_SENDER_ENTITY_ID, message.getSender().getEntityID());
            data.put(BDefines.Keys.MESSAGE_SENDER_NAME, message.getSender().getMetaName());
            data.put(BDefines.Keys.MESSAGE_TYPE, message.getType());
            data.put(BDefines.Keys.MESSAGE_PAYLOAD, message.getText());
            //For iOS
            data.put(BDefines.Keys.BADGE, BDefines.Keys.INCREMENT);
            data.put(BDefines.Keys.ALERT, fullText);
            // For making sound in iOS
            data.put(BDefines.Keys.SOUND, BDefines.Keys.Default);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BNetworkManager.getCoreInterface().getPushHandler().pushToChannels(channels, data);
    }


}
