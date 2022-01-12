package sdk.chat.dcom;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;

public class DCom {

    static final DCom instance = new DCom();
    public static DCom shared() {
        return instance;
    }

    public void deleteAllMessages(Thread thread) {
        thread.markRead();
        List<Message> messages = new ArrayList<>();
        for (Message message: thread.getMessages()) {
            if (ChatSDK.thread().canDeleteMessage(message)) {
                messages.add(message);
            }
        }
        ChatSDK.thread().deleteMessages(messages).subscribe();
    }

}
