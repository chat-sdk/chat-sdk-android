package co.chatsdk.firestore;

import org.joda.time.DateTime;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.rx.DisposableList;
import sdk.chat.micro.types.DeliveryReceiptType;

public class FirestoreReadReceiptHandler implements ReadReceiptHandler {

    private DisposableList disposableList = new DisposableList();

    public FirestoreReadReceiptHandler () {
        disposableList.add(MicroChatSDK.shared().getDeliveryReceiptStream().subscribe(deliveryReceipt -> {

            // Get the sender
            String senderId = deliveryReceipt.from;

            disposableList.add(UserHelper.fetchUser(senderId).subscribe(user -> {
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
                        MicroChatSDK.shared().sendDeliveryReceipt(otherUser.getEntityID(), DeliveryReceiptType.read(), m.getEntityID())
                                .doOnError(Throwable::printStackTrace)
                                .subscribe();
                    }
                }
            }
        }
    }
}
