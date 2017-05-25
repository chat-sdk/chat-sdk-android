package co.chatsdk.firebase;

import com.braunster.chatsdk.network.BNetworkManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import co.chatsdk.core.NM;
import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.Executor;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.ReplaySubject;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 10/05/2017.
 */

public class FirebaseStateManager implements EventHandler {

    private static final boolean DEBUG = Debug.StateManager;

    final private ReplaySubject<NetworkEvent> eventSource = ReplaySubject.create();

    private static FirebaseStateManager instance;
    boolean isOn = false;

    public static FirebaseStateManager shared() {
        if (instance == null) {
            instance = new FirebaseStateManager();
        }
        return instance;
    }

    public void userOn(final String entityID){

        if(isOn) {
            return;
        }
        isOn = true;

        if (DEBUG) Timber.v("userOn, EntityID: %s", entityID);

        final BUser user = DaoCore.fetchEntityWithEntityID(BUser.class, entityID);

        final DatabaseReference threadsRef = FirebasePaths.userThreadsRef(entityID);
        ChildEventListener threadsListener = threadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(final DataSnapshot snapshot, String s, boolean hasValue) {
                if(hasValue) {
                    final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

                    if(!thread.getModel().hasUser(user)) {
                        DaoCore.connectUserAndThread(user, thread.getModel());
                    }

                    // Starting to listen to thread changes.
                    thread.on().doOnNext(new Consumer<BThread>() {
                        @Override
                        public void accept(BThread thread) throws Exception {
                            eventSource.onNext(NetworkEvent.threadDetailsUpdated(thread));
                        }
                    });

                    thread.messagesOn().doOnNext(new Consumer<BMessage>() {
                        @Override
                        public void accept(BMessage message) throws Exception {
                            eventSource.onNext(NetworkEvent.messageAdded(message.getThread(), message));
                        }
                    });

                    thread.usersOn().doOnNext(new Consumer<BUser>() {
                        @Override
                        public void accept(BUser user) throws Exception {
                            eventSource.onNext(NetworkEvent.threadUsersChanged(thread.getModel(), user));
                        }
                    });

                    eventSource.onNext(NetworkEvent.privateThreadAdded(thread.getModel()));

                }
            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {
                if (hasValue) {
                    ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
                    thread.off();
                    eventSource.onNext(NetworkEvent.privateThreadRemoved(thread.getModel()));
                }
            }
        }));
        FirebaseReferenceManager.shared().addRef(threadsRef, threadsListener);

        DatabaseReference publicThreadsRef = FirebasePaths.publicThreadsRef();
        ChildEventListener publicThreadsListener = publicThreadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

                // Make sure that we're not in the thread
                // there's an edge case where the user could kill the app and remain
                // a member of a public thread
                thread.removeUser(UserWrapper.initWithModel(user));

                // Starting to listen to thread changes.
                thread.on().doOnNext(new Consumer<BThread>() {
                    @Override
                    public void accept(BThread thread) throws Exception {
                        eventSource.onNext(NetworkEvent.threadDetailsUpdated(thread));
                    }
                }).subscribe();

                thread.messagesOn().doOnNext(new Consumer<BMessage>() {
                    @Override
                    public void accept(BMessage message) throws Exception {
                        eventSource.onNext(NetworkEvent.messageAdded(message.getThread(), message));
                    }
                }).subscribe();

                thread.usersOn().doOnNext(new Consumer<BUser>() {
                    @Override
                    public void accept(BUser user) throws Exception {
                        eventSource.onNext(NetworkEvent.threadUsersChanged(thread.getModel(), user));
                    }
                }).subscribe();

                eventSource.onNext(NetworkEvent.publicThreadAdded(thread.getModel()));
            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {
                ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
                thread.off();
                eventSource.onNext(NetworkEvent.publicThreadRemoved(thread.getModel()));
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

                //FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
//
//
//
//                UserWrapper wrapper = UserWrapper.initWithModel(follower.getBUser());
//                wrapper.once();
//                wrapper.metaOn();

                eventSource.onNext(NetworkEvent.followerAdded());

            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {

//                FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
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
//                FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
//
//                UserWrapper wrapper = UserWrapper.initWithModel(follower.getBUser());
//                wrapper.once();
//                wrapper.metaOn();

                eventSource.onNext(NetworkEvent.followingAdded());

            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {

//                FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
//                DaoCore.deleteEntity(follower);

                eventSource.onNext(NetworkEvent.followingRemoved());
            }
        }));
        FirebaseReferenceManager.shared().addRef(followersRef, followingListener);

        Executor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                for (BUser contact : user.getContacts()) {
                    UserWrapper.initWithModel(contact).metaOn().subscribe(new Consumer<BUser>() {
                        @Override
                        public void accept(BUser user) throws Exception {
                            eventSource.onNext(NetworkEvent.userMetaUpdated(user));
                        }
                    });
                }
            }
        });

    }

    public void userOff(final String entityID){
        isOn = false;
        if (DEBUG) Timber.v("userOff, EntityID: $s", entityID);

        final BUser user = DaoCore.fetchEntityWithEntityID(BUser.class, entityID);

        FirebaseReferenceManager.shared().removeListener(FirebasePaths.userThreadsRef(entityID));
        FirebaseReferenceManager.shared().removeListener(FirebasePaths.publicThreadsRef());
        FirebaseReferenceManager.shared().removeListener(FirebasePaths.userFollowersRef(entityID));
        FirebaseReferenceManager.shared().removeListener(FirebasePaths.userFollowingRef(entityID));

        ThreadWrapper wrapper;
        for (BThread thread : NM.thread().getThreads(ThreadType.All))
        {
            wrapper = new ThreadWrapper(thread);

            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }

        Executor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                for (BUser contact : user.getContacts())
                    UserWrapper.initWithModel(contact).metaOff();
            }
        });

        if (NM.push() != null) {
            NM.push().unsubscribeToPushChannel(user.getPushChannel());
        }
    }

    public ReplaySubject<NetworkEvent> source () {
        return eventSource;
    }

}
