package firefly.sdk.chat.message;

import firefly.sdk.chat.types.DeliveryReceiptType;
import firefly.sdk.chat.types.SendableType;

public class DeliveryReceipt extends Sendable {

    public static String MessageId = "id";

    public DeliveryReceipt() {
        type = SendableType.DeliveryReceipt;
    }

    public DeliveryReceipt(DeliveryReceiptType type, String messageUid) {
        this();
        setBodyType(type);
        body.put(MessageId, messageUid);
    }

    public String getMessageId() throws Exception {
        return getBodyString(MessageId);
    }

    public DeliveryReceiptType getBodyType() {
        return new DeliveryReceiptType(super.getBodyType());
    }

    public static DeliveryReceipt fromSendable(Sendable sendable) {
        DeliveryReceipt deliveryReceipt = new DeliveryReceipt();
        sendable.copyTo(deliveryReceipt);
        return deliveryReceipt;
    }

}
