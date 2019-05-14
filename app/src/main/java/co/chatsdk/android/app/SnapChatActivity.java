package co.chatsdk.android.app;

import co.chatsdk.ui.chat.ChatActivity;

public class SnapChatActivity extends ChatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        reloadData();
    }
}