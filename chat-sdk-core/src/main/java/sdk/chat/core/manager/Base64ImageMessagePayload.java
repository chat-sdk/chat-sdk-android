package sdk.chat.core.manager;

import sdk.chat.core.dao.Message;

public class Base64ImageMessagePayload extends ImageMessagePayload {
    public Base64ImageMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return toString();
    }

    @Override
    public String imageURL() {
        return null;
    }

}
