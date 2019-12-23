package sdk.chat.micro.types;

public class SendableType extends BaseType {

    public static String Message = "message";
    public static String DeliveryReceipt = "receipt";
    public static String TypingState = "typing";
    public static String Presence = "presence";
    public static String Invitation = "invitation";

    public SendableType(String type) {
        super(type);
    }

    public SendableType(BaseType type) {
        super(type);
    }

    public static SendableType message() {
        return new SendableType(Message);
    }

    public static SendableType deliveryReceipt() {
        return new SendableType(DeliveryReceipt);
    }

    public static SendableType typingState() {
        return new SendableType(TypingState);
    }

    public static SendableType presence() {
        return new SendableType(Presence);
    }

    public static SendableType invitation() {
        return new SendableType(Invitation);
    }
}
