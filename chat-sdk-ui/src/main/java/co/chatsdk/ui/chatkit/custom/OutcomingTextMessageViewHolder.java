package co.chatsdk.ui.chatkit.custom;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.ui.chatkit.model.MessageHolder;

public class OutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<MessageHolder> {

    public OutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(MessageHolder message) {
        super.onBind(message);

        time.setText(message.getStatus() + " " + time.getText());
    }
}
