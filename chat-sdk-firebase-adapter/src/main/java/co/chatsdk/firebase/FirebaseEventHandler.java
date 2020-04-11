package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import sdk.chat.core.base.AbstractEventHandler;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.utils.Generic;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import sdk.guru.common.EventType;
import sdk.guru.realtime.RealtimeEventListener;
import sdk.guru.realtime.RealtimeReferenceManager;
import sdk.guru.realtime.RXRealtime;

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

        if (user != null) {
            if(ChatSDK.hook() != null) {
                HashMap<String, Object> data = new HashMap<>();
                data.put(HookEvent.User, user);
                ChatSDK.hook().executeHook(HookEvent.UserOn, data).doOnError(this).subscribe(ChatSDK.events());
            }

            threadsOn(user);
            publicThreadsOn(user);
            contactsOn(user);

            if (ChatSDK.push() != null) {
                ChatSDK.push().subscribeToPushChannel(user.getPushChannel());
            }
        }
    }

    protected void threadsOn (User user) {
        String entityID = user.getEntityID();

        final DatabaseReference threadsRef = FirebasePaths.userThreadsRef(entityID);

        new RXRealtime().childOn(threadsRef).flatMapCompletable(change -> {
            final ThreadWrapper thread = new ThreadWrapper(change.getSnapshot().getKey());
            if (change.getType() == EventType.Added && !thread.getModel().typeIs(ThreadType.Public)) {
                thread.getModel().addUser(user);
                threadWrapperOn(thread);
                eventSource.onNext(NetworkEvent.threadAdded(thread.getModel()));
            }
            if (change.getType() == EventType.Removed) {
                thread.off();
                return thread.deleteThread().doOnComplete(() -> {
                    eventSource.onNext(NetworkEvent.threadRemoved(thread.getModel()));
                });
            }
            return Completable.complete();

        }).subscribe(this);
    }

    protected void publicThreadsOn (User user) {
        if (!FirebaseModule.config().disablePublicThreads) {
            DatabaseReference publicThreadsRef = FirebasePaths.publicThreadsRef();

            Query query = publicThreadsRef.orderByChild(Keys.CreationDate);

            if (ChatSDK.config().publicChatRoomLifetimeMinutes != 0) {
                double loadRoomsSince = new Date().getTime() - TimeUnit.MINUTES.toMillis(ChatSDK.config().publicChatRoomLifetimeMinutes);
                query = query.startAt(loadRoomsSince);
            }

            ChildEventListener publicThreadsListener = query.addChildEventListener(new RealtimeEventListener().onChildAdded((snapshot, s, hasValue) -> {
                final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

                // Make sure that we're not in the thread
                // there's an edge case where the user could kill the app and remain
                // a member of a public thread
                if (!ChatSDK.config().publicChatAutoSubscriptionEnabled) {
                    ChatSDK.thread().removeUsersFromThread(thread.getModel(), user).subscribe(ChatSDK.events());
                }

                threadWrapperOn(thread);

                eventSource.onNext(NetworkEvent.threadAdded(thread.getModel()));

            }).onChildRemoved((snapshot, hasValue) -> {
                ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
                thread.off();
                eventSource.onNext(NetworkEvent.threadRemoved(thread.getModel()));
            }));
            RealtimeReferenceManager.shared().addRef(publicThreadsRef, publicThreadsListener);
        }
    }

    protected void threadWrapperOn(ThreadWrapper thread) {
        // Starting to listen to thread changes.
       Completable.merge(Arrays.asList(
               thread.on().ignoreElements(),
               thread.metaOn().ignoreElements(),
               thread.messagesOn().ignoreElements(),
               thread.messageRemovedOn().ignoreElements(),
               thread.usersOn().ignoreElements()
       )).subscribe(this);
    }

    protected void contactsOn (User user) {

        DatabaseReference ref = FirebasePaths.userContactsRef(user.getEntityID());

        ref.addChildEventListener(new RealtimeEventListener().onChildAdded((snapshot, s, hasValue) -> {
            if (hasValue) {
                User contact = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, snapshot.getKey());

                HashMap<String, Long> data = snapshot.getValue(Generic.contactType());
                if (data != null) {
                    Long type = data.get(Keys.Type);
                    if (type != null) {
                        ConnectionType connectionType = ConnectionType.values()[type.intValue()];
                        dm.add(ChatSDK.contact().addContactLocal(contact, connectionType).subscribe());
                    }
                }
            }
        }));

        ref.addChildEventListener(new RealtimeEventListener().onChildRemoved((snapshot, hasValue) -> {
            if (hasValue) {
                User contact = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, snapshot.getKey());

                HashMap<String, Long> data = snapshot.getValue(Generic.contactType());
                if (data != null) {
                    Long type = data.get(Keys.Type);
                    if (type != null) {
                        ConnectionType connectionType = ConnectionType.values()[type.intValue()];
                        ChatSDK.contact().deleteContactLocal(contact, connectionType);
                    }
                }
            }
        }));

    }

    public void impl_currentUserOff(final String entityID){
        isOn = false;

        final User user = DaoCore.fetchEntityWithEntityID(User.class, entityID);

        if (user != null) {
            threadsOff(user);
            publicThreadsOff(user);
            contactsOff(user);
            if (ChatSDK.push() != null) {
                ChatSDK.push().unsubscribeToPushChannel(user.getPushChannel());
            }
        }

        dm.disposeAll();
    }

    protected void threadsOff (User user) {
        String entityID = user.getEntityID();
        RealtimeReferenceManager.shared().removeListeners(FirebasePaths.userThreadsRef(entityID));
        for (Thread thread : ChatSDK.thread().getThreads(ThreadType.Private)) {
            ThreadWrapper wrapper = new ThreadWrapper(thread);
            wrapper.off();
            wrapper.messagesOff();
            wrapper.usersOff();
        }
    }

    protected void publicThreadsOff (User user) {
        if (!FirebaseModule.config().disablePublicThreads) {
            RealtimeReferenceManager.shared().removeListeners(FirebasePaths.publicThreadsRef());
            for (Thread thread : ChatSDK.thread().getThreads(ThreadType.Public)) {
                ThreadWrapper wrapper = new ThreadWrapper(thread);
                wrapper.off();
                wrapper.messagesOff();
                wrapper.usersOff();
            }
        }
    }

    protected void contactsOff (User user) {
        for (User contact : ChatSDK.contact().contacts()) {
            UserWrapper.initWithModel(contact).metaOff();
        }
    }

}
