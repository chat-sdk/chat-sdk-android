package sdk.chat.core.module;

import sdk.chat.core.handlers.MessageHandler;

public abstract class AbstractModule implements Module {

    @Override
    public MessageHandler getMessageHandler() {
        return null;
    }

}
