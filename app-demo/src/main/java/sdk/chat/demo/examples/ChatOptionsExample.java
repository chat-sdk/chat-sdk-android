package sdk.chat.demo.examples;

import sdk.chat.core.interfaces.ChatOption;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.options.ChatOptionBuilder;

public class ChatOptionsExample {

    public static void run() {

        ChatOption option = new ChatOptionBuilder().title("New Option!").action((activity, thread) -> {
            // When the option is pressed, you can perform an action

            return null;
        }).build();

        ChatSDK.ui().addChatOption(option);

    }

}
