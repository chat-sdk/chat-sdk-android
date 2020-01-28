package co.chatsdk.ui.chatkit.view_holders;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chatkit.model.MessageHolder;

public class IncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<MessageHolder> {

    private View onlineIndicator;
    protected TextView userName;

    public IncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        userName = itemView.findViewById(R.id.userName);
    }

    @Override
    public void onBind(MessageHolder holder) {
        super.onBind(holder);

        Message message = holder.getMessage();
        Message previousMessage = message.getPreviousMessage();
        Message nextMessage = message.getNextMessage();

        boolean isOnline = holder.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_offline);
        }

        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(view -> {
            if (payload != null && payload.avatarClickListener != null) {
                payload.avatarClickListener.onAvatarClick(holder.getMessage().getSender());
            }
        });

        if (message.getThread().typeIs(ThreadType.Group) && (previousMessage == null || !message.getSender().equalsEntity(previousMessage.getSender()))) {
            userName.setVisibility(View.VISIBLE);
            userName.setText(holder.getUser().getName());
        } else {
            userName.setVisibility(View.GONE);
        }

        // Hide the time if it's the same as the next message
        DateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm");
        if (nextMessage != null && format.format(message.getDate().toDate()).equals(format.format(nextMessage.getDate().toDate()))) {
            time.setVisibility(View.GONE);
        } else {
            time.setVisibility(View.VISIBLE);
        }
    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    public interface OnAvatarClickListener {
        void onAvatarClick(User user);
    }
}
