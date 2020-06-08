package sdk.chat.ui.view_holders.base;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.binders.IconBinder;
import sdk.chat.ui.binders.MessageBinder;
import sdk.chat.ui.binders.ReadStatusViewBinder;
import sdk.chat.ui.binders.ReplyViewBinder;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.utils.DrawableUtil;

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

        ReplyViewBinder.onBind(replyView, replyTextView, replyImageView, message, imageLoader);
        ReadStatusViewBinder.onBind(readStatus, message);
        MessageBinder.onBindSendStatus(time, message);
        IconBinder.bind(messageIcon, message, imageLoader);

        // Color state lists don't work for old versions of Android
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Context context = bubble.getContext();
            bubble.setBackground(DrawableUtil.getMessageSelector(
                    context,
                    R.attr.outcomingDefaultBubbleColor,
                    R.attr.outcomingDefaultBubbleSelectedColor,
                    R.attr.outcomingDefaultBubblePressedColor,
                    R.attr.outcomingBubbleDrawable));
        }
    }
}
