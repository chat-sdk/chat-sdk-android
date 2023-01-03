package sdk.chat.ui.view_holders.v2;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.ui.custom.TextMessageRegistration;
import sdk.chat.ui.view_holders.v2.outer.V2;

@Deprecated
public class V2TextMessageRegistration extends TextMessageRegistration {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.setIncomingTextConfig(V2.IncomingMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_incoming_text_message)
                .setOutcomingTextConfig(V2.OutcomingMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_outcoming_text_message);
    }

}
