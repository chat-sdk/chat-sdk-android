package co.patchat.android.app;

import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.chat.MessageListItem;
import co.chatsdk.ui.chat.MessagesListAdapter;

public class CustomMessagesListAdapter extends MessagesListAdapter {

    public class CustomMessageViewHolder extends MessagesListAdapter.MessageViewHolder {

        public TextView senderTextView;

        public CustomMessageViewHolder(View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.txt_sender);
        }

    }

    public CustomMessagesListAdapter(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ViewTypeReply) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View row = inflater.inflate(R.layout.custom_row_message_reply , null);
            return new CustomMessageViewHolder(row);
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MessageListItem messageItem = messageItems.get(position);
        if (!messageItem.message.getSender().isMe()) {
            if (messageItem.message.getThread().typeIs(ThreadType.Group)) {
                String name = messageItem.getMessage().getSender().getName();
                if (name == null || name.isEmpty()) {
                    name = messageItem.getMessage().getSender().getEmail();
                }
                ((CustomMessageViewHolder) holder).senderTextView.setText(name);
            } else {
                ((CustomMessageViewHolder) holder).senderTextView.setText("");
            }
        }
    }

}
