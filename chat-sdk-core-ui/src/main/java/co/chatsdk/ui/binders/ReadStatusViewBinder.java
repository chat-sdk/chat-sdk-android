package co.chatsdk.ui.binders;

import android.view.View;
import android.widget.ImageView;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.types.ReadStatus;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.MessageHolder;

public class ReadStatusViewBinder {

    public static void onBind(ImageView view, MessageHolder holder) {
        if (ChatSDK.readReceipts() == null || holder == null || holder.getMessage().getMessageType().is(MessageType.System) || !holder.getUser().getUser().isMe() || holder.getReadStatus().is(ReadStatus.hide())) {
            view.setVisibility(View.GONE);
        } else {
            ReadStatus status = holder.getReadStatus();
            MessageSendStatus sendStatus = holder.getSendStatus();

//            int resource = R.drawable.icn_30_sending;
            int resource = -1;

            if (sendStatus == MessageSendStatus.Failed) {
                resource = R.drawable.icn_30_sending;
            }
            if (sendStatus == MessageSendStatus.Sent) {
                resource = R.drawable.icn_30_sent;
            }
            if (status.is(ReadStatus.delivered())) {
                resource = R.drawable.icn_30_delivered;
            } else if (status.is(ReadStatus.read())) {
                resource = R.drawable.icn_30_read;
            }

            if (view != null && resource != -1) {
                view.setImageResource(resource);
                view.setVisibility(status.is(ReadStatus.hide()) ? View.GONE : View.VISIBLE);
            }
        }
    }
}
