package sdk.chat.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.view_holders.IncomingTextMessageViewHolder;
import sdk.chat.ui.view_holders.OutcomingTextMessageViewHolder;

public class TextMessageHandler extends CustomMessageHandler {

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.setIncomingTextConfig(IncomingTextMessageViewHolder.class, R.layout.view_holder_incoming_text_message, getAvatarClickPayload(context))
                .setOutcomingTextConfig(OutcomingTextMessageViewHolder.class, R.layout.view_holder_outcoming_text_message, getAvatarClickPayload(context));
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.typeIs(MessageType.Text)) {
            return super.onNewMessageHolder(message);
        }
        return null;
    }

    @Override
    public List<Byte> getTypes() {
        return types(MessageType.Text);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return true;
    }

    @Override
    public boolean onClick(Activity activity, View rootView, Message message) {
        if (!super.onClick(activity, rootView, message)) {
            return false;
        }
        return true;
    }

}
