package sdk.chat.demo.examples.activities;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.options.BaseChatOption;
import io.reactivex.Completable;

public class MessageTestChatOption extends BaseChatOption {

    public MessageTestChatOption(String title, Drawable iconDrawable) {
        super(title, iconDrawable, (activity, thread) -> {
            ArrayList<Completable> completables = new ArrayList<>();
            for (Integer i = 0; i < 100; i++) {
                completables.add(ChatSDK.thread().sendMessageWithText(i.toString(), thread));

            }
            return Completable.concat(completables);
        });
    }

    public MessageTestChatOption(String title) {
        this(title, null);
    }

}
