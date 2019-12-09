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
}
