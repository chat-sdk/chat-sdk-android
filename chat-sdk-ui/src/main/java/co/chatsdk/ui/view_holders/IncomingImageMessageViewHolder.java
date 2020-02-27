package co.chatsdk.ui.view_holders;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.NameBinder;
import co.chatsdk.ui.binders.OnlineStatusBinder;
import co.chatsdk.ui.chat.model.ImageMessageHolder;

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

        NameBinder.bind(userName, message);
    }

    @Override
    protected Object getPayloadForImageLoader(ImageMessageHolder message) {
        return message;
    }

}