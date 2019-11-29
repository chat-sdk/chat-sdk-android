package sdk.chat.micro.message;

import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.SendableType;

public class DeliveryReceipt extends Sendable {

    public static String MessageId = "mid";

    public DeliveryReceipt() {
        type = SendableType.DeliveryReceipt;
    }

    public DeliveryReceipt(DeliveryReceiptType type, String messageUid) {
        this();
        setType(type);
        body.put(MessageId, messageUid);
    }

    public String getMessageId() throws Exception {
        return getBodyString(MessageId);
    }

    public DeliveryReceiptType getBodyType() {
        return new DeliveryReceiptType(super.getBodyType());
    }

}
