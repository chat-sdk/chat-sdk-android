package co.chatsdk.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.update.FirebaseUpdate;
import co.chatsdk.firebase.update.FirebaseUpdateWriter;
import co.chatsdk.firebase.wrappers.MessageWrapper;
import co.chatsdk.firebase.wrappers.ThreadPusher;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public class FirebaseThreadHandler extends AbstractThreadHandler {

    public static int UserThreadLinkTypeAddUser = 1;
    public static int UserThreadLinkTypeRemoveUser = 2;

    public Single<List<Message>> loadMoreMessagesForThread(final Date fromDate, final Thread thread, boolean loadFromServer) {
        return super.loadMoreMessagesForThread(fromDate, thread, loadFromServer).flatMap(localMessages -> {

            int messageToLoad = ChatSDK.config().messagesToLoadPerBatch;
            int localMessageSize = localMessages.size();

            if (localMessageSize < messageToLoad && loadFromServer) {
                // If we did load some messages locally, update the from date to the last of those messages
                Date finalFromDate = localMessageSize > 0 ? localMessages.get(localMessageSize-1).getDate().toDate() : fromDate;

                return new ThreadWrapper(thread).loadMoreMessages(finalFromDate, messageToLoad).map((Function<List<Message>, List<Message>>) remoteMessages -> {

//                    if (ChatSDK.encryption() != null) {
//                        for (Message m : remoteMessages) {
//                            ChatSDK.encryption().decrypt(m);
//                        }
//                    }

                    ArrayList<Message> mergedMessages = new ArrayList<>(localMessages);
                    mergedMessages.addAll(remoteMessages);

                    if (ChatSDK.encryption() != null) {
                        for (Message m : mergedMessages) {
                            ChatSDK.encryption().decrypt(m);
                        }
                    }

                    return mergedMessages;
                });
            }
            return Single.just(localMessages);
        });
    }

    /**
     * Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.
     **/
    public Completable addUsersToThread(final Thread thread, final List<User> users) {
        return setUserThreadLinkValue(thread, users, UserThreadLinkTypeAddUser);
    }

    /**
     * This function is a convenience function to add or remove batches of users
     * from threads. If the value is defined, it will populate the thread/users
     * path with the user IDs. And add the thread ID to the user/threads path for
     * private threads. If value is null, the users will be removed from the thread/users
     * path and the thread will be removed from the user/threads path
     *
     * @param thread
     * @param users
     * @param userThreadLinkType - 1 => Add, 2 => Remove
     * @return
     */
    protected Completable setUserThreadLinkValue(final Thread thread, final List<User> users, final int userThreadLinkType) {
        return Single.create((SingleOnSubscribe<FirebaseUpdateWriter>) emitter -> {
            FirebaseUpdateWriter updateWriter = new FirebaseUpdateWriter(FirebaseUpdateWriter.Type.Update);

            for (final User u : users) {

                DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(u.getEntityID()).child(Keys.Status);
                DatabaseReference userThreadsRef = FirebasePaths.userThreadsRef(u.getEntityID()).child(thread.getEntityID()).child(Keys.InvitedBy);

                if (userThreadLinkType == UserThreadLinkTypeAddUser) {

                    updateWriter.add(new FirebaseUpdate(threadUsersRef, u.equalsEntity(thread.getCreator()) ? Keys.Owner : Keys.Member));

                    // Public threads aren't added to the user path
                    if (!thread.typeIs(ThreadType.Public)) {
                        updateWriter.add(new FirebaseUpdate(userThreadsRef, ChatSDK.currentUserID()));
                    }

                    if (thread.typeIs(ThreadType.Public) && u.isMe() && !ChatSDK.config().publicChatAutoSubscriptionEnabled) {
                        threadUsersRef.onDisconnect().removeValue();
                    }

                } else if (userThreadLinkType == UserThreadLinkTypeRemoveUser) {
                    updateWriter.add(new FirebaseUpdate(threadUsersRef, null));
                    updateWriter.add(new FirebaseUpdate(userThreadsRef, null));
                }
            }
            emitter.onSuccess(updateWriter);
        }).flatMap((Function<FirebaseUpdateWriter, SingleSource<?>>) FirebaseUpdateWriter::execute)
                .ignoreElement()
                .doOnComplete(() -> {
            FirebaseEntity.pushThreadUsersUpdated(thread.getEntityID()).subscribe(ChatSDK.events());
            for (User u : users) {
                FirebaseEntity.pushUserThreadsUpdated(u.getEntityID()).subscribe(ChatSDK.events());
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable mute(Thread thread) {
        return Completable.create(emitter -> {
            DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(ChatSDK.currentUserID()).child(Keys.Mute);
            threadUsersRef.setValue(true).addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
        }).subscribeOn(Schedulers.io());
    }

    public Completable unmute(Thread thread) {
        return Completable.create(emitter -> {
            DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(ChatSDK.currentUserID()).child(Keys.Mute);
            threadUsersRef.setValue(false).addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
        }).subscribeOn(Schedulers.io());
    }

    public Completable removeUsersFromThread(final Thread thread, List<User> users) {
        return setUserThreadLinkValue(thread, users, UserThreadLinkTypeRemoveUser);
    }

    public Completable pushThread(Thread thread) {
        return new ThreadWrapper(thread).push();
    }

    public Completable pushThreadMeta(Thread thread) {
        return new ThreadWrapper(thread).pushMeta();
    }

    @Override
    public boolean muteEnabled(Thread thread) {
        return true;
    }

    /**
     * Send a text,
     * The text need to have a owner thread attached to it or it cant be added.
     * If the destination thread is public the system will add the user to the text thread if needed.
     * The uploading to the server part can bee seen her {@see FirebaseCoreAdapter#PushMessageWithComplition}.
     */
    public Completable sendMessage(final Message message) {
        return new MessageWrapper(message).send().andThen(new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                pushForMessage(message);
                observer.onComplete();
            }
        });
    }

    public Single<Thread> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL) {
        return Single.create((SingleOnSubscribe<ThreadPusher>) e -> {

            // If the entity ID is set, see if the thread exists and return it if it does
            // TODO: Check this - what if for some reason the user isn't a member of this thread?
            if (entityID != null) {
                Thread t = ChatSDK.db().fetchThreadWithEntityID(entityID);
                if (t != null) {
                    e.onSuccess(new ThreadPusher(t, false));
                    return;
                }
            }
            ArrayList<User> users = new ArrayList<>(theUsers);
            User currentUser = ChatSDK.currentUser();

            if (!users.contains(currentUser)) {
                users.add(currentUser);
            }

            if (users.size() == 2 && (type == ThreadType.None || ThreadType.is(type, ThreadType.Private1to1))) {

                User otherUser = null;
                Thread jointThread = null;

                for (User user : users) {
                    if (!user.equals(currentUser)) {
                        otherUser = user;
                        break;
                    }
                }

                // Check to see if a thread already exists with these
                // two users
                for(Thread thread : getThreads(ThreadType.Private1to1, ChatSDK.config().reuseDeleted1to1Threads, true)) {
                    if(thread.getUsers().size() == 2 &&
                            thread.containsUser(currentUser) &&
                            thread.containsUser(otherUser))
                    {
                        jointThread = thread;
                        break;
                    }
                }

                if(jointThread != null) {
                    jointThread.setDeleted(false);
                    jointThread.update();
                    e.onSuccess(new ThreadPusher(jointThread, false));
                    return;
                }
            }

            final Thread thread = ChatSDK.db().createEntity(Thread.class);

            thread.setEntityID(entityID);
            thread.setCreator(currentUser);
            thread.setCreationDate(new Date());
            thread.setName(name);
            thread.setImageUrl(imageURL);
            thread.addUsers(users);

            if (type != -1) {
                thread.setType(type);
            } else {
                thread.setType(users.size() == 2 ? ThreadType.Private1to1 : ThreadType.PrivateGroup);
            }

            thread.update();

            // Save the thread to the database.
            e.onSuccess(new ThreadPusher(thread, true));

        }).flatMap((Function<ThreadPusher, SingleSource<Thread>>) ThreadPusher::push)
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteThread(Thread thread) {
        return Completable.defer(() -> {
            return new ThreadWrapper(thread).deleteThread();
        }).subscribeOn(Schedulers.io());
    }

    protected void pushForMessage(final Message message) {
        if (ChatSDK.push() != null && message.getThread().typeIs(ThreadType.Private)) {
            HashMap<String, Object> data = ChatSDK.push().pushDataForMessage(message);
            ChatSDK.push().sendPushNotification(data);
        }
    }

    public Completable deleteMessage(Message message) {
        return new MessageWrapper(message).delete();
    }

    public Completable leaveThread(Thread thread) {
        return null;
    }

    public Completable joinThread(Thread thread) {
        return null;
    }

}
