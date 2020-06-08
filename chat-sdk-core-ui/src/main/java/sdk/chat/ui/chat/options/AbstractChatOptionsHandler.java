package sdk.chat.ui.chat.options;

import java.lang.ref.WeakReference;

import sdk.chat.core.interfaces.ChatOption;
import sdk.chat.core.interfaces.ChatOptionsDelegate;
import sdk.chat.core.interfaces.ChatOptionsHandler;

/**
 * Created by ben on 10/11/17.
 */

public abstract class AbstractChatOptionsHandler implements ChatOptionsHandler {

    protected WeakReference<ChatOptionsDelegate> delegate;

    public AbstractChatOptionsHandler (ChatOptionsDelegate delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    public void executeOption (ChatOption option) {
        if(delegate != null) {
            delegate.get().executeChatOption(option);
        }
    }

    public void setDelegate(ChatOptionsDelegate delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    @Override
    public ChatOptionsDelegate getDelegate() {
        return delegate.get();
    }

}
