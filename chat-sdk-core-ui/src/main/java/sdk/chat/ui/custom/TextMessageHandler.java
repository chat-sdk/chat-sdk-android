package sdk.chat.ui.custom;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.view_holders.IncomingTextMessageViewHolder;
import sdk.chat.ui.view_holders.OutcomingTextMessageViewHolder;

public class TextMessageHandler extends MessageHandler {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.setIncomingTextConfig(IncomingTextMessageViewHolder.class, R.layout.view_holder_incoming_text_message, getAvatarClickPayload(context))
                .setOutcomingTextConfig(OutcomingTextMessageViewHolder.class, R.layout.view_holder_outcoming_text_message, getAvatarClickPayload(context));
    }

    @Override
    public boolean hasContentFor(MessageHolder message, byte type) {
        return type == MessageType.Text;
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.typeIs(MessageType.Text)) {
            return super.onNewMessageHolder(message);
        }
        return null;
    }

}
