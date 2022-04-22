package sdk.chat.message;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.ui.custom.TextMessageRegistration;
import sdk.chat.ui.view_holders.v2.V2TextMessageViewHolder;

public class V2TextMessageRegistration extends TextMessageRegistration {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.setIncomingTextConfig(V2TextMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_incoming_text_message, getAvatarClickPayload(context))
                .setOutcomingTextConfig(V2TextMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_outcoming_text_message, getAvatarClickPayload(context));
    }

}
