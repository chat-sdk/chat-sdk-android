package co.chatsdk.read_receipts;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.firebase.moderation.Permission;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.hook.Executor;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;
import sdk.guru.realtime.DocumentChange;
import sdk.guru.realtime.RXRealtime;
import sdk.guru.realtime.RealtimeEventListener;
import co.chatsdk.firebase.FirebasePaths;
import sdk.guru.realtime.RealtimeReferenceManager;
import co.chatsdk.firebase.utils.Generic;
import co.chatsdk.firebase.wrappers.MessageWrapper;

public class FirebaseReadReceiptHandler implements ReadReceiptHandler {

    protected DisposableMap dm = new DisposableMap();

    public FirebaseReadReceiptHandler() {
        ChatSDK.hook().addHook(Hook.sync(data -> {
            for (Thread t: ChatSDK.db().fetchThreadsForCurrentUser()) {
                threadReadReceiptsOn(t);
            }
        }), HookEvent.DidAuthenticate);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            Message message = (Message) data.get(HookEvent.Message);
            updateReadReceipts(message);
        }), HookEvent.MessageReceived);

        dm.add(ChatSDK.events().source().filter(NetworkEvent.filterType(EventType.ThreadUserRoleUpdated)).subscribe(event -> {
            Thread thread = event.getThread();
            if (ChatSDK.thread().isBanned(thread, event.getUser())) {
                threadReadReceiptsOff(thread);
            } else {
                threadReadReceiptsOn(thread);
            }
        }));

    }

    public void threadReadReceiptsOff(Thread thread) {
        List<Message> messages = ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), null, null, FirebaseReadReceiptsModule.config().maxMessagesPerThread);
        for(Message message : messages) {
            messageReadReceiptsOff(message);
        }
    }

    public void threadReadReceiptsOn(Thread thread) {
        if(readReceiptsEnabledForThread(thread)) {
            List<Message> messages = ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), null, null, FirebaseReadReceiptsModule.config().maxMessagesPerThread);
            for(Message message : messages) {
                this.updateReadReceipts(message);
            }
        }
    }

//    public void updateReadReceipts(Thread thread) {
//        if(readReceiptsEnabledForThread(thread)) {
//            List<Message> messages = ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), 20, null);
//            for(Message message : messages) {
//                this.updateReadReceipts(message);
//            }
//        }
//    }

    public void updateReadReceipts(Message message) {
        if(message.getSender().isMe()) {
            if(shouldListenToReadReceipt(message)) {
                messageReadReceiptsOn(message);
            }
            else {
                messageReadReceiptsOff(message);
            }
        }
    }

    public void markRead(Message message) {
        RX.computation().scheduleDirect(() -> {
            if(shouldMarkReadReceipt(message)) {
                new MessageWrapper(message).setReadStatus(ReadStatus.read()).subscribe(ChatSDK.events());
            }
        });
    }

    @Override
    public void markDelivered(Message message) {

    }

    private boolean shouldListenToReadReceipt(Message message) {
        return message.getSender().isMe() && readReceiptsEnabledForMessage(message) && !ChatSDK.thread().roleForUser(message.getThread(), ChatSDK.currentUser()).equals(Permission.Banned);
    }

    private boolean shouldMarkReadReceipt(Message message) {
        return !message.getSender().isMe() && readReceiptsEnabledForMessage(message);
    }

    /**
     * Read receipts are enabled if:
     * 1. They are enabled on the thread
     * 2. They are not too old
     * 3. The read status isn't already set to read
     * @param message
     * @return
     */
    private boolean readReceiptsEnabledForMessage(Message message) {
        boolean enabled = readReceiptsEnabledForThread(message.getThread()) &&
                message.getDate().getTime() > (new Date().getTime() - FirebaseReadReceiptsModule.config().maxAge);
        if (enabled) {
            ReadStatus status = message.getReadStatus();
            return !status.is(ReadStatus.read());
        }
        return false;
    }

    private boolean readReceiptsEnabledForThread (Thread thread) {
        return thread.typeIs(ThreadType.Private) && !ChatSDK.thread().isBanned(thread, ChatSDK.currentUser());
    }

    private void messageReadReceiptsOn(final Message message) {
        DatabaseReference ref = messageRef(message);
        if(!RealtimeReferenceManager.shared().isOn(ref)) {

            RXRealtime realtime = new RXRealtime();
            realtime.on(ref).doOnNext(change -> {
                if (change.getSnapshot().getValue() != null) {

                    Map<String, Map<String, Long>> map = change.getSnapshot().getValue(Generic.readReceiptHashMap());
                    if (map != null) {
                        if (new MessageWrapper(message).updateReadReceipts(map)) {
                            ChatSDK.events().source().onNext(NetworkEvent.messageReadReceiptUpdated(message));
                        }
                    }

                    // If this is now read, remove the read receipt
                    if (!shouldListenToReadReceipt(message)) {
                        messageReadReceiptsOff(message);
                    }
                }
            }).ignoreElements().subscribe(ChatSDK.events());
            realtime.addToReferenceManager();

        }
     }

    private void messageReadReceiptsOff(Message message) {
        DatabaseReference ref = messageRef(message);
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    private DatabaseReference messageRef(Message message) {
        return FirebasePaths.threadMessagesReadRef(message.getThread().getEntityID(), message.getEntityID());
    }

}
