package co.chatsdk.ui.chat.binders;

import android.view.View;
import android.widget.ImageView;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.MessageHolder;

public class ReadStatusViewBinder {

    public static void onBind(ImageView view, MessageHolder holder) {
        if (ChatSDK.readReceipts() == null) {
            view.setVisibility(View.GONE);
        } else {
            int resource = R.drawable.ic_message_received;
            ReadStatus status = holder.getReadStatus();

            if (status.is(ReadStatus.delivered())) {
                resource = R.drawable.ic_message_delivered;
            }
            if (status.is(ReadStatus.read())) {
                resource = R.drawable.ic_message_read;
            }
            if (view != null) {
                view.setImageResource(resource);
                view.setVisibility(status.is(ReadStatus.hide()) ? View.GONE : View.VISIBLE);
            }
        }
    }
}
