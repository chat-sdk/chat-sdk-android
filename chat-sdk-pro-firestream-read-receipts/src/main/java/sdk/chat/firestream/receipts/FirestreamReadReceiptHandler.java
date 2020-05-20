package sdk.chat.firestream.receipts;


import java.util.Date;

import firestream.chat.events.ConnectionEvent;
import firestream.chat.interfaces.IChat;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.namespace.Fire;
import firestream.chat.types.DeliveryReceiptType;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.EventType;

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
                        message.setUserReadStatus(user, ReadStatus.read(), new Date());
                    } else {
                        message.setUserReadStatus(user, ReadStatus.delivered(), new Date());
                    }
                }
            }
        });
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
            chat.manage(Fire.stream().markRead(message.getSender().getEntityID(), message.getEntityID()).subscribe(() -> {}, this));
        }
    }

    @Override
    public void markDelivered(Message message) {

    }

    @Override
    public void accept(Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }
}
