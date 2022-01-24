package sdk.chat.waka;

import android.os.Bundle;

import sdk.chat.core.handlers.EventHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.activities.ChatActivity;
import sdk.guru.common.EventType;

public class ZChatActivity extends ChatActivity {

    public void updateThread(Bundle bundle) {
        super.updateThread(bundle);

        // If this thread is not already on, turn it on...
        if (thread != null && thread.getEntityID() != null) {
            EventHandler handler = ChatSDK.a().events;
            if (handler instanceof ZFirebaseEventHandler) {
                ZFirebaseEventHandler zh = (ZFirebaseEventHandler) handler;
                zh.threadOn(ChatSDK.currentUser(), thread.getEntityID(), EventType.Added);
            }
        }

    }
}
