package sdk.chat.firebase.adapter;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import org.pmw.tinylog.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import sdk.chat.core.base.AbstractEventHandler;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.firebase.adapter.moderation.Permission;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.utils.Generic;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;
import sdk.guru.common.EventType;
import sdk.guru.realtime.RXRealtime;
import sdk.guru.realtime.RealtimeEventListener;
import sdk.guru.realtime.RealtimeReferenceManager;

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
        }
    }

    protected void threadsOn(User user) {
        String entityID = user.getEntityID();

        final DatabaseReference threadsRef = FirebasePaths.userThreadsRef(entityID);

        new RXRealtime().childOn(threadsRef).flatMapCompletable(change -> {
            final ThreadWrapper thread = new ThreadWrapper(change.getSnapshot().getKey());
            if (change.getType() == EventType.Added && !thread.getModel().typeIs(ThreadType.Public)) {

                long now = new Date().getTime();
                if (ChatSDK.config().privateChatRoomLifetimeMinutes == 0 || thread.getModel().getCreationDate() == null || (now - thread.getModel().getCreationDate().getTime()) < TimeUnit.MINUTES.toMillis(ChatSDK.config().privateChatRoomLifetimeMinutes)) {

                    Logger.debug("Thread added: " + change.getSnapshot().getKey());

                    if (thread.getModel().typeIs(ThreadType.Group)) {
                        String permission = thread.getModel().getPermission(user.getEntityID());
                        if (permission != null && permission.equals(Permission.None)) {
                            ChatSDK.thread().sendLocalSystemMessage(ChatSDK.getString(R.string.you_were_added_to_the_thread), thread.getModel());
                        }
                    }

                    thread.getModel().addUser(user, false);

                    thread.on().subscribe(this);

                }

            }
            if (change.getType() == EventType.Removed) {
                Logger.debug("Thread removed: " + change.getSnapshot().getKey());

                if (thread.getModel().typeIs(ThreadType.Group)) {
                    ChatSDK.thread().sendLocalSystemMessage(ChatSDK.getString(R.string.you_were_removed_from_the_thread), thread.getModel());
                }
                thread.getModel().setPermission(user.getEntityID(), Permission.None, true, false);
                thread.off();
            }
            return Completable.complete();

        }).subscribe(this);
    }

    protected void publicThreadsOn(User user) {
        if (!FirebaseModule.config().disablePublicThreads) {
            DatabaseReference publicThreadsRef = FirebasePaths.publicThreadsRef();

            Query query = publicThreadsRef.orderByChild(Keys.CreationDate);

            if (ChatSDK.config().publicChatRoomLifetimeMinutes != 0) {
                double loadRoomsSince = new Date().getTime() - TimeUnit.MINUTES.toMillis(ChatSDK.config().publicChatRoomLifetimeMinutes);
                query = query.startAt(loadRoomsSince);
            }

            ChildEventListener publicThreadsListener = query.addChildEventListener(new RealtimeEventListener().onChildAdded((snapshot, s, hasValue) -> {
                final ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());

                thread.on().subscribe(this);

            }).onChildRemoved((snapshot, hasValue) -> {
                ThreadWrapper thread = new ThreadWrapper(snapshot.getKey());
                thread.off();
            }));
            RealtimeReferenceManager.shared().addRef(publicThreadsRef, publicThreadsListener);
        }
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
        }

        dm.disposeAll();
    }

    protected void threadsOff (User user) {
        String entityID = user.getEntityID();
        RealtimeReferenceManager.shared().removeListeners(FirebasePaths.userThreadsRef(entityID));
        for (Thread thread : ChatSDK.thread().getThreads(ThreadType.Private)) {
            ThreadWrapper wrapper = new ThreadWrapper(thread);
            wrapper.off();
        }
    }

    protected void publicThreadsOff (User user) {
        if (!FirebaseModule.config().disablePublicThreads) {
            RealtimeReferenceManager.shared().removeListeners(FirebasePaths.publicThreadsRef());
            for (Thread thread : ChatSDK.thread().getThreads(ThreadType.Public)) {
                ThreadWrapper wrapper = new ThreadWrapper(thread);
                wrapper.off();
            }
        }
    }

    protected void contactsOff (User user) {
        for (User contact : ChatSDK.contact().contacts()) {
            UserWrapper.initWithModel(contact).metaOff();
        }
    }

}
