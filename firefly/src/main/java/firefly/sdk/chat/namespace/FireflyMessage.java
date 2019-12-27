package firefly.sdk.chat.namespace;

import firefly.sdk.chat.message.Message;

public class FireflyMessage extends Message {

    public static FireflyMessage fromMessage(Message message) {
        FireflyMessage fireflyMessage = new FireflyMessage();

        fireflyMessage.id = message.id;
        fireflyMessage.type = message.type;
        fireflyMessage.from = message.from;
        fireflyMessage.date = message.date;
        fireflyMessage.body = message.body;

        return fireflyMessage;
    }

}
