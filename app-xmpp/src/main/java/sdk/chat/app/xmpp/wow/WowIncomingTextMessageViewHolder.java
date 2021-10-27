package sdk.chat.app.xmpp.wow;

import android.view.View;

import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.view_holders.IncomingTextMessageViewHolder;

public class WowIncomingTextMessageViewHolder extends IncomingTextMessageViewHolder {
    public WowIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(MessageHolder message) {
        super.onBind(message);
    }
}
