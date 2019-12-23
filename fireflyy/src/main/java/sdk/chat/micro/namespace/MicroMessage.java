package sdk.chat.micro.namespace;

import sdk.chat.micro.message.Message;

public class MicroMessage extends Message {

    public static MicroMessage fromMessage(Message message) {
        MicroMessage microMessage = new MicroMessage();

        microMessage.id = message.id;
        microMessage.type = message.type;
        microMessage.from = message.from;
        microMessage.date = message.date;
        microMessage.body = message.body;

        return microMessage;
    }

}
