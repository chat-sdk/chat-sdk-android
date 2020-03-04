package co.chatsdk.ui.view_holders.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.IconBinder;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.ReadStatusViewBinder;
import co.chatsdk.ui.binders.ReplyViewBinder;
import co.chatsdk.ui.chat.model.MessageHolder;

public class BaseOutcomingTextMessageViewHolder<T extends MessageHolder>
        extends MessageHolders.OutcomingTextMessageViewHolder<T> {

    @BindView(R2.id.messageIcon) @Nullable protected ImageView messageIcon;
    @BindView(R2.id.readStatus) protected ImageView readStatus;
    @BindView(R2.id.replyView) @Nullable protected View replyView;
    @BindView(R2.id.replyImageView) @Nullable protected ImageView replyImageView;
    @BindView(R2.id.replyTextView) @Nullable protected TextView replyTextView;

    public BaseOutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        bindButterKnife();
   }

    public void bindButterKnife() {
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(T message) {
        super.onBind(message);

        ReplyViewBinder.onBind(replyView, replyTextView, replyImageView, message);
        ReadStatusViewBinder.onBind(readStatus, message);
        MessageBinder.onBindSendStatus(time, message);
        IconBinder.bind(messageIcon, imageLoader, message);


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
