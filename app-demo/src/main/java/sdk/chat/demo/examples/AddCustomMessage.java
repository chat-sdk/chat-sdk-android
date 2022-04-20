package sdk.chat.demo.examples;

import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.types.MessageType;
import sdk.chat.demo.examples.message.DefaultTextMessageRegistration;
import sdk.chat.demo.examples.message.ExampleSnapMessageRegistration;
import sdk.chat.ui.ChatSDKUI;

public class AddCustomMessage {

    public static void run() {

        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new DefaultTextMessageRegistration());
        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new ExampleSnapMessageRegistration());
    }

    public static void sendCustomMessage(Thread thread) {

        Disposable d = MessageSendRig.create(new MessageType(ExampleSnapMessageRegistration.SnapMessageType), thread, message -> {
            // Set custom Data here
            message.setValueForKey("Value", "Key");
        }).run().subscribe(() -> {
            // Success
        }, throwable -> {
            // Failure
        });

    }
}
