package firestream.chat.pro.interfaces;

import javax.annotation.Nullable;

import firestream.chat.interfaces.IFireStream;
import firestream.chat.pro.types.DeliveryReceiptType;
import firestream.chat.pro.types.PresenceType;
import firestream.chat.pro.types.TypingStateType;
import io.reactivex.Completable;
import io.reactivex.functions.Consumer;

public interface IFirestreamPro extends IFireStream {

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a message type delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the message
     * @param userId - the recipient user id
     * @param type - the status getTypingStateType
     * @return - subscribe to get a completion, error update from the method
     */
    Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId);
    Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId);

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param userId - the recipient user id
     * @param type - the status getTypingStateType
     * @return - subscribe to get a completion, error update from the method
     */
    Completable sendTypingIndicator(String userId, TypingStateType type);
    Completable sendTypingIndicator(String userId, TypingStateType type, @Nullable Consumer<String> newId);

    /**
     * Messages can always be deleted locally. Messages can only be deleted remotely
     * for recent messages. Specifically, when the client connects, it will add a
     * message listener to get an update for "new" messages. By default, we listen
     * to messages that were added after we last sent a message or a received delivery
     * receipt. This is the dateOfLastDeliveryReceipt. A client will only pick up
     * remote delivery receipts if the date of delivery is after this date.
     * @return completion
     */
    Completable sendPresence(String userId, PresenceType type);
    Completable sendPresence(String userId, PresenceType type, @Nullable Consumer<String> newId);

    Completable markReceived(String fromUserId, String sendableId);
    Completable markRead(String fromUserId, String sendableId);

}
