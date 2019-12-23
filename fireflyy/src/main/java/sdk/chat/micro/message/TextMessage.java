package sdk.chat.micro.message;

public class TextMessage extends Message {

    public static String TextKey = "text";

    public TextMessage() {

    }

    public TextMessage(String text) {
        body.put(TextKey, text);
    }

}



