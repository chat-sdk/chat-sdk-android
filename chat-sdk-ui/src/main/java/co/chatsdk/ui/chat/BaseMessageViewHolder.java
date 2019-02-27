package co.chatsdk.ui.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import co.chatsdk.core.base.AbstractMessageViewHolder;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.ui.R;

public class BaseMessageViewHolder extends AbstractMessageViewHolder {

    public static final String MoreThanYearFormat = "MM/yy";
    public static final String MoreThanDayFormat = "MMM dd";

    protected SimpleDraweeView avatarImageView;
    protected TextView timeTextView;
    protected SimpleDraweeView messageImageView;
    protected ConstraintLayout messageBubble;
    protected TextView messageTextView;
    protected ImageView messageIconView;
    protected LinearLayout extraLayout;
    protected ImageView readReceiptImageView;
    protected ProgressBar progressBar;

    public BaseMessageViewHolder(View itemView, Activity activity) {
        super(itemView, activity);

        timeTextView = itemView.findViewById(R.id.txt_time);
        avatarImageView = itemView.findViewById(R.id.avatar);
        messageBubble = itemView.findViewById(R.id.message_bubble);
        messageTextView = itemView.findViewById(R.id.txt_content);
        messageIconView = itemView.findViewById(R.id.icon);
        messageImageView = itemView.findViewById(R.id.image);
        extraLayout = itemView.findViewById(R.id.extra_layout);
        readReceiptImageView = itemView.findViewById(R.id.read_receipt);
        progressBar = itemView.findViewById(R.id.progress_bar);

        itemView.setOnClickListener(v -> {
            onClick(v);
        });

        itemView.setOnLongClickListener(v -> {
            return onLongClick(v);
        });

        if(readReceiptImageView != null) {
            readReceiptImageView.setVisibility(ChatSDK.readReceipts() != null ? View.VISIBLE : View.INVISIBLE);
        }

        // Enable linkify
        messageTextView.setAutoLinkMask(Linkify.ALL);

    }

    public void onClick (View v) {
        if (onClickListener != null) {
            onClickListener.onClick(v);
        }
    }

    public boolean onLongClick (View v) {
        if (onLongClickListener != null) {
            onLongClickListener.onLongClick(v);
        } else if (message != null && message.getSender().isMe()) {

            Context context = v.getContext();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(itemView.getContext().getString(R.string.delete_message));

            // Set up the buttons
            builder.setPositiveButton(context.getString(R.string.delete), (dialog, which) -> {
                try {
                    ChatSDK.thread().deleteMessage(message).subscribe( new CrashReportingCompletableObserver());
                }
                catch (NoSuchMethodError e) {
                    ChatSDK.logError(e);
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

            builder.show();

        }
        return false;
    }

    public void setMessage (Message message) {
        super.setMessage(message);

        setBubbleHidden(true);
        setTextHidden(true);
        setIconHidden(true);
        setImageHidden(true);

        float alpha = message.getMessageStatus() == MessageSendStatus.Sent || message.getMessageStatus() == MessageSendStatus.Delivered ? 1.0f : 0.7f;
        setAlpha(alpha);

        String time = String.valueOf(getTimeFormat(message).format(message.getDate().toDate()));
        timeTextView.setText(time);

        avatarImageView.setImageURI(message.getSender().getAvatarURL());

        if (message.getSender().isMe()) {
            messageTextView.setTextColor(ChatSDK.config().messageTextColorMe);
            messageTextView.setPadding(4,0,4,0);
            messageBubble.getBackground().setColorFilter(ChatSDK.config().messageColorMe, PorterDuff.Mode.MULTIPLY);
        }
        else {
            messageTextView.setTextColor(ChatSDK.config().messageTextColorReply);
            messageTextView.setPadding(4,0,4,0);
            messageBubble.getBackground().setColorFilter(ChatSDK.config().messageColorReply, PorterDuff.Mode.MULTIPLY);
        }

        updateReadStatus();
    }

    protected void updateReadStatus () {
        if (message != null) {
            int resource = R.drawable.ic_message_received;
            ReadStatus status = message.getReadStatus();

            // Hide the read receipt for public threads
            if(message.getThread().typeIs(ThreadType.Public) || ChatSDK.readReceipts() == null) {
                status = ReadStatus.hide();
            }

            if(status.is(ReadStatus.delivered())) {
                resource = R.drawable.ic_message_delivered;
            }
            if(status.is(ReadStatus.read())) {
                resource = R.drawable.ic_message_read;
            }
            if(readReceiptImageView != null) {
                readReceiptImageView.setImageResource(resource);
                readReceiptImageView.setVisibility(status.is(ReadStatus.hide()) ? View.INVISIBLE : View.VISIBLE);
            }
        }
    }

    public void setAlpha (float alpha) {
        messageImageView.setAlpha(alpha);
        messageTextView.setAlpha(alpha);
        extraLayout.setAlpha(alpha);
    }

    @Override
    public LinearLayout getExtraLayout() {
        return extraLayout;
    }

    public int maxWidth () {
        return activity.getResources().getDimensionPixelSize(R.dimen.message_image_max_width);
    }

    public int maxHeight () {
        return activity.getResources().getDimensionPixelSize(R.dimen.message_image_max_height);
    }

    public void showProgressBar () {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        progressBar.bringToFront();
    }

    public void showProgressBar (float progress) {
        if (progress == 0) {
            showProgressBar();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setProgress((int) Math.ceil(progress * progressBar.getMax()));
            progressBar.bringToFront();
        }
    }

    public void hideProgressBar () {
        progressBar.setVisibility(View.GONE);
    }

    public void setIconSize(int width, int height) {
        messageIconView.getLayoutParams().width = width;
        messageIconView.getLayoutParams().height = height;
        messageIconView.requestLayout();
    }

    public void setIconMargins(int start, int top, int end, int bottom) {
        // TODO: Make this more elegant...
        ViewGroup.LayoutParams params = messageIconView.getLayoutParams();
//        if (params instanceof RelativeLayout.LayoutParams) {
//            ((RelativeLayout.LayoutParams) messageIconView.getLayoutParams()).setMargins(start, top, end, bottom);
//        }
//        if (params instanceof ConstraintLayout.LayoutParams) {
            ((ConstraintLayout.LayoutParams) messageIconView.getLayoutParams()).setMargins(start, top, end, bottom);
//        }
        messageIconView.requestLayout();
    }

    public void setImageSize(int width, int height) {
        messageImageView.getLayoutParams().width = width;
        messageImageView.getLayoutParams().height = height;
        messageImageView.requestLayout();
    }

    public void setBubbleHidden (boolean hidden) {
        messageBubble.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
        messageBubble.getLayoutParams().width = hidden ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        messageBubble.getLayoutParams().height = hidden ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        messageBubble.requestLayout();
    }

    public void setIconHidden (boolean hidden) {
        messageIconView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
        if (hidden) {
            setIconSize(0, 0);
            setIconMargins(0,0,0,0);
        } else {
            setIconSize(activity.getResources().getDimensionPixelSize(R.dimen.message_icon_max_width), activity.getResources().getDimensionPixelSize(R.dimen.message_icon_max_height));
            setIconMargins(5,0,5,0);
        }
        messageBubble.requestLayout();
    }

    public void setImageHidden (boolean hidden) {
        messageImageView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
        if (hidden) {
            setImageSize(0, 0);
        } else {
//                setImageSize(maxWidth(), maxHeight());
            setImageSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

    }

    public void setTextHidden (boolean hidden) {
        messageTextView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
        ConstraintLayout.LayoutParams textLayoutParams = (ConstraintLayout.LayoutParams) messageTextView.getLayoutParams();
        if(hidden) {
            textLayoutParams.width = 0;
            textLayoutParams.height = 0;
        }
        else {
            textLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            textLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        messageTextView.setLayoutParams(textLayoutParams);
        messageTextView.requestLayout();
        messageBubble.requestLayout();
    }

    public View viewForClassType (Class classType) {
        for (int i = 0; i < extraLayout.getChildCount(); i++) {
            View view = extraLayout.getChildAt(i);
            if (classType.isInstance(view)) {
                return view;
            }
        }
        return null;
    }

    protected SimpleDateFormat getTimeFormat(Message message){

        Date curTime = new Date();
        long interval = (curTime.getTime() - message.getDate().toDate().getTime()) / 1000L;

        String dateFormat = ChatSDK.config().messageTimeFormat;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());

        // More then a day ago
        if (interval < 3600 * 24) {
            return simpleDateFormat;
        }
        else if (interval < 3600 * 24 * 365) {
            simpleDateFormat.applyPattern(dateFormat + " " +MoreThanDayFormat);
        }
        else {
            simpleDateFormat.applyPattern(dateFormat + " " +MoreThanYearFormat);
        }
        return simpleDateFormat;
    }

}
