package co.chatsdk.android.app;

import java.util.HashMap;

import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.handlers.EncryptionHandler;
import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.session.ChatSDK;

public class BaseEncryptionHandler implements EncryptionHandler {

    public BaseEncryptionHandler () {
        Hook hook = new Hook(data -> {
            Object message = data.get(BaseHookHandler.MessageReceived_Message);
            if (message != null && message instanceof Message) {
                decrypt((Message) message);
            }
        });
        ChatSDK.hook().addHook(hook, BaseHookHandler.MessageReceived);

        Hook auth = new Hook(data -> {
            // This code will run here, when the user is logged in
            // ...
        });
        ChatSDK.hook().addHook(auth, BaseHookHandler.UserAuthFinished);


    }

    @Override
    public void encrypt(Message message) {
        message.getThread().getUsers();
    }

    @Override
    public void decrypt(Message message) {
        message.setTextString(message.getTextString() + " Conrad");
    }
}
