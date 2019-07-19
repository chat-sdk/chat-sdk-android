package co.chatsdk.android.app.test;

import android.app.Activity;

import java.util.ArrayList;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.chat.LocationSelector;
import co.chatsdk.ui.chat.options.BaseChatOption;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class MessageTestChatOption extends BaseChatOption {

    public MessageTestChatOption(String title, Integer iconResourceId) {
        super(title, iconResourceId, (activity, thread) -> {
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
