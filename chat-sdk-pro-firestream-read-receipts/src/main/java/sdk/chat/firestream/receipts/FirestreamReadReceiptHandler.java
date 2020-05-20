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

public class FirestreamReadReceiptHandler implements ReadReceiptHandler, Consumer<Throwable> {

    private DisposableMap dm = new DisposableMap();

    public FirestreamReadReceiptHandler() {

        // We want to add these listeners when we connect and remove them when we disconnect
        dm.add(Fire.stream().getConnectionEvents().subscribe(connectionEvent -> {
            if (connectionEvent.getType() == ConnectionEvent.Type.DidConnect) {
                dm.add(Fire.stream().getSendableEvents().getDeliveryReceipts().pastAndNewEvents().subscribe(event -> {
                    dm.add(handleReceipt(event.get()));
                }));
            }
            if (connectionEvent.getType() == ConnectionEvent.Type.WillDisconnect) {
                dm.disposeAll();
            }
        }));

    }

    public Disposable handleReceipt(DeliveryReceipt receipt) {
        return ChatSDK.core().getUserForEntityID(receipt.getFrom()).subscribe(user -> {
            // get the message
            Message message = ChatSDK.db().fetchEntityWithEntityID(receipt.getMessageId(), Message.class);
            if (message != null) {
                Thread thread = message.getThread();
                if (thread != null) {
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
