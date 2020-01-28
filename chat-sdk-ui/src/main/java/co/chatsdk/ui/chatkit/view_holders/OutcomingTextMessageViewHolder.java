package co.chatsdk.ui.chatkit.view_holders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageSendStatusFormatter;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ReadStatusViewBinder;
import co.chatsdk.ui.chatkit.model.MessageHolder;

public class OutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<MessageHolder> {

    ImageView readStatus;

    public OutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        readStatus = itemView.findViewById(R.id.read_status);

        if (ChatSDK.readReceipts() == null) {
            readStatus.setVisibility(View.GONE);
        } else {
            readStatus.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBind(MessageHolder holder) {
        super.onBind(holder);

        String status = MessageSendStatusFormatter.format(ChatSDK.shared().context(), holder.getStatus(), holder.getUploadPercentage());
        String timeString = status + " " + time.getText();

        time.setText(timeString);

        ReadStatusViewBinder.bind(readStatus, holder.getMessage());
    }
}
