package sdk.chat.core.base;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.MessageHandler;

public abstract class AbstractMessageHandler implements MessageHandler {
    @Override
    public List<String> remoteURLs(Message message) {
        return new ArrayList<>();
    }
}
