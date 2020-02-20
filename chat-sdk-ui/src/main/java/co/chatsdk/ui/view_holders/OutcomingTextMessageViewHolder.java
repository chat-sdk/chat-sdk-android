package co.chatsdk.ui.view_holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.ReadStatusViewBinder;
import co.chatsdk.ui.binders.ReplyViewBinder;
import co.chatsdk.ui.chat.model.MessageHolder;

public class OutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<MessageHolder> {

    @BindView(R2.id.readStatus) protected ImageView readStatus;
    @BindView(R2.id.replyView) protected View replyView;
    @BindView(R2.id.replyImageView) protected ImageView replyImageView;
    @BindView(R2.id.replyTextView) protected TextView replyTextView;

    public OutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(MessageHolder holder) {
        super.onBind(holder);

        ReplyViewBinder.onBind(replyView, replyTextView, replyImageView, holder);
        ReadStatusViewBinder.onBind(readStatus, holder);
        MessageBinder.onBindSendStatus(time, holder);

//        time.setVisibility(View.VISIBLE);
//        if (holder.getStatus() == MessageSendStatus.Sent) {
//            Message nextMessage = message.getNextMessage();
//
//            // Hide the time if it's the same as the next message
//            if (nextMessage != null && format.format(message.getDate().toDate()).equals(format.format(nextMessage.getDate().toDate()))) {
//                time.setVisibility(View.GONE);
//            }
//        } else {
//        }



    }


}
