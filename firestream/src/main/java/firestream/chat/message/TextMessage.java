package firestream.chat.message;

public class TextMessage extends Message {

    public static String TextKey = "text";

    public TextMessage() {

    }

    public TextMessage(String text) {
        body.put(TextKey, text);
    }

    public String getText() {
        return (String) body.get(TextKey);
    }

    public static TextMessage fromSendable(Sendable sendable) {
        TextMessage message = new TextMessage();
        sendable.copyTo(message);
        return message;
    }


}



