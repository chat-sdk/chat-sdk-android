package sdk.chat.dcom;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.fragments.ChatFragment;

public class DCom {

    public static String reloadData = "reload";

    static final DCom instance = new DCom();
    public static DCom shared() {
        return instance;
    }

    public void deleteAllMessages(ThreadX thread) {
        thread.markRead();
        List<Message> messages = new ArrayList<>();
        for (Message message: thread.getMessages()) {
            if (ChatSDK.thread().canDeleteMessage(message)) {
                messages.add(message);
            }
        }
        ChatSDK.thread().deleteMessages(messages).subscribe();
    }

    public void setup() {
        ChatSDKUI.setChatFragmentProvider((thread) -> {
            ChatFragment fragment = new DComChatFragment();
            fragment.setThread(thread);
            return fragment;
        });
        ChatSDK.ui().setPrivateThreadsFragment(new DComPrivateThreadsFragment());
    }
}
