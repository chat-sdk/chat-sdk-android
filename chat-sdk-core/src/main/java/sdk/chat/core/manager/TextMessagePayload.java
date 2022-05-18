package sdk.chat.core.manager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
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

    @Override
    public List<String> remoteURLs() {
        return new ArrayList<>();
    }

    @Override
    public Completable downloadMessageContent() {
        return null;
    }

}
