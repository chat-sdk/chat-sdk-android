package sdk.chat.ui.binders;

import android.view.View;
import android.widget.ImageView;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;

public class ReadStatusViewBinder {

    public void onBind(ImageView view, MessageHolder holder) {
        // Holder is last message so can be null
        if (holder == null) {
            return;
        }
        if (holder.isTyping() || holder.getMessage().getMessageType().is(MessageType.System) || !holder.getUser().isMe() || holder.getReadStatus().is(ReadStatus.hide())) {
            view.setVisibility(View.GONE);
        } else {
            ReadStatus status = holder.getReadStatus();
            MessageSendStatus sendStatus = holder.getSendStatus();

//            int resource = R.drawable.icn_30_sending;
            int resource = -1;

            if (sendStatus == MessageSendStatus.Initial || sendStatus == MessageSendStatus.Uploading) {
                resource = R.drawable.icn_30_sending;
            } else if (holder.canResend()) {
                resource = R.drawable.icn_30_failed;
            } else if (ChatSDK.readReceipts() != null) {
                if (sendStatus == MessageSendStatus.Sent) {
                    resource = R.drawable.icn_30_sent;
                }
                if (status.is(ReadStatus.delivered())) {
                    resource = R.drawable.icn_30_delivered;
                } else if (status.is(ReadStatus.read())) {
                    resource = R.drawable.icn_30_read;
                }
            }

            if (view != null && resource != -1) {
                view.setImageResource(resource);
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }
}
