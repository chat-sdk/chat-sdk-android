package co.chatsdk.ui.chat.view_holders;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.ImageMessageHolder;
import co.chatsdk.ui.chat.model.MessageHolder;

/*
 * Created by troy379 on 05.04.17.
 */
public class IncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<ImageMessageHolder> {

    private View onlineIndicator;
    protected TextView userName;

    public IncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        userName = itemView.findViewById(R.id.userName);
    }

    @Override
    public void onBind(ImageMessageHolder message) {
        super.onBind(message);

        boolean isOnline = message.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_offline);
        }

        if (message.getMessage().getThread().typeIs(ThreadType.Group)) {
            userName.setVisibility(View.VISIBLE);
            userName.setText(message.getUser().getName());
        } else {
            userName.setVisibility(View.GONE);
        }
    }

    @Override
    protected Object getPayloadForImageLoader(ImageMessageHolder message) {
        return message;
    }

}