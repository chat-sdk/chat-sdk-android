package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.CrashReportingObserver;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
        ChildEventListener threadsListener = threadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {
            if(hasValue) {
                final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

                thread.getModel().addUser(user);

                // Starting to listen to thread changes.
                thread.on().doOnNext(thread14 -> eventSource.onNext(NetworkEvent.threadDetailsUpdated(thread14))).subscribe(new CrashReportingObserver<>(disposableList));
                thread.lastMessageOn().doOnNext(thread13 -> eventSource.onNext(NetworkEvent.threadLastMessageUpdated(thread13))).subscribe(new CrashReportingObserver<>(disposableList));
                thread.messagesOn().doOnNext(message -> eventSource.onNext(NetworkEvent.messageAdded(message.getThread(), message))).subscribe(new CrashReportingObserver<>(disposableList));
                thread.messageRemovedOn().doOnNext(message -> eventSource.onNext(NetworkEvent.messageRemoved(message.getThread(), message))).subscribe(new CrashReportingObserver<>(disposableList));
                thread.usersOn().doOnNext(user12 -> eventSource.onNext(NetworkEvent.threadUsersChanged(thread.getModel(), user12))).subscribe(new CrashReportingObserver<>(disposableList));
                thread.metaOn().doOnNext(thread1 -> eventSource.onNext(NetworkEvent.threadMetaUpdated(thread.getModel()))).subscribe(new CrashReportingObserver<>(disposableList));

                eventSource.onNext(NetworkEvent.threadAdded(thread.getModel()));

            }
        }).onChildRemoved((snapshot, hasValue) -> {
            if (hasValue) {
                ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
                thread.off();
                eventSource.onNext(NetworkEvent.threadRemoved(thread.getModel()));
            }
        }));
        FirebaseReferenceManager.shared().addRef(threadsRef, threadsListener);

        DatabaseReference publicThreadsRef = FirebasePaths.publicThreadsRef();
        ChildEventListener publicThreadsListener = publicThreadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {
            final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

            // Starting to listen to thread changes.
            thread.on().doOnNext(thread12 -> eventSource.onNext(NetworkEvent.threadDetailsUpdated(thread12))).subscribe(new CrashReportingObserver<>(disposableList));
            thread.lastMessageOn().doOnNext(thread15 -> eventSource.onNext(NetworkEvent.threadLastMessageUpdated(thread15))).subscribe(new CrashReportingObserver<>(disposableList));
            thread.messagesOn().doOnNext(message -> eventSource.onNext(NetworkEvent.messageAdded(message.getThread(), message))).subscribe(new CrashReportingObserver<>(disposableList));
            thread.messageRemovedOn().doOnNext(message -> eventSource.onNext(NetworkEvent.messageRemoved(message.getThread(), message))).subscribe(new CrashReportingObserver<>(disposableList));
            thread.usersOn().doOnNext(user1 -> eventSource.onNext(NetworkEvent.threadUsersChanged(thread.getModel(), user1))).subscribe(new CrashReportingObserver<>(disposableList));

            eventSource.onNext(NetworkEvent.threadAdded(thread.getModel()));
        }).onChildRemoved((snapshot, hasValue) -> {
            ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
            thread.off();
            eventSource.onNext(NetworkEvent.threadRemoved(thread.getModel()));
        }));
        FirebaseReferenceManager.shared().addRef(publicThreadsRef, publicThreadsListener);

        if (NM.push() != null) {
            NM.push().subscribeToPushChannel(user.getPushChannel());
        }

        // TODO: Check this
        DatabaseReference followersRef = FirebasePaths.userFollowersRef(entityID);
        ChildEventListener followersListener = followersRef.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {

            //TODO: Implement this

            //FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//
//
//
//                UserWrapper wrapper = UserWrapper.initWithModel(follower.getUser());
//                wrapper.once();
//                wrapper.metaOn();

            eventSource.onNext(NetworkEvent.followerAdded());

        }).onChildRemoved((snapshot, hasValue) -> {

//                FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//                DaoCore.deleteEntity(follower);

            eventSource.onNext(NetworkEvent.followerRemoved());

        }));
        FirebaseReferenceManager.shared().addRef(followersRef, followersListener);

        DatabaseReference followingRef = FirebasePaths.userFollowingRef(entityID);
        ChildEventListener followingListener = followingRef.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {

            // TODO: Implement this
//                FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//
//                UserWrapper wrapper = UserWrapper.initWithModel(follower.getUser());
//                wrapper.once();
//                wrapper.metaOn();

            eventSource.onNext(NetworkEvent.followingAdded());

        }).onChildRemoved((snapshot, hasValue) -> {

//                FollowerLink follower = (FollowerLink) FirebaseInterface.objectFromSnapshot(snapshot);
//                DaoCore.deleteEntity(follower);

            eventSource.onNext(NetworkEvent.followingRemoved());
        }));
        FirebaseReferenceManager.shared().addRef(followersRef, followingListener);

        contactsMetaOn().subscribe(new CrashReportingCompletableObserver(disposableList));
    }

    private Completable contactsMetaOn () {
        return Completable.create(e -> {
            for (User contact : NM.contact().contacts()) {
                NM.core().userOn(contact);
            }
            e.onComplete();
        }).subscribeOn(Schedulers.single());
    }

    public void userOff(final String entityID){
        isOn = false;

        final User user = DaoCore.fetchEntityWithEntityID(User.class, entityID);

        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.userThreadsRef(entityID));
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.publicThreadsRef());
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.userFollowersRef(entityID));
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.userFollowingRef(entityID));

        ThreadWrapper wrapper;
        for (Thread thread : NM.thread().getThreads(ThreadType.All))
        {
            wrapper = new ThreadWrapper(thread);

            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }

        for (User contact : NM.contact().contacts()) {
            UserWrapper.initWithModel(contact).metaOff();
        }

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
