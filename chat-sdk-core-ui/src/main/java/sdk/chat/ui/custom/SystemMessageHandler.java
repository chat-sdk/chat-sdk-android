package sdk.chat.ui.custom;

import android.content.Context;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.chat.model.SystemMessageHolder;
import sdk.chat.ui.view_holders.SystemMessageViewHolder;

public class SystemMessageHandler extends CustomMessageHandler {

    @Override
    public List<Byte> getTypes() {
        return types(MessageType.System);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return holder.getClass().equals(SystemMessageHolder.class);
    }

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.System,
                SystemMessageViewHolder.class,
                R.layout.view_holder_system_message,
                R.layout.view_holder_system_message,
                ChatSDKUI.shared().getMessageCustomizer());
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.typeIs(MessageType.System)) {
            return new SystemMessageHolder(message);
        }
        return null;
    }

}
