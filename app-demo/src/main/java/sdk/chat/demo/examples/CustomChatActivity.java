package sdk.chat.demo.examples;

import androidx.annotation.LayoutRes;

import sdk.chat.demo.pre.R;
import sdk.chat.ui.activities.ChatActivity;

public class CustomChatActivity extends ChatActivity {

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_chat;
    }

}
