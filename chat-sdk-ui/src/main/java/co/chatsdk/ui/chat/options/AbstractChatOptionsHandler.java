package co.chatsdk.ui.chat.options;

import android.content.Intent;

import java.lang.ref.WeakReference;

import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.core.session.ChatSDK;

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
