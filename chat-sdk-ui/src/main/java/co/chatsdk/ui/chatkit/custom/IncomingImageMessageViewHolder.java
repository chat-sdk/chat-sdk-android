package co.chatsdk.ui.chatkit.custom;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.ui.R;
import co.chatsdk.ui.chatkit.model.MessageHolder;

/*
 * Created by troy379 on 05.04.17.
 */
public class IncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<MessageHolder> {

    private View onlineIndicator;

    public IncomingImageMessageViewHolder(View itemView, Object payload) {
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
    }
}