package co.chatsdk.ui.view_holders.base;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.IconBinder;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.NameBinder;
import co.chatsdk.ui.binders.OnlineStatusBinder;
import co.chatsdk.ui.binders.ReplyViewBinder;
import co.chatsdk.ui.chat.model.MessageHolder;

public class BaseIncomingTextMessageViewHolder<T extends MessageHolder>
        extends MessageHolders.IncomingTextMessageViewHolder<T>  {

    @BindView(R2.id.messageIcon) @Nullable protected ImageView messageIcon;
    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.userName) protected TextView userName;
    @BindView(R2.id.replyView) @Nullable protected View replyView;
    @BindView(R2.id.replyImageView) @Nullable protected ImageView replyImageView;
    @BindView(R2.id.replyTextView) @Nullable protected TextView replyTextView;

    protected View itemView;

    protected WeakReference<Context> context;
    protected DateFormat format;

    public BaseIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        this.itemView = itemView;
        bindButterKnife();

        context = new WeakReference<>(itemView.getContext());

        format = MessageBinder.messageTimeComparisonDateFormat(context.get());
    }

    protected void bindButterKnife() {
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(T message) {
        super.onBind(message);

        ReplyViewBinder.onBind(replyView, replyTextView, replyImageView, message);

        OnlineStatusBinder.bind(onlineIndicator, message);

        //We can set onClick listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(view -> {
            if (payload != null && payload.avatarClickListener != null) {
                payload.avatarClickListener.onAvatarClick(message.getMessage().getSender());
            }
        });

        NameBinder.bind(userName, message);

        IconBinder.bind(messageIcon, imageLoader, message);

        // Hide the time if it's the same as the next message
        if (!message.showDate()) {
            time.setVisibility(View.GONE);
        } else {
            time.setVisibility(View.VISIBLE);
        }

    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    public interface OnAvatarClickListener {
        void onAvatarClick(User user);
    }

}
