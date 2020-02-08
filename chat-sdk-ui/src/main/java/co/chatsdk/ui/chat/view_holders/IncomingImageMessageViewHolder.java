package co.chatsdk.ui.chat.view_holders;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.chat.binders.OnlineStatusBinder;
import co.chatsdk.ui.chat.model.ImageMessageHolder;
import co.chatsdk.ui.chat.model.MessageHolder;

/*
 * Created by troy379 on 05.04.17.
 */
public class IncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<ImageMessageHolder> {

    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.userName) protected TextView userName;

    public IncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(ImageMessageHolder message) {
        super.onBind(message);

        boolean isOnline = message.getUser().isOnline();
        OnlineStatusBinder.bind(onlineIndicator, isOnline);

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