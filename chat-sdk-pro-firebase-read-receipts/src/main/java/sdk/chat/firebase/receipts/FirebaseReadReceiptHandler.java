package sdk.chat.firebase.receipts;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.List;
import java.util.Map;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.moderation.Permission;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.utils.Generic;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;
import sdk.guru.realtime.RXRealtime;
import sdk.guru.realtime.RealtimeReferenceManager;

public class FirebaseReadReceiptHandler implements ReadReceiptHandler {

    protected DisposableMap dm = new DisposableMap();

    public FirebaseReadReceiptHandler() {
        ChatSDK.hook().addHook(Hook.sync(data -> {
            for (Thread t: ChatSDK.db().fetchThreadsForCurrentUser()) {
                threadReadReceiptsOn(t);
            }
        }), HookEvent.DidAuthenticate);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            Object message = data.get(HookEvent.Message);
            if (message instanceof Message) {
                updateReadReceipts((Message) message);
            }
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
            if (!message.getSender().isMe()) {
                message.setIsRead(true, true, true);
            }
            if(shouldMarkReadReceipt(message)) {
                FirebaseModule.config().provider.messageWrapper(message).setReadStatus(ReadStatus.read()).subscribe(ChatSDK.events());
            }
        });
    }

    @Override
    public void markDelivered(Message message) {

    }

    private boolean shouldListenToReadReceipt(Message message) {
        return message.getSender().isMe() && readReceiptsEnabledForMessage(message) && !message.getReadStatus().is(ReadStatus.read()) && !ChatSDK.thread().roleForUser(message.getThread(), ChatSDK.currentUser()).equals(Permission.Banned);
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
        return readReceiptsEnabledForThread(message.getThread()) &&
                message.getDate().getTime() > (new Date().getTime() - FirebaseReadReceiptsModule.config().maxAge);
//        if (enabled) {
//            ReadStatus status = message.getReadStatus();
//            return !status.is(ReadStatus.read());
//        }
//        return false;
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
                        FirebaseModule.config().provider.messageWrapper(message).updateReadReceipts(map).doOnSuccess(aBoolean -> {
                            if (aBoolean) {
                                ChatSDK.events().source().accept(NetworkEvent.messageReadReceiptUpdated(message));
                            }
                        }).ignoreElement().subscribe(ChatSDK.events());
                    }

                    // If this is now read, remove the read receipt
                    if (!shouldListenToReadReceipt(message)) {
                        messageReadReceiptsOff(message);
                    }
                }
            }).ignoreElements().subscribe(ChatSDK.events());
//            realtime.addToReferenceManager();

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
