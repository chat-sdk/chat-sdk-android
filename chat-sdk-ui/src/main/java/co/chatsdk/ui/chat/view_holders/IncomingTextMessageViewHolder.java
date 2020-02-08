package co.chatsdk.ui.chat.view_holders;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.chat.binders.MessageBinder;
import co.chatsdk.ui.chat.binders.OnlineStatusBinder;
import co.chatsdk.ui.chat.binders.ReadStatusViewBinder;
import co.chatsdk.ui.chat.binders.ReplyViewBinder;
import co.chatsdk.ui.chat.model.MessageHolder;

public class IncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<MessageHolder>  {

    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.userName) protected TextView userName;
    @BindView(R2.id.replyView) protected View replyView;
    @BindView(R2.id.replyImageView) protected ImageView replyImageView;
    @BindView(R2.id.replyTextView) protected TextView replyTextView;

    protected View itemView;

    protected WeakReference<Context> context;
    protected DateFormat format;

    public IncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);

        context = new WeakReference<>(itemView.getContext());

        format = MessageBinder.messageTimeComparisonDateFormat(context.get());
    }

    @Override
    public void onBind(MessageHolder holder) {
        super.onBind(holder);

        ReplyViewBinder.onBind(replyView, replyTextView, replyImageView, holder);

        boolean isOnline = holder.getUser().isOnline();
        OnlineStatusBinder.bind(onlineIndicator, isOnline);

        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(view -> {
            if (payload != null && payload.avatarClickListener != null) {
                payload.avatarClickListener.onAvatarClick(holder.getMessage().getSender());
            }
        });

        if (holder.showNames()) {
            userName.setVisibility(View.VISIBLE);
            userName.setText(holder.getUser().getName());
        } else {
            userName.setVisibility(View.GONE);
        }

        // Hide the time if it's the same as the next message
        if (!holder.showDate()) {
            time.setVisibility(View.GONE);
        } else {
            time.setVisibility(View.VISIBLE);
            // https://stackoverflow.com/questions/12728255/in-android-how-do-i-set-margins-in-dp-programmatically
//            if (itemView instanceof RelativeLayout) {
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) itemView.getLayoutParams();
//                params.setMargins(16, 8, );
//            }
//
//            itemView.setMar

        }


    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    public interface OnAvatarClickListener {
        void onAvatarClick(User user);
    }

    protected Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }
}
