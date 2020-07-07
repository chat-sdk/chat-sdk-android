package sdk.chat.demo.examples.message;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.demo.pre.R;
import sdk.chat.ui.custom.TextMessageHandler;
import sdk.chat.ui.view_holders.IncomingTextMessageViewHolder;

public class CustomTextMessageHandler extends TextMessageHandler {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.setIncomingTextConfig(IncomingTextMessageViewHolder.class, R.layout.view_holder_incoming_text_message, getAvatarClickPayload(context))
                .setOutcomingTextConfig(CustomOutcomingTextMessageViewHolder.class, R.layout.view_holder_outcoming_text_message, getAvatarClickPayload(context));
    }

}
