package co.chatsdk.ui.view_holders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.ReadStatusViewBinder;
import co.chatsdk.ui.chat.model.ImageMessageHolder;

/*
 * Created by troy379 on 05.04.17.
 */
public class OutcomingImageMessageViewHolder
        extends MessageHolders.OutcomingImageMessageViewHolder<ImageMessageHolder> {

    @BindView(R2.id.readStatus) protected ImageView readStatus;

    public OutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
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