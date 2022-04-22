package sdk.chat.ui.view_holders.v2;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.ui.chat.model.MessageHolder;

public class BaseMessageViewHolder2<T extends MessageHolder> extends MessageHolders.BaseMessageViewHolder<T> {

    public BaseMessageViewHolder2(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(T t) {

    }
}
