package sdk.chat.app.xmpp.wow;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.demo.xmpp.R;
import sdk.chat.ui.custom.TextMessageHandler;

public class WowTextMessageHandler extends TextMessageHandler {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.setIncomingTextConfig(WowIncomingTextMessageViewHolder.class, R.layout.wow_view_holder_incoming_text_message, getAvatarClickPayload(context))
                .setOutcomingTextConfig(WowOutcomingTextMessageViewHolder.class, R.layout.wow_view_holder_outcoming_text_message, getAvatarClickPayload(context));
        holders.setDateHeaderConfig(MessageHolders.DefaultDateHeaderViewHolder.class, R.layout.wow_item_date_header);
    }


}
