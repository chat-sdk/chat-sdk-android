package co.chatsdk.ui.chat;

import android.view.View;
import android.widget.ImageView;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.ui.R;

public class ReadStatusViewBinder {
    public static void bind(ImageView view, Message message) {
        if (ChatSDK.readReceipts() == null && !message.getSender().isMe()) {
            view.setVisibility(View.GONE);
        } else {
            int resource = R.drawable.ic_message_received;
            ReadStatus status = message.getReadStatus();

            // Hide the read receipt for public threads
            if(message.getThread().typeIs(ThreadType.Public)) {
                status = ReadStatus.hide();
            }

            if(status.is(ReadStatus.delivered())) {
                resource = R.drawable.ic_message_delivered;
            }
            if(status.is(ReadStatus.read())) {
                resource = R.drawable.ic_message_read;
            }
            if(view != null) {
                view.setImageResource(resource);
                view.setVisibility(status.is(ReadStatus.hide()) ? View.GONE : View.VISIBLE);
            }
        }
    }
}
