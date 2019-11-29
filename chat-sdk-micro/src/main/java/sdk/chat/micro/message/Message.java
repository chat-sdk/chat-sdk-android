package sdk.chat.micro.message;

import java.util.HashMap;

import sdk.chat.micro.types.SendableType;

public class Message extends Sendable {

    public Message () {
        type = SendableType.Message;
    }

    public Message (HashMap<String, Object> body) {
        this();
        this.body = body;
    }

}
