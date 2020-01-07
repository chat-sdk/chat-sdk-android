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

public class FirestreamReadReceiptHandler implements ReadReceiptHandler {

    private DisposableMap dm = new DisposableMap();

    public FirestreamReadReceiptHandler() {

        // We want to add these listeners when we connect and remove them when we disconnect
        Disposable d = Fire.Stream.getConnectionEvents().subscribe(connectionEvent -> {
            if (connectionEvent.getType() == ConnectionEvent.Type.DidConnect) {

                dm.add(Fire.Stream.getSendableEvents().getDeliveryReceipts().subscribe(receipt -> {
                    handleReceipt(receipt.getFrom(), receipt);
                }));

                dm.add(Fire.Stream.getChatEvents().pastAndNewEvents().subscribe(chatEvent -> {
                    IChat chat = chatEvent.getChat();
                    if (chatEvent.typeIs(EventType.Added)) {

                        chat.getDisposableMap().add(chat.getSendableEvents().getDeliveryReceipts().subscribe(receipt -> {
                            handleReceipt(receipt.getFrom(), receipt);
                        }));
                    }
                }));
            }
            if (connectionEvent.getType() == ConnectionEvent.Type.WillDisconnect) {
                dm.disposeAll();
            }
        });

    }

    protected void handleReceipt(String threadEntityID, DeliveryReceipt receipt) {
        dm.add(APIHelper.fetchRemoteUser(receipt.getFrom()).subscribe(user -> {
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
                    ChatSDK.events().source().onNext(NetworkEvent.threadReadReceiptUpdated(thread, message));
                }
            }
        }));
    }

    @Override
    public void updateReadReceipts(Thread thread) {

    }

    @Override
    public void updateReadReceipts(Message message) {

    }

    @Override
    public void markRead(Thread thread) {
        if (thread.typeIs(ThreadType.Private1to1)) {

            for (Message m : thread.getMessages()) {
                if (!m.getSender().isMe()) {
                    ReadStatus status = m.readStatusForUser(ChatSDK.currentUserID());
                    if (!status.is(ReadStatus.read())) {

                        m.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new DateTime());
                        // Send the read status

                        User otherUser = thread.otherUser();
                        Fire.Stream.sendDeliveryReceipt(otherUser.getEntityID(), DeliveryReceiptType.read(), m.getEntityID())
                                .doOnError(Throwable::printStackTrace)
                                .subscribe();
                    }
                }
            }
        }
    }
}
