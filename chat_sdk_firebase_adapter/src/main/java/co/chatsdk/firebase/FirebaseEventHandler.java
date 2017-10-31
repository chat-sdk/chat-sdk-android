package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import co.chatsdk.core.session.NM;
import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.Executor;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by benjaminsmiley-andrews on 10/05/2017.
 */

public class FirebaseEventHandler implements EventHandler {

    final private PublishSubject<NetworkEvent> eventSource = PublishSubject.create();

    private static FirebaseEventHandler instance;
    boolean isOn = false;
    private DisposableList disposableList = new DisposableList();

    public static FirebaseEventHandler shared() {
        if (instance == null) {
            instance = new FirebaseEventHandler();
        }
        return instance;
    }

    public void currentUserOn(final String entityID){

        if(isOn) {
            return;
        }
        isOn = true;

        final User user = DaoCore.fetchEntityWithEntityID(User.class, entityID);

        if(NM.hook() != null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put(BaseHookHandler.UserOn_User, user);
            NM.hook().executeHook(BaseHookHandler.UserOn, data);
        }

        // Remove all users from public threads
        // These may not have been cleared down when we exited so clear them down and
        // start again
        for(Thread thread : NM.thread().getThreads(ThreadType.Public)) {
            for(User u : thread.getUsers()) {
                thread.removeUser(u);
            }
        }

        final DatabaseReference threadsRef = FirebasePaths.userThreadsRef(entityID);
        ChildEventListener threadsListener = threadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(final DataSnapshot snapshot, String s, boolean hasValue) {
                if(hasValue) {
                    final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

                    thread.getModel().addUser(user);

                    // Starting to listen to thread changes.
                    disposableList.add(thread.on().doOnNext(new Consumer<Thread>() {
                        @Override
                        public void accept(Thread thread) throws Exception {
                            eventSource.onNext(NetworkEvent.threadDetailsUpdated(thread));
                        }
                    }).subscribe());

                    disposableList.add(thread.messagesOn().doOnNext(new Consumer<Message>() {
                        @Override
                        public void accept(Message message) throws Exception {
                            eventSource.onNext(NetworkEvent.messageAdded(message.getThread(), message));
                        }
                    }).subscribe());

                    disposableList.add(thread.usersOn().doOnNext(new Consumer<User>() {
                        @Override
                        public void accept(User user) throws Exception {
                            eventSource.onNext(NetworkEvent.threadUsersChanged(thread.getModel(), user));
                        }
                    }).subscribe());

                    eventSource.onNext(NetworkEvent.threadAdded(thread.getModel()));

                }
            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {
                if (hasValue) {
                    ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
                    thread.off();
                    eventSource.onNext(NetworkEvent.threadRemoved(thread.getModel()));
                }
            }
        }));
        FirebaseReferenceManager.shared().addRef(threadsRef, threadsListener);

        DatabaseReference publicThreadsRef = FirebasePaths.publicThreadsRef();
        ChildEventListener publicThreadsListener = publicThreadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

                // Starting to listen to thread changes.
                disposableList.add(thread.on().doOnNext(new Consumer<Thread>() {
                    @Override
                    public void accept(Thread thread) throws Exception {
                        eventSource.onNext(NetworkEvent.threadDetailsUpdated(thread));
                    }
                }).subscribe());

                disposableList.add(thread.messagesOn().doOnNext(new Consumer<Message>() {
                    @Override
                    public void accept(Message message) throws Exception {
                        eventSource.onNext(NetworkEvent.messageAdded(message.getThread(), message));
                    }
                }).subscribe());

                disposableList.add(thread.usersOn().doOnNext(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        eventSource.onNext(NetworkEvent.threadUsersChanged(thread.getModel(), user));
                    }
                }).subscribe());

                eventSource.onNext(NetworkEvent.threadAdded(thread.getModel()));
            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {
                ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
                thread.off();
                eventSource.onNext(NetworkEvent.threadRemoved(thread.getModel()));
            }
        }));
        FirebaseReferenceManager.shared().addRef(publicThreadsRef, publicThreadsListener);

        if (NM.push() != null) {
            NM.push().subscribeToPushChannel(user.getPushChannel());
        }

        // TODO: Check this
        DatabaseReference followersRef = FirebasePaths.userFollowersRef(entityID);
        ChildEventListener followersListener = followersRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {

                //TODO: Implement this

                //FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//
//
//
//                UserWrapper wrapper = UserWrapper.initWithModel(follower.getUser());
//                wrapper.once();
//                wrapper.metaOn();

                eventSource.onNext(NetworkEvent.followerAdded());

            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {

//                FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//                DaoCore.deleteEntity(follower);

                eventSource.onNext(NetworkEvent.followerRemoved());

            }
        }));
        FirebaseReferenceManager.shared().addRef(followersRef, followersListener);

        DatabaseReference followingRef = FirebasePaths.userFollowingRef(entityID);
        ChildEventListener followingListener = followingRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {

                // TODO: Implement this
//                FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//
//                UserWrapper wrapper = UserWrapper.initWithModel(follower.getUser());
//                wrapper.once();
//                wrapper.metaOn();

                eventSource.onNext(NetworkEvent.followingAdded());

            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {

//                FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//                DaoCore.deleteEntity(follower);

                eventSource.onNext(NetworkEvent.followingRemoved());
            }
        }));
        FirebaseReferenceManager.shared().addRef(followersRef, followingListener);

        contactsMetaOn().subscribe();
    }

    private Completable contactsMetaOn () {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                for (User contact : NM.contact().contacts()) {
                    NM.core().userOn(contact);
                }
                e.onComplete();
            }
        }).observeOn(Schedulers.single());
    }

    public void userOff(final String entityID){
        isOn = false;

        final User user = DaoCore.fetchEntityWithEntityID(User.class, entityID);

        FirebaseReferenceManager.shared().removeListener(FirebasePaths.userThreadsRef(entityID));
        FirebaseReferenceManager.shared().removeListener(FirebasePaths.publicThreadsRef());
        FirebaseReferenceManager.shared().removeListener(FirebasePaths.userFollowersRef(entityID));
        FirebaseReferenceManager.shared().removeListener(FirebasePaths.userFollowingRef(entityID));

        ThreadWrapper wrapper;
        for (Thread thread : NM.thread().getThreads(ThreadType.All))
        {
            wrapper = new ThreadWrapper(thread);

            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }

        Executor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                for (User contact : NM.contact().contacts())
                    UserWrapper.initWithModel(contact).metaOff();
            }
        });

        if (NM.push() != null) {
            NM.push().unsubscribeToPushChannel(user.getPushChannel());
        }

        disposableList.dispose();
    }

    public PublishSubject<NetworkEvent> source () {
        return eventSource;
    }

    public Observable<NetworkEvent> sourceOnMain () {
        return eventSource.observeOn(AndroidSchedulers.mainThread());
    }


}
