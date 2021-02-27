package sdk.chat.core.module;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.handlers.MessageHandler;

public abstract class AbstractModule implements Module {

    @Override
    public MessageHandler getMessageHandler() {
        return null;
    }

    public List<String> requiredPermissions() {
        return new ArrayList<>();
    }

    @Override
    public void stop() {

    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public boolean isPremium() {
        return true;
    }

}
