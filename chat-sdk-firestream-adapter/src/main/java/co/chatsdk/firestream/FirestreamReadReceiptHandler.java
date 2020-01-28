package co.chatsdk.firestream;

import org.joda.time.DateTime;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import firestream.chat.interfaces.IChat;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.events.EventType;
import firestream.chat.firebase.rx.DisposableMap;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.namespace.Fire;
import firestream.chat.types.DeliveryReceiptType;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class FirestreamReadReceiptHandler implements ReadReceiptHandler, Consumer<Throwable> {

    private DisposableMap dm = new DisposableMap();

    public FirestreamReadReceiptHandler() {

        // We want to add these listeners when we connect and remove them when we disconnect
        Disposable d = Fire.stream().getConnectionEvents().subscribe(connectionEvent -> {
            if (connectionEvent.getType() == ConnectionEvent.Type.DidConnect) {

                dm.add(Fire.stream().getSendableEvents().getDeliveryReceipts().pastAndNewEvents().subscribe(event -> {
                    dm.add(handleReceipt(event.get().getFrom(), event.get()));
                }));

                dm.add(Fire.stream().getChatEvents().pastAndNewEvents().subscribe(chatEvent -> {
                    IChat chat = chatEvent.get();
                    if (chatEvent.typeIs(EventType.Added)) {
                        chat.manage(chat.getSendableEvents().getDeliveryReceipts().pastAndNewEvents().subscribe(event -> {
                            chat.manage(handleReceipt(chat.getId(), event.get()));
                        }));
                    }
                }));
            }
            if (connectionEvent.getType() == ConnectionEvent.Type.WillDisconnect) {
                dm.disposeAll();
            }
        });

    }

    public Disposable handleReceipt(String threadEntityID, DeliveryReceipt receipt) {
        return ChatSDK.core().getUserForEntityID(receipt.getFrom()).subscribe(user -> {
            Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
            if (thread != null) {
                // Get the text
                Message message = thread.getMessageWithEntityID(receipt.getMessageId());
                if (message != null) {
                    if (receipt.getDeliveryReceiptType().equals(DeliveryReceiptType.read())) {
                        message.setUserReadStatus(user, ReadStatus.read(), new DateTime());
                    } else {
                        message.setUserReadStatus(user, ReadStatus.delivered(), new DateTime());
                    }
                }
            }
        });
    }

    @Override
    public void updateReadReceipts(Thread thread) {

    }

    @Override
    public void updateReadReceipts(Message message) {

    }

    @Override
    public void markRead(Message message) {
        Thread thread = message.getThread();

        if (thread.typeIs(ThreadType.Private1to1)) {
            User otherUser = thread.otherUser();
            dm.add(Fire.stream().markRead(otherUser.getEntityID(), message.getEntityID()).subscribe(() -> {}, this));
        }
        if (thread.typeIs(ThreadType.PrivateGroup)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                chat.manage(chat.markRead(message.getEntityID()).subscribe(() -> {}, this));
            }
        }
    }
    @Override
    public void accept(Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }
}
