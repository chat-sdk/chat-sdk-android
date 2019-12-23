package sdk.chat.micro.types;

public class DeliveryReceiptType extends BaseType {

    public static String Received = "received";
    public static String Read = "read";

    public DeliveryReceiptType (String type) {
        super(type);
    }

    public DeliveryReceiptType (BaseType type) {
        super(type);
    }

    public static DeliveryReceiptType received() {
        return new DeliveryReceiptType(Received);
    }

    public static DeliveryReceiptType read() {
        return new DeliveryReceiptType(Read);
    }

}
