package sdk.chat.ui.view_holders;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.ui.chat.model.TypingThreadHolder;
import sdk.chat.ui.module.UIModule;
import sdk.guru.common.DisposableMap;

public class ThreadViewHolder extends DialogsListAdapter.DialogViewHolder<ThreadHolder> {

    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.readStatus) protected ImageView readStatus;

    protected DisposableMap dm = new DisposableMap();
    protected TypingThreadHolder typingThreadHolder = null;

    public ThreadViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(ThreadHolder holder) {

        if (typingThreadHolder != null) {
            holder = typingThreadHolder;
        }

        super.onBind(holder);

        ChatSDKUI.provider().imageLoader().loadThread(ivAvatar, holder.getDialogPhoto(), holder.getThread().typeIs(ThreadType.Group), R.dimen.thread_image_size);

        if (holder instanceof TypingThreadHolder) {
            @ColorRes int color = UIModule.config().threadViewHolderTypingTextColor;
            if (color != 0) {
                tvLastMessage.setTextColor(ContextCompat.getColor(tvLastMessage.getContext(), color));
            }
        }

        // Show the thread created date if there are no messages
        if (tvDate.getText() == null || tvDate.length() == 0) {
            Date date = holder.getDate();

            if (this.datesFormatter != null) {
                tvDate.setText(this.datesFormatter.format(date));
            } else {
                tvDate.setText(getDateString(date));
            }
        }

        bindOnlineIndicator(holder);
        bindReadStatus(holder);
    }

    public void bindReadStatus(ThreadHolder holder) {
        UIModule.shared().getReadStatusViewBinder().onBind(readStatus, holder.getLastMessage());
    }

    public void bindOnlineIndicator(ThreadHolder holder) {
        if (holder.getThread().typeIs(ThreadType.Private1to1)) {
            onlineIndicator.setVisibility(View.VISIBLE);
            boolean isOnline = false;
            if (holder.getThread().otherUser() != null) {
                isOnline = holder.getThread().otherUser().getIsOnline();
            }
            UIModule.shared().getOnlineStatusBinder().bind(onlineIndicator, isOnline);
        } else {
            onlineIndicator.setVisibility(View.GONE);
        }
    }

//    public void addListeners(ThreadHolder holder) {
//        dm.dispose();
//
//
//    }

}
