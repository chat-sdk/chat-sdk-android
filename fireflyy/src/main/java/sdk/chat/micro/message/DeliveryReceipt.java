package sdk.chat.micro.message;

import com.google.firebase.firestore.Exclude;

import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.SendableType;

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

    @Exclude
    public String getMessageId() throws Exception {
        return getBodyString(MessageId);
    }

    @Exclude
    public DeliveryReceiptType getBodyType() {
        return new DeliveryReceiptType(super.getBodyType());
    }

}
