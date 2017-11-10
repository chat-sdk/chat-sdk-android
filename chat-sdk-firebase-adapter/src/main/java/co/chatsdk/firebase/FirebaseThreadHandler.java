package co.chatsdk.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.FirebaseDefines;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.firebase.wrappers.MessageWrapper;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public class FirebaseThreadHandler extends AbstractThreadHandler {

    public Single<List<Message>> loadMoreMessagesForThread(final Message fromMessage,final Thread thread) {
        return super.loadMoreMessagesForThread(fromMessage, thread).flatMap(new Function<List<Message>, SingleSource<? extends List<Message>>>() {
            @Override
            public SingleSource<? extends List<Message>> apply(List<Message> messages) throws Exception {
                if(messages.isEmpty()) {
                    return new ThreadWrapper(thread).loadMoreMessages(fromMessage, FirebaseDefines.NumberOfMessagesPerBatch);
                }
                return Single.just(messages);
            }
        });
    }

    /**
     * Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.
     **/
    public Completable addUsersToThread(final Thread thread, final List<User> users) {
        return setUserThreadLinkValue(thread, users, Keys.Null);
    }

    /**
     * This function is a convenience function to add or remove batches of users
     * from threads. If the value is defined, it will populate the thread/users
     * path with the user IDs. And add the thread ID to the user/threads path for
     * private threads. If value is null, the users will be removed from the thread/users
     * path and the thread will be removed from the user/threads path
     * @param thread
     * @param users
     * @param value
     * @return
     */
    private Completable setUserThreadLinkValue(final Thread thread, final List<User> users, final String value) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                DatabaseReference ref = FirebasePaths.firebaseRef();
                final HashMap<String, Object> data = new HashMap<>();

                for (final User u : users) {
                    PathBuilder threadUsersPath = FirebasePaths.threadUsersPath(thread.getEntityID(), u.getEntityID());
                    PathBuilder userThreadsPath = FirebasePaths.userThreadsPath(u.getEntityID(), thread.getEntityID());

                    if (value != null) {
                        threadUsersPath.append(Keys.Null);
                        userThreadsPath.append(Keys.InvitedBy);
                    }

                    data.put(threadUsersPath.build(), value);

                    if (thread.typeIs(ThreadType.Private)) {
                        data.put(userThreadsPath.build(), value != null ? NM.currentUser().getEntityID() : value);
                    }
                    else if (value != null) {
                        // TODO: Check this
                        // If we add users to a public thread, make sure that they are removed if we
                        // log off
                        FirebasePaths.firebaseRef().child(threadUsersPath.build()).onDisconnect().removeValue();
                    }
                }

                ref.updateChildren(data, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            FirebaseEntity.pushThreadUsersUpdated(thread.getEntityID()).subscribe();
                            for(User u : users) {
                                FirebaseEntity.pushUserThreadsUpdated(u.getEntityID()).subscribe();
                            }
                            e.onComplete();
                        } else {
                            e.onError(databaseError.toException());
                        }
                    }
                });
            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable removeUsersFromThread(final Thread thread, List<User> users) {
        return setUserThreadLinkValue(thread, users, null);
    }

    public Completable pushThread(Thread thread) {
        return new ThreadWrapper(thread).push();
    }

    /** Send a message,
     *  The message need to have a owner thread attached to it or it cant be added.
     *  If the destination thread is public the system will add the user to the message thread if needed.
     *  The uploading to the server part can bee seen her {@see FirebaseCoreAdapter#PushMessageWithComplition}.*/
    public Observable<MessageSendProgress> sendMessage(final Message message){
        return Observable.create(new ObservableOnSubscribe<MessageSendProgress>() {
            @Override
            public void subscribe(final ObservableEmitter<MessageSendProgress> e) throws Exception {
                new MessageWrapper(message).send()
                        .subscribeOn(Schedulers.single())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                pushForMessage(message);
                                e.onNext(new MessageSendProgress(message));
                                e.onComplete();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                e.onError(throwable);
                            }
                        });
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
    public Single<Thread> createThread(final List<User> users) {
        return createThread(null, users);
    }

    public Single<Thread> createThread(final String name, final List<User> users) {
        return createThread(name, users, -1);
    }

    public Single<Thread> createThread(final String name, final List<User> users, final int type) {
        return Single.create(new SingleOnSubscribe<Thread>() {
            @Override
            public void subscribe(final SingleEmitter<Thread> e) throws Exception {

                User currentUser = NM.currentUser();

                if(!users.contains(currentUser)) {
                    users.add(currentUser);
                }

                if(users.size() == 2 && (type == -1 || type == ThreadType.Private1to1)) {

                    User otherUser = null;
                    Thread jointThread = null;

                    for(User user : users) {
                        if(!user.equals(currentUser)) {
                            otherUser = user;
                            break;
                        }
                    }

                    // Check to see if a thread already exists with these
                    // two users
                    for(Thread thread : getThreads(ThreadType.Private1to1, true)) {
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
                        return;
                    }
                }

                final Thread thread = DaoCore.getEntityForClass(Thread.class);
                DaoCore.createEntity(thread);
                thread.setCreator(currentUser);
                thread.setCreatorEntityId(currentUser.getEntityID());
                thread.setCreationDate(new Date());
                thread.setName(name);

                if(type != -1) {
                    thread.setType(type);
                }
                else {
                    thread.setType(users.size() == 2 ? ThreadType.Private1to1 : ThreadType.PrivateGroup);
                }

                // Save the thread to the database.
                e.onSuccess(thread);

            }
        }).flatMap(new Function<Thread, SingleSource<? extends Thread>>() {
            @Override
            public SingleSource<? extends Thread> apply(final Thread thread) throws Exception {
                return Single.create(new SingleOnSubscribe<Thread>() {
                    @Override
                    public void subscribe(final SingleEmitter<Thread> e) throws Exception {
                        if(thread.getEntityID() == null) {
                            ThreadWrapper wrapper = new ThreadWrapper(thread);

                            wrapper.push().concatWith(addUsersToThread(thread, users)).doOnError(new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    throwable.printStackTrace();
                                    e.onError(throwable);
                                }
                            }).subscribe(new Action() {
                                @Override
                                public void run() throws Exception {
                                    e.onSuccess(thread);
                                }
                            });

                        }
                        else {
                            e.onSuccess(thread);
                        }
                    }
                });
            }
        }).doOnSuccess(new Consumer<Thread>() {
            @Override
            public void accept(Thread thread) throws Exception {
                thread.addUser(NM.currentUser());
            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable deleteThread(Thread thread) {
        return deleteThreadWithEntityID(thread.getEntityID());
    }

    public Completable deleteThreadWithEntityID(final String entityID) {
        return Single.create(new SingleOnSubscribe<Thread>() {
            @Override
            public void subscribe(SingleEmitter<Thread> e) throws Exception {
                final Thread thread = DaoCore.fetchEntityWithEntityID(Thread.class, entityID);
                e.onSuccess(thread);
            }
        }).flatMapCompletable(new Function<Thread, Completable>() {
            @Override
            public Completable apply(Thread thread) throws Exception {
                return new ThreadWrapper(thread).deleteThread();
            }
        }).subscribeOn(Schedulers.single());
    }

    protected void pushForMessage(final Message message){
        if (NM.push() == null) {
            return;
        }

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
                        date = (Long) snapshot.child(Keys.Date).getValue();
                    } catch (ClassCastException e) {
                        date = (((Double)snapshot.child(Keys.Date).getValue()).longValue());
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

                    User currentUser = NM.currentUser();
                    List<User> users = new ArrayList<User>();

                    for (User user : message.getThread().getUsers())
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

    protected void pushToUsers(Message message, List<User> users){

        if (NM.push() == null || users.size() == 0)
            return;

        // We'recyclerView identifying each user using push channels. This means that
        // when a user signs up, they signUp with backendless on a particular
        // channel. In this case user_[user id] this means that we can
        // send a push to a specific user if we know their user id.
        List<String> channels = new ArrayList<>();
        for (User user : users) {
            channels.add(user.getPushChannel());
        }

        String messageText = Strings.payloadAsString(message);

        String sender = message.getSender().getName();
        String fullText = sender + " " + messageText;

//        JSONObject data = new JSONObject();
//        try {
//            data.put(Keys.ACTION, ChatSDKReceiver.ACTION_MESSAGE);
//
//            data.put(Keys.CONTENT, fullText);
//            data.put(Keys.MESSAGE_ENTITY_ID, message.getEntityID());
//            data.put(Keys.THREAD_ENTITY_ID, message.getThread().getEntityID());
//            data.put(Keys.MESSAGE_DATE, message.getDate().toDate().getTime());
//            data.put(Keys.MESSAGE_SENDER_ENTITY_ID, message.getSender().getEntityID());
//            data.put(Keys.MESSAGE_SENDER_NAME, message.getSender().getName());
//            data.put(Keys.MESSAGE_TYPE, message.getType());
//            data.put(Keys.MESSAGE_PAYLOAD, message.getTextString());
//            //For iOS
//            data.put(Keys.BADGE, Keys.INCREMENT);
//            data.put(Keys.ALERT, fullText);
//            // For making sound in iOS
//            data.put(Keys.SOUND, Keys.Default);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        // TODO: Check this
        //NM.push().pushToChannels(channels, data);
    }

    public Completable leaveThread (Thread thread) {
        return null;
    }

    public Completable joinThread (Thread thread) {
        return null;
    }

}
