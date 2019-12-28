package co.chatsdk.firefly;

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
import firefly.sdk.chat.firebase.rx.DisposableList;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.DeliveryReceiptType;

public class FireflyReadReceiptHandler implements ReadReceiptHandler {

    private DisposableList disposableList = new DisposableList();

    public FireflyReadReceiptHandler() {
        disposableList.add(Fl.y.getEvents().getDeliveryReceipts().subscribe(deliveryReceipt -> {

            // Get the sender
            String senderId = deliveryReceipt.from;

            disposableList.add(APIHelper.fetchRemoteUser(senderId).subscribe(user -> {
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(senderId);
                if (thread != null) {
                    // Get the text
                    Message message = thread.getMessageWithEntityID(deliveryReceipt.getMessageId());
                    if (message != null) {
                        if (deliveryReceipt.getBodyType().equals(DeliveryReceiptType.read())) {
                            message.setUserReadStatus(user, ReadStatus.read(), new DateTime());
                        } else {
                            message.setUserReadStatus(user, ReadStatus.delivered(), new DateTime());
                        }

                        ChatSDK.events().source().onNext(NetworkEvent.threadReadReceiptUpdated(thread, message));
                    }
                }
            }));

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
