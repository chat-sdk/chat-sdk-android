package firestream.chat.pro.interfaces;

import androidx.annotation.Nullable;

import firestream.chat.interfaces.IChat;
import firestream.chat.message.Sendable;
import firestream.chat.pro.types.DeliveryReceiptType;
import firestream.chat.pro.types.TypingStateType;
import io.reactivex.Completable;
import io.reactivex.functions.Consumer;

public interface IChatPro extends IChat {

    /**
     * Mark a message as received
     * @param sendable to mark as received
     * @return completion
     */
    Completable markReceived(Sendable sendable);
    Completable markReceived(String sendableId);

    /**
     * Mark a message as read
     * @param sendable to mark as read
     * @return completion
     */
    Completable markRead(Sendable sendable);
    Completable markRead(String sendableId);

    /**
     * Send a typing indicator message
     * @param type typing state
     * @param newId message's new ID before sending
     * @return completion
     */
    Completable sendTypingIndicator(TypingStateType type, @Nullable Consumer<String> newId);

    /**
     * Send a typing indicator message. An indicator should be sent when starting and stopping typing
     * @param type typing state
     * @return completion
     */
    Completable sendTypingIndicator(TypingStateType type);

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a message type delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the message
     * @param type receipt type
     * @param newId message's new ID before sending
     * @return completion
     */
    Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId);

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a message type delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the message
     * @param type receipt type
     * @return completion
     */
    Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId);

}
