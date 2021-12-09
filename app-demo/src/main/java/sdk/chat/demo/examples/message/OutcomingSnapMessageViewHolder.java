package sdk.chat.demo.examples.message;

import android.view.View;

import sdk.chat.ui.view_holders.base.BaseOutcomingTextMessageViewHolder;

public class OutcomingSnapMessageViewHolder extends BaseOutcomingTextMessageViewHolder<SnapMessageHolder> {
    public OutcomingSnapMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(SnapMessageHolder message) {
        super.onBind(message);

    }

}
