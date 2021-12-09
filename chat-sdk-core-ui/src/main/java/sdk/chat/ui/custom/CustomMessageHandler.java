package sdk.chat.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.utils.DialogUtils;
import sdk.chat.ui.view_holders.base.BaseIncomingTextMessageViewHolder;

public abstract class CustomMessageHandler implements IMessageHandler {

    protected BaseIncomingTextMessageViewHolder.Payload getAvatarClickPayload(Context context) {
        BaseIncomingTextMessageViewHolder.Payload holderPayload = new BaseIncomingTextMessageViewHolder.Payload();

        holderPayload.avatarClickListener = user -> {
            ChatSDK.ui().startProfileActivity(context, user.getEntityID());
        };

        return holderPayload;
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        return new MessageHolder(message);
    }

    @Override
    public boolean onClick(Activity activity, View rootView, Message message) {
        if (message.getSender().isMe() && message.getMessageStatus() == MessageSendStatus.Failed) {
            DialogUtils.showToastDialog(activity, R.string.message_send_failed, R.string.try_to_resend_the_message, R.string.send, R.string.cancel, () -> {
                MessageSendRig.create(message).run().subscribe(ChatSDK.events());
            }, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onLongClick(Activity activity, View rootView, Message message) {
        return false;
    }

    protected final List<Byte> types(Integer... values) {
        List<Byte> bytes = new ArrayList<>();
        for (Integer value: values) {
            bytes.add(value.byteValue());
        }
        return bytes;
    }

}
