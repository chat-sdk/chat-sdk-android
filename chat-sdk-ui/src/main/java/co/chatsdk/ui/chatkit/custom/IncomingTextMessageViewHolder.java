package co.chatsdk.ui.chatkit.custom;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.ui.R;
import co.chatsdk.ui.chatkit.model.MessageHolder;

public class IncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<MessageHolder> {

    private View onlineIndicator;

    public IncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
    }

    @Override
    public void onBind(MessageHolder message) {
        super.onBind(message);

        boolean isOnline = message.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_offline);
        }

        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payload != null && payload.avatarClickListener != null) {
                    payload.avatarClickListener.onAvatarClick();
                }
            }
        });
    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    public interface OnAvatarClickListener {
        void onAvatarClick();
    }
}
