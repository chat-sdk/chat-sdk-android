package sdk.chat.core.manager;

import sdk.chat.core.dao.Message;

public class TextMessagePayload extends AbstractMessagePayload {

    public TextMessagePayload(Message message) {
        super(message, null);
    }

    public TextMessagePayload(Message message, MessagePayload reply) {
        super(message, reply);
    }

    @Override
    public String imageURL() {
        return null;
    }

}
