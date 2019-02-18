package co.chatsdk.android.app;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * This class contains a list of example API calls for reference
 */
public class ApiExamples {

    /**
     * Example of how to send an image message to a thread
     * @param filePath - Local path to image file
     * @param thread - The thread to send the message to
     */
    public void sendImageMessage (String filePath, Thread thread) {
        Disposable d = ChatSDK.imageMessage().sendMessageWithImage(filePath, thread).subscribe(messageSendProgress -> {

        }, throwable -> {

        });
    }

    /**
     * Example of how to listen for when a message is received
     */
    public void listenForReceivedMessage () {
        ChatSDK.hook().addHook(new Hook(data -> Completable.create(emitter -> {
            Message message = (Message) data.get(HookEvent.Message);
            emitter.onComplete();
        })), HookEvent.MessageReceived);
    }

}
