package sdk.chat.core.manager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
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

    @Override
    public List<String> remoteURLs() {
        return new ArrayList<>();
    }

    @Override
    public Completable downloadMessageContent() {
        return Completable.complete();
    }
}
