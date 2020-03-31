package co.chatsdk.read_receipts;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseReferenceManager;
import co.chatsdk.firebase.utils.Generic;
import co.chatsdk.firebase.wrappers.MessageWrapper;

public class FirebaseReadReceiptHandler implements ReadReceiptHandler {

    public FirebaseReadReceiptHandler() {
        ChatSDK.hook().addHook(Hook.sync(data -> {
            for (Thread t: ChatSDK.db().fetchThreadsForCurrentUser()) {
                if(readReceiptsEnabledForThread(t)) {
                    List<Message> messages = ChatSDK.db().fetchMessagesForThreadWithID(t.getId(), 20, null);
                    for(Message message : messages) {
                        this.updateReadReceipts(message);
                    }
                }
            }
        }), HookEvent.DidAuthenticate);
    }

    public void updateReadReceipts(Thread thread) {
        if(readReceiptsEnabledForThread(thread)) {
            List<Message> messages = ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), 20, null);
            for(Message message : messages) {
                this.updateReadReceipts(message);
            }
        }
    }

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

    public void markRead (Message message) {
        if(shouldMarkReadReceipt(message)) {
            new MessageWrapper(message).setReadStatus(ReadStatus.read()).subscribe(ChatSDK.events());
        }
    }

    @Override
    public void markDelivered(Message message) {

    }

    private boolean shouldListenToReadReceipt(Message message) {
        return message.getSender().isMe() && readReceiptsEnabledForMessage(message);
    }

    private boolean shouldMarkReadReceipt (Message message) {
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
    private boolean readReceiptsEnabledForMessage (Message message) {
        boolean enabled =  readReceiptsEnabledForThread(message.getThread()) &&
                message.getDate().isAfter(new Date().getTime() - FirebaseReadReceiptsModule.config().maxAge);
        if (enabled) {
            ReadStatus status = message.getReadStatus();
            return !status.is(ReadStatus.read());
        }
        return false;
    }

    private boolean readReceiptsEnabledForThread (Thread thread) {
        return thread.typeIs(ThreadType.Private);
    }

    private void messageReadReceiptsOn(final Message message) {
        DatabaseReference ref = messageRef(message);
        if(!FirebaseReferenceManager.shared().isOn(ref)) {
            ValueEventListener listener = ref.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue && snapshot.getValue() instanceof HashMap) {
                    HashMap<String, HashMap<String, Long>> map = snapshot.getValue(Generic.readReceiptHashMap());
                    if (map != null) {
                        new MessageWrapper(message).updateReadReceipts(map);
                    }

                    if (!message.readStatusForUser(ChatSDK.currentUser()).is(ReadStatus.none())) {
                        ChatSDK.events().source().onNext(NetworkEvent.messageReadReceiptUpdated(message));
                    }

                    // If this is now read, remove the read receipt
                    if (!shouldListenToReadReceipt(message)) {
                        messageReadReceiptsOff(message);
                    }
                }

            }));
            FirebaseReferenceManager.shared().addRef(ref, listener);
        }
     }

    private void messageReadReceiptsOff (Message message) {
        DatabaseReference ref = messageRef(message);
        if (FirebaseReferenceManager.shared().isOn(ref)) {
            FirebaseReferenceManager.shared().removeListeners(ref);
        }
    }

    private DatabaseReference messageRef (Message message) {
        return FirebasePaths.threadMessagesReadRef(message.getThread().getEntityID(), message.getEntityID());
    }

}
