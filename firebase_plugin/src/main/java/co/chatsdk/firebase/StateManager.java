package co.chatsdk.firebase;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseInterface;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebasePaths;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BThreadWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.FollowerLink;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.utils.Executor;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.ReplaySubject;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 10/05/2017.
 */

public class StateManager implements EventHandler {

    private static final boolean DEBUG = Debug.StateManager;

    private ChildEventListener userThreadsRefListener;
    private ChildEventListener publicThreadsRefListener;
    private ChildEventListener userFollowersListener;
    private ChildEventListener userFollowingListener;

    final private ReplaySubject<NetworkEvent> eventSource = ReplaySubject.create();

    private static StateManager instance;

    public static StateManager shared() {
        if (instance == null) {
            instance = new StateManager();
        }
        return instance;
    }

    public void userOn(final String entityID){

        if (DEBUG) Timber.v("userOn, EntityID: %s", entityID);

        final BUser user = DaoCore.fetchEntityWithEntityID(BUser.class, entityID);

        DatabaseReference threadsRef = FirebasePaths.userThreadsRef(entityID);
        userThreadsRefListener = threadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(final DataSnapshot snapshot, String s, boolean hasValue) {
                if(hasValue) {
                    BThreadWrapper thread = new BThreadWrapper(snapshot.getKey());

                    if(!thread.getModel().hasUser(user)) {
                        DaoCore.connectUserAndThread(user, thread.getModel());
                    }

                    // Starting to listen to thread changes.
                    thread.on();
                    thread.messagesOn();
                    thread.usersOn();

                    eventSource.onNext(NetworkEvent.privateThreadAdded());

                }
            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {
                if (hasValue) {
                    new BThreadWrapper(snapshot.getKey()).off();
                    eventSource.onNext(NetworkEvent.privateThreadRemoved());
                }
            }
        }));

        DatabaseReference publicThreadsRef = FirebasePaths.publicThreadsRef();
        publicThreadsRefListener = publicThreadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                BThreadWrapper thread = new BThreadWrapper(snapshot.getKey());

                // Make sure that we're not in the thread
                // there's an edge case where the user could kill the app and remain
                // a member of a public thread
                thread.removeUser(BUserWrapper.initWithModel(user));

                // Starting to listen to thread changes.
                thread.on();
                thread.messagesOn();
                thread.usersOn();

                eventSource.onNext(NetworkEvent.publicThreadAdded());
            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {
                new BThreadWrapper(snapshot.getKey()).off();
                eventSource.onNext(NetworkEvent.publicThreadRemoved());
            }
        }));

        if (NetworkManager.shared().a.push != null) {
            NetworkManager.shared().a.push.subscribeToPushChannel(user.getPushChannel());
        }

        // TODO: Check this
        DatabaseReference userFollowersRef = FirebasePaths.userFollowersRef(entityID);
        userFollowersListener = userFollowersRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);

                BUserWrapper wrapper = BUserWrapper.initWithModel(follower.getBUser());
                wrapper.once();
                wrapper.metaOn();

                eventSource.onNext(NetworkEvent.followerAdded());

            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {

                FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
                DaoCore.deleteEntity(follower);

                eventSource.onNext(NetworkEvent.followerRemoved());

            }
        }));

        DatabaseReference userFollowingRef = FirebasePaths.userFollowingRef(entityID);
        userFollowingListener = userFollowingRef.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
            @Override
            public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {

                FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);

                BUserWrapper wrapper = BUserWrapper.initWithModel(follower.getBUser());
                wrapper.once();
                wrapper.metaOn();

                eventSource.onNext(NetworkEvent.followingAdded());

            }
        }).onChildRemoved(new FirebaseEventListener.Removed() {
            @Override
            public void trigger(DataSnapshot snapshot, boolean hasValue) {

                FollowerLink follower = (FollowerLink) BFirebaseInterface.objectFromSnapshot(snapshot);
                DaoCore.deleteEntity(follower);

                eventSource.onNext(NetworkEvent.followingRemoved());

            }
        }));

        Executor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                for (BUser contact : user.getContacts())
                    BUserWrapper.initWithModel(contact).metaOn();
            }
        });

    }

    public void broadcast (Intent intent) {
        LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);
    }

    public void userOff(final String entityID){
        if (DEBUG) Timber.v("userOff, EntityID: $s", entityID);

        final BUser user = DaoCore.fetchEntityWithEntityID(BUser.class, entityID);

        FirebasePaths.userThreadsRef(entityID).removeEventListener(userThreadsRefListener);
        FirebasePaths.publicThreadsRef().removeEventListener(publicThreadsRefListener);
        FirebasePaths.userFollowersRef(entityID).removeEventListener(userFollowersListener);
        FirebasePaths.userFollowingRef(entityID).removeEventListener(userFollowingListener);

        BThreadWrapper wrapper;
        for (BThread thread : BNetworkManager.getThreadsInterface().getThreads())
        {
            wrapper = new BThreadWrapper(thread);

            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }

        Executor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                for (BUser contact : user.getContacts())
                    BUserWrapper.initWithModel(contact).metaOff();
            }
        });

        if (NetworkManager.shared().a.push != null) {
            NetworkManager.shared().a.push.unsubscribeToPushChannel(user.getPushChannel());
        }


    }

    public ReplaySubject<NetworkEvent> source () {
        return eventSource;
    }

}
