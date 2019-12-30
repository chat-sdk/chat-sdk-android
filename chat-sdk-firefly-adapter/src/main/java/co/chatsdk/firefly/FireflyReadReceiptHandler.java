package co.chatsdk.firefly;

import org.joda.time.DateTime;

import java.util.HashMap;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import firefly.sdk.chat.chat.Chat;
import firefly.sdk.chat.events.ConnectionEvent;
import firefly.sdk.chat.events.EventType;
import firefly.sdk.chat.firebase.rx.DisposableMap;
import firefly.sdk.chat.message.DeliveryReceipt;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.DeliveryReceiptType;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class FireflyReadReceiptHandler implements ReadReceiptHandler {

    private DisposableMap dm = new DisposableMap();

    public FireflyReadReceiptHandler() {

        // We want to add these listeners when we connect and remove them when we disconnect
        Disposable d = Fl.y.getConnectionEvents().subscribe(connectionEvent -> {
            if (connectionEvent.getType() == ConnectionEvent.Type.DidConnect) {

                dm.add(Fl.y.getEvents().getDeliveryReceipts().subscribe(receipt -> {
                    handleReceipt(receipt.from, receipt);
                }));

                dm.add(Fl.y.getChatEvents().pastAndNewEvents().subscribe(chatEvent -> {
                    Chat chat = chatEvent.chat;
                    if (chatEvent.type == EventType.Added) {

                        chat.getDisposableMap().add(chat.getEvents().getDeliveryReceipts().subscribe(receipt -> {
                            handleReceipt(receipt.from, receipt);
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
        dm.add(APIHelper.fetchRemoteUser(receipt.from).subscribe(user -> {
            Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
            if (thread != null) {
                // Get the text
                Message message = thread.getMessageWithEntityID(receipt.getMessageId());
                if (message != null) {
                    if (receipt.getBodyType().equals(DeliveryReceiptType.read())) {
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
                        Fl.y.sendDeliveryReceipt(otherUser.getEntityID(), DeliveryReceiptType.read(), m.getEntityID())
                                .doOnError(Throwable::printStackTrace)
                                .subscribe();
                    }
                }
            }
        }
    }
}
