package sdk.chat.ui.view_holders.base;

import android.content.Context;
import android.os.Build;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.lang.ref.WeakReference;
import java.text.DateFormat;



import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.DrawableUtil;

@Deprecated
public class BaseIncomingTextMessageViewHolder<T extends MessageHolder>
        extends MessageHolders.IncomingTextMessageViewHolder<T>  {

    @Nullable protected ImageView messageIcon;
    protected View onlineIndicator;
    protected TextView userName;
    @Nullable protected View replyView;
    @Nullable protected ImageView replyImageView;
    @Nullable protected TextView replyTextView;

    protected View itemView;

    protected WeakReference<Context> context;
    protected DateFormat format;

    public BaseIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        this.itemView = itemView;

        messageIcon = itemView.findViewById(R.id.messageIcon);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        userName = itemView.findViewById(R.id.userName);
        replyView = itemView.findViewById(R.id.replyView);
        replyImageView = itemView.findViewById(R.id.replyImageView);
        replyTextView = itemView.findViewById(R.id.replyTextView);

        context = new WeakReference<>(itemView.getContext());

        format = UIModule.shared().getMessageBinder().messageTimeComparisonDateFormat(context.get());
    }

    @Override
    public void onBind(T message) {
        super.onBind(message);

        UIModule.shared().getReplyViewBinder().onBind(replyView, replyTextView, replyImageView, message);
        UIModule.shared().getOnlineStatusBinder().bind(onlineIndicator, message);
        UIModule.shared().getTimeBinder().bind(time, message);

        //We can set onClick listener on view from payload
//        final Payload payload = (Payload) this.payload;
//        userAvatar.setOnClickListener(view -> {
//            if (payload != null && payload.avatarClickListener != null && UIModule.config().startProfileActivityOnChatViewIconClick) {
//                payload.avatarClickListener.onAvatarClick(message.getMessage().getSender());
//            }
//        });

        UIModule.shared().getNameBinder().bind(userName, message);

        UIModule.shared().getIconBinder().bind(messageIcon, message);

        if(text != null) {
            text.setAutoLinkMask(Linkify.ALL);
        }

        // Hide the time if it's the same as the next message
        if (!message.showDate()) {
            time.setVisibility(View.GONE);
        } else {
            time.setVisibility(View.VISIBLE);
        }

        // Color state lists don't work for old versions of Android
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Context context = bubble.getContext();
            bubble.setBackground(DrawableUtil.getMessageSelector(
                    context,
                    R.attr.incomingDefaultBubbleColor,
                    R.attr.incomingDefaultBubbleSelectedColor,
                    R.attr.incomingDefaultBubblePressedColor,
                    R.attr.incomingBubbleDrawable));
        }



    }

}
