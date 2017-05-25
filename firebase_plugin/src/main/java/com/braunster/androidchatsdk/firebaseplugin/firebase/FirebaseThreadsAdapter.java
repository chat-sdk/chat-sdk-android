package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.ChatSDKReceiver;

import co.chatsdk.core.dao.core.BMessage;
import co.chatsdk.core.dao.core.BThread;
import co.chatsdk.core.dao.core.BUser;
import co.chatsdk.core.dao.core.DaoDefines;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.wrappers.MessageWrapper;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.firebase.wrappers.UserWrapper;
import co.chatsdk.core.dao.core.DaoCore;
import co.chatsdk.core.defines.FirebaseDefines;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.NetworkManager;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.abstracthandlers.ThreadsManager;

/**
 * Created by KyleKrueger on 02.04.2017.
 */

public class FirebaseThreadsAdapter extends ThreadsManager {


    @Override
    public Single<List<BMessage>> loadMoreMessagesForThread(BThread thread) {
        return new ThreadWrapper(thread).loadMoreMessages(FirebaseDefines.NumberOfMessagesPerBatch);
    }

    /** Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.*/
    @Override
    public Flowable<BUser> addUsersToThread(final BThread thread, final List<BUser> users) {

        if(thread == null) {
            return Flowable.error(new Throwable("Thread cannot be null"));
        }

        ThreadWrapper threadWrapper = new ThreadWrapper(thread);
        ArrayList<Single<BUser>> singles = new ArrayList<>();

        for (final BUser user : users){
            singles.add(threadWrapper.addUser(UserWrapper.initWithModel(user)).toSingle(new Callable<BUser>() {
                @Override
                public BUser call () throws Exception {
                    return user;
                }
            }));
        }

        return Single.merge(singles);
    }

    @Override
    public Flowable<BUser> removeUsersFromThread(final BThread thread, List<BUser> users) {

        if(thread == null) {
            return Flowable.error(new Throwable("Thread cannot be null"));
        }

        ThreadWrapper threadWrapper = new ThreadWrapper(thread);
        ArrayList<Single<BUser>> singles = new ArrayList<>();

        for (final BUser user : users){
            singles.add(threadWrapper.removeUser(UserWrapper.initWithModel(user)).toSingle(new Callable<BUser>() {
                @Override
                public BUser call () throws Exception {
                    return user;
                }
            }));
        }

        return Single.merge(singles);
    }

    @Override
    public Completable  pushThread(BThread thread) {
        return new ThreadWrapper(thread).push();
    }

    /** Send a message,
     *  The message need to have a owner thread attached to it or it cant be added.
     *  If the destination thread is public the system will add the user to the message thread if needed.
     *  The uploading to the server part can bee seen her {@see FirebaseCoreAdapter#PushMessageWithComplition}.*/
    @Override
    public Completable sendMessage(final BMessage message){
        if (DEBUG) Timber.v("sendMessage");

        return new MessageWrapper(message).send().doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                // Setting the time stamp for the last message added to the thread.
                DatabaseReference threadRef = FirebasePaths.threadRef(message.getThread().getEntityID()).child(FirebasePaths.DetailsPath);

                threadRef.updateChildren(FirebasePaths.getMap(new String[]{DaoDefines.Keys.LastMessageAdded}, ServerValue.TIMESTAMP));

                // Pushing the message to all offline users. we cant push it before the message was
                // uploaded as the date is saved by the firebase server using the timestamp.
                pushForMessage(message);
            }
        });
    }


    /**
     * Create thread for given users.
     *  When the thread is added to the server the "onMainFinished" will be invoked,
     *  If an error occurred the error object would not be null.
     *  For each user that was successfully added the "onItem" method will be called,
     *  For any item adding failure the "onItemFailed will be called.
     *   If the main task will fail the error object in the "onMainFinished" method will be called."
     **/

    @Override
    public Single<BThread> createThreadWithUsers(final String name, final List<BUser> users) {
        return Single.create(new SingleOnSubscribe<BThread>() {
            @Override
            public void subscribe(final SingleEmitter<BThread> e) throws Exception {

                BUser currentUser = NetworkManager.shared().a.core.currentUserModel();

                if(!users.contains(currentUser)) {
                    users.add(currentUser);
                }

                if(users.size() == 2) {

                    BUser otherUser = null;
                    BThread jointThread = null;

                    for(BUser user : users) {
                        if(!user.equals(currentUser)) {
                            otherUser = user;
                            break;
                        }
                    }

                    // Check to see if a thread already exists with these
                    // two users

                    for(BThread thread : getThreads(ThreadType.Private1to1)) {
                        if(thread.getUsers().size() == 2 &&
                                thread.getUsers().contains(currentUser) &&
                                thread.getUsers().contains(otherUser))
                        {
                            jointThread = thread;
                            break;
                        }
                    }

                    if(jointThread != null) {
                        jointThread.setDeleted(false);
                        DaoCore.updateEntity(jointThread);
                        e.onSuccess(jointThread);
                    }
                }
                else {

                    final BThread thread = new BThread();
                    thread.setCreator(currentUser);
                    thread.setCreatorEntityId(currentUser.getEntityID());
                    thread.setCreationDate(new Date());
                    thread.setName(name);
                    thread.setType(users.size() == 2 ? ThreadType.Private1to1 : ThreadType.PrivateGroup);

                    // Save the thread to the database.

                    e.onSuccess(thread);

                }
            }
        }).flatMap(new Function<BThread, SingleSource<? extends BThread>>() {
            @Override
            public SingleSource<? extends BThread> apply(final BThread thread) throws Exception {
                return new SingleSource<BThread>() {
                    @Override
                    public void subscribe(final SingleObserver<? super BThread> observer) {

                        ThreadWrapper wrapper = new ThreadWrapper(thread);
                        wrapper.push().andThen(addUsersToThread(thread, users)).doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                observer.onSuccess(thread);
                            }
                        }).subscribe();

                    }
                };
            }
        }).doOnSuccess(new Consumer<BThread>() {
            @Override
            public void accept(BThread thread) throws Exception {
                DaoCore.createEntity(thread);
                DaoCore.connectUserAndThread(NetworkManager.shared().a.core.currentUserModel(),thread);
                DaoCore.updateEntity(thread);
            }
        });
    }

    @Override
    public Completable deleteThreadWithEntityID(final String entityID) {
        final BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, entityID);
        return new ThreadWrapper(thread).deleteThread();
    }

    protected void pushForMessage(final BMessage message){
        if (NetworkManager.shared().a.push == null)
            return;

        if (DEBUG) Timber.v("pushForMessage");
        if (message.getThread().typeIs(ThreadType.Private)) {

            // Loading the message from firebase to get the timestamp from server.
            DatabaseReference firebase = FirebasePaths.threadRef(message.getThread().getEntityID())
                    .child(FirebasePaths.MessagesPath)
                    .child(message.getEntityID());

            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Long date = null;
                    try {
                        date = (Long) snapshot.child(DaoDefines.Keys.Date).getValue();
                    } catch (ClassCastException e) {
                        date = (((Double)snapshot.child(DaoDefines.Keys.Date).getValue()).longValue());
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

                    BUser currentUser = NetworkManager.shared().a.core.currentUserModel();
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

        if (NetworkManager.shared().a.push == null || users.size() == 0)
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

        if (message.getType() == BMessage.Type.LOCATION)
            messageText = "Location CoreMessage";
        else if (message.getType() == BMessage.Type.IMAGE)
            messageText = "Picture CoreMessage";

        String sender = message.getSender().getMetaName();
        String fullText = sender + " " + messageText;

        JSONObject data = new JSONObject();
        try {
            data.put(DaoDefines.Keys.ACTION, ChatSDKReceiver.ACTION_MESSAGE);

            data.put(DaoDefines.Keys.CONTENT, fullText);
            data.put(DaoDefines.Keys.MESSAGE_ENTITY_ID, message.getEntityID());
            data.put(DaoDefines.Keys.THREAD_ENTITY_ID, message.getThread().getEntityID());
            data.put(DaoDefines.Keys.MESSAGE_DATE, message.getDate().toDate().getTime());
            data.put(DaoDefines.Keys.MESSAGE_SENDER_ENTITY_ID, message.getSender().getEntityID());
            data.put(DaoDefines.Keys.MESSAGE_SENDER_NAME, message.getSender().getMetaName());
            data.put(DaoDefines.Keys.MESSAGE_TYPE, message.getType());
            data.put(DaoDefines.Keys.MESSAGE_PAYLOAD, message.getText());
            //For iOS
            data.put(DaoDefines.Keys.BADGE, DaoDefines.Keys.INCREMENT);
            data.put(DaoDefines.Keys.ALERT, fullText);
            // For making sound in iOS
            data.put(DaoDefines.Keys.SOUND, DaoDefines.Keys.Default);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        NetworkManager.shared().a.push.pushToChannels(channels, data);
    }


}
