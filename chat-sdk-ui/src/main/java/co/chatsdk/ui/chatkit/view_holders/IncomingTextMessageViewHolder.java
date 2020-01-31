package co.chatsdk.ui.chatkit.view_holders;

import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chatkit.binders.MessageBinder;
import co.chatsdk.ui.chatkit.binders.ReadStatusViewBinder;
import co.chatsdk.ui.chatkit.binders.ReplyViewBinder;
import co.chatsdk.ui.chatkit.model.MessageHolder;

public class IncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<MessageHolder>  {

    private View onlineIndicator;
    protected TextView userName;
    protected WeakReference<Context> context;

    protected View replyView;
    protected ImageView replyImageView;
    protected TextView replyTextView;
    protected DateFormat format;
    protected View itemView;

    public IncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        this.itemView = itemView;

        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        userName = itemView.findViewById(R.id.userName);
        replyView = itemView.findViewById(R.id.replyView);
        replyImageView = itemView.findViewById(R.id.replyImageView);
        replyTextView = itemView.findViewById(R.id.replyTextView);

        context = new WeakReference<>(itemView.getContext());

        format = MessageBinder.messageTimeComparisonDateFormat(context.get());
    }

    @Override
    public void onBind(MessageHolder holder) {
        super.onBind(holder);

        ReplyViewBinder.onBind(replyView, replyTextView, replyImageView, holder);

        boolean isOnline = holder.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_offline);
        }

        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(view -> {
            if (payload != null && payload.avatarClickListener != null) {
                payload.avatarClickListener.onAvatarClick(holder.getMessage().getSender());
            }
        });

//        Message message = holder.getMessage();
//        Message previousMessage = message.getPreviousMessage();
//        Message nextMessage = message.getNextMessage();

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
