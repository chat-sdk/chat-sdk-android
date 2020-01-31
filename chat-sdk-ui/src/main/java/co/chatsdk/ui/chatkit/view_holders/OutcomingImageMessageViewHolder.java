package co.chatsdk.ui.chatkit.view_holders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatusFormatter;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chatkit.binders.MessageBinder;
import co.chatsdk.ui.chatkit.binders.ReadStatusViewBinder;
import co.chatsdk.ui.chatkit.model.ImageMessageHolder;

/*
 * Created by troy379 on 05.04.17.
 */
public class OutcomingImageMessageViewHolder
        extends MessageHolders.OutcomingImageMessageViewHolder<ImageMessageHolder> {

    protected ImageView readStatus;

    public OutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        readStatus = itemView.findViewById(R.id.readStatus);
    }

    @Override
    public void onBind(ImageMessageHolder holder) {
        super.onBind(holder);

        ReadStatusViewBinder.onBind(readStatus, holder);
        MessageBinder.onBindSendStatus(time, holder);

    }

    @Override
    protected Object getPayloadForImageLoader(ImageMessageHolder message) {
        return message;
    }
}