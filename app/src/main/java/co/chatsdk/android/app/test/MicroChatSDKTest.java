package co.chatsdk.android.app.test;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import sdk.chat.micro.MicroChatSDK;

public class MicroChatSDKTest {

    public static void start () {
        MicroChatSDK.shared();

        ChatSDK.hook().addHook(new Hook(data -> Completable.create(emitter -> {
            Message message = (Message) data.get(HookEvent.Message);
            // Now send the text with micro sdk
            if (message.getThread().typeIs(ThreadType.Private1to1)) {
                MicroChatSDK.shared().sendMessageWithText(message.getThread().otherUser().getEntityID(), message.getText()).subscribe();
            }
        })), HookEvent.MessageSent);


    }

}
