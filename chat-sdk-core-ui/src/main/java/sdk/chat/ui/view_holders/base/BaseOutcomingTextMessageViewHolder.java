package sdk.chat.ui.view_holders.base;

import android.content.Context;
import android.os.Build;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.messages.MessageHolders;



import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.DrawableUtil;

@Deprecated
public class BaseOutcomingTextMessageViewHolder<T extends MessageHolder>
        extends MessageHolders.OutcomingTextMessageViewHolder<T> {

    @Nullable protected ImageView messageIcon;
    protected ImageView readStatus;
    @Nullable protected View replyView;
    @Nullable protected ImageView replyImageView;
    @Nullable protected TextView replyTextView;

    public BaseOutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

        messageIcon = itemView.findViewById(R.id.messageIcon);
        readStatus = itemView.findViewById(R.id.readStatus);
        replyView = itemView.findViewById(R.id.replyView);
        replyImageView = itemView.findViewById(R.id.replyImageView);
        replyTextView = itemView.findViewById(R.id.replyTextView);

    }

    @Override
    public void onBind(T message) {
        super.onBind(message);

        UIModule.shared().getReplyViewBinder().onBind(replyView, replyTextView, replyImageView, message);
        UIModule.shared().getReadStatusViewBinder().onBind(readStatus, message);
        UIModule.shared().getMessageBinder().onBindSendStatus(time, message);
        UIModule.shared().getIconBinder().bind(messageIcon, message);
//        UIModule.shared().getTimeBinder().bind(time, message);

        if(text != null) {
            text.setAutoLinkMask(Linkify.ALL);
        }

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
