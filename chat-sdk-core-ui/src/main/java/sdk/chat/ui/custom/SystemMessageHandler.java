package sdk.chat.ui.custom;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.chat.model.SystemMessageHolder;
import sdk.chat.ui.view_holders.SystemMessageViewHolder;

public class SystemMessageHandler extends MessageHandler {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.System,
                SystemMessageViewHolder.class,
                R.layout.view_holder_system_message,
                R.layout.view_holder_system_message,
                MessageCustomizer.shared());
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.typeIs(MessageType.System)) {
            return new SystemMessageHolder(message);
        }
        return null;
    }

    @Override
    public boolean hasContentFor(MessageHolder message, byte type) {
        return type == MessageType.System && message instanceof SystemMessageHolder;
    }

}
