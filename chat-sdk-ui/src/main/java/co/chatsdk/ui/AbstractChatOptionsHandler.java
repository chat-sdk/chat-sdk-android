package co.chatsdk.ui;

import android.content.Intent;

import java.lang.ref.WeakReference;

import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.ui.manager.InterfaceManager;

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for(ChatOption option : InterfaceManager.shared().a.getChatOptions()) {

        }
    }

}
