package co.chatsdk.android.app;

import co.chatsdk.ui.chat.ChatActivity;

public class CustomChatActivity extends ChatActivity {

    @Override
    protected void initViews() {
        super.initViews();
        if (messageListAdapter == null || !messageListAdapter.getClass().equals(CustomMessagesListAdapter.class)) {
            messageListAdapter = new CustomMessagesListAdapter(CustomChatActivity.this);
        }
        recyclerView.setAdapter(messageListAdapter);
    }

}
