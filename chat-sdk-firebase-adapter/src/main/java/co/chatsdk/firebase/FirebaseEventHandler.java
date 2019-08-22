package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

import co.chatsdk.core.base.AbstractEventHandler;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.CrashReportingObserver;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by benjaminsmiley-andrews on 10/05/2017.
 */

public class FirebaseEventHandler extends AbstractEventHandler {

    protected boolean isOn = false;

    public void impl_currentUserOn(final String entityID){

        if(isOn) {
            return;
        }
        isOn = true;

        final User user = DaoCore.fetchEntityWithEntityID(User.class, entityID);

        if(ChatSDK.hook() != null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put(HookEvent.User, user);
            ChatSDK.hook().executeHook(HookEvent.UserOn, data).subscribe(new CrashReportingCompletableObserver());;
        }

        threadsOn(user);
        publicThreadsOn(user);
        contactsOn(user);

        if (ChatSDK.push() != null) {
            ChatSDK.push().subscribeToPushChannel(user.getPushChannel());
        }
    }

    protected void threadsOn (User user) {
        String entityID = user.getEntityID();

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
    }

    protected void publicThreadsOn (User user) {
        String entityID = user.getEntityID();
        // Remove all users from public threads
        // These may not have been cleared down when we exited so clear them down and
        // start again
//        for(Thread thread : ChatSDK.thread().getThreads(ThreadType.Public)) {
//            for(User u : thread.getUsers()) {
//                thread.removeUser(u);
//            }
//        }

        DatabaseReference publicThreadsRef = FirebasePaths.publicThreadsRef();
        ChildEventListener publicThreadsListener = publicThreadsRef.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {
            final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());



            // Make sure that we're not in the thread
            // there's an edge case where the user could kill the app and remain
            // a member of a public thread
            ChatSDK.thread().removeUsersFromThread(thread.getModel(), user).subscribe(new CrashReportingCompletableObserver());

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
    }

    protected void contactsOn (User user) {

        DatabaseReference ref = FirebasePaths.userContactsRef(user.getEntityID());

        ref.addChildEventListener(new FirebaseEventListener().onChildAdded((snapshot, s, hasValue) -> {
            if (hasValue) {
                User contact = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, snapshot.getKey());
                Object value = snapshot.getValue();
                if (value instanceof HashMap) {
                    Object type = ((HashMap) value).get(Keys.Type);
                    if (type instanceof Long) {
                        ConnectionType connectionType = ConnectionType.values()[((Long) type).intValue()];
                        ChatSDK.contact().addContactLocal(contact, connectionType);
                        disposableList.add(ChatSDK.core().userOn(contact).subscribe(() -> {
                            eventSource.onNext(NetworkEvent.contactAdded(contact));
                            }, eventSource::onError));
                    }
                }
            }
        }));

        ref.addChildEventListener(new FirebaseEventListener().onChildRemoved((snapshot, hasValue) -> {
            if (hasValue) {
                User contact = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, snapshot.getKey());
                Object value = snapshot.getValue();
                if (value instanceof HashMap) {
                    Object type = ((HashMap) value).get(Keys.Type);
                    if (type instanceof Long) {
                        ConnectionType connectionType = ConnectionType.values()[((Long) type).intValue()];
                        ChatSDK.contact().deleteContactLocal(contact, connectionType);
                        eventSource.onNext(NetworkEvent.contactDeleted(contact));
                    }
                }
            }
        }));

    }

    public void impl_currentUserOff(final String entityID){
        isOn = false;

        final User user = DaoCore.fetchEntityWithEntityID(User.class, entityID);

        threadsOff(user);
        publicThreadsOff(user);
        contactsOff(user);

        if (ChatSDK.push() != null) {
            ChatSDK.push().unsubscribeToPushChannel(user.getPushChannel());
        }

        disposableList.dispose();
    }

    protected void threadsOff (User user) {
        String entityID = user.getEntityID();
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.userThreadsRef(entityID));
        for (Thread thread : ChatSDK.thread().getThreads(ThreadType.Private)) {
            ThreadWrapper wrapper = new ThreadWrapper(thread);
            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }
    }

    protected void publicThreadsOff (User user) {
        String entityID = user.getEntityID();
        FirebaseReferenceManager.shared().removeListeners(FirebasePaths.publicThreadsRef());
        for (Thread thread : ChatSDK.thread().getThreads(ThreadType.Public)) {
            ThreadWrapper wrapper = new ThreadWrapper(thread);
            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }
    }

    protected void contactsOff (User user) {
        String entityID = user.getEntityID();
        for (User contact : ChatSDK.contact().contacts()) {
            UserWrapper.initWithModel(contact).metaOff();
        }
    }

}
