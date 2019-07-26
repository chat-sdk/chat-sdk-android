package co.chatsdk.ui.chat.viewholder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import co.chatsdk.core.base.AbstractMessageViewHolder;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.message_action.CopyMessageAction;
import co.chatsdk.ui.chat.message_action.DeleteMessageAction;
import co.chatsdk.ui.chat.message_action.ForwardMessageAction;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class BaseMessageViewHolder extends AbstractMessageViewHolder {

    public static final String MoreThanYearFormat = "MM/yy";
    public static final String WeekToYearFormat = "dd/MM";
    public static final String DayToWeekFormat = "EEE";

    protected SimpleDraweeView avatarImageView;
    protected TextView timeTextView;
    protected SimpleDraweeView messageImageView;
    protected ConstraintLayout messageBubble;
    protected TextView messageTextView;
    protected SimpleDraweeView messageIconView;
    protected LinearLayout extraLayout;
    protected ImageView readReceiptImageView;
    protected ProgressBar progressBar;
    protected TextView usernameTextView;

    public BaseMessageViewHolder(View itemView, Activity activity, PublishSubject<List<MessageAction>> actionPublishSubject) {
        super(itemView, activity, actionPublishSubject);

        timeTextView = itemView.findViewById(R.id.text_time);
        avatarImageView = itemView.findViewById(R.id.image_avatar);
        messageBubble = itemView.findViewById(R.id.image_message_bubble);
        messageTextView = itemView.findViewById(R.id.text_content);
        messageIconView = itemView.findViewById(R.id.image_icon);
        messageImageView = itemView.findViewById(R.id.image_message_image);
        extraLayout = itemView.findViewById(R.id.layout_extra);
        readReceiptImageView = itemView.findViewById(R.id.image_read_receipt);
        progressBar = itemView.findViewById(R.id.progress_bar);
        usernameTextView = itemView.findViewById(R.id.username_text);

        itemView.setOnClickListener(this::onClick);
        itemView.setOnLongClickListener(this::onLongClick);

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
        } else if (message != null) {

            ArrayList<MessageAction> actions = new ArrayList<>();

            if (message.getSender().isMe()) {
                actions.add(new DeleteMessageAction(message));
            }
            actions.add(new CopyMessageAction(message));
            actions.add(new ForwardMessageAction(message));

            actionPublishSubject.onNext(actions);
            return true;
        }
        return false;
    }

    public void setMessage (Message message) {
        super.setMessage(message);

        setBubbleHidden(true);
        setTextHidden(true);
        setIconHidden(true);
        setImageHidden(true);
        //these setting have to do with displaying the username and the time on top of the message or not
        setUsernameTextHidden(true);
        setTimeTextViewHidden(false);

        //This is the block that deals with the Username display on top of the message.
        //Only if the message is in a public thread and the sender isn't you is it displayed

        if (!message.getSender().isMe() && message.getThread().typeIs(ThreadType.Public) && ChatSDK.config().messageUserNamesEnabled) {
            setUsernameTextHidden(false);
            setTimeTextViewHidden(true);
        }
        //This part will load the name of the sender and the time into the message display at the top of the message.
        String usernameAndTime = message.getSender().getName() + ", " + String.valueOf(getTimeFormat(message).format(message.getDate().toDate()));
        usernameTextView.setText(usernameAndTime);
        //Now we check to see if the user in in the list of contacts. There names are color code green
        //or red depending on if the user is or is not already in the list of contacts.
        User user = message.getSender();
        if (!ChatSDK.contact().exists(user)) {
            usernameTextView.setTextColor(Color.RED);
        } else {
            usernameTextView.setTextColor(Color.rgb(0, 153, 0));
        }
        //This is the action that is triggered when the user clicks on a contact that is not already in his or her list.
        //I put this block inside of of the setMessage function in order to be able to re-use the usernameTextView.
        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = message.getSender();
                if (!ChatSDK.contact().exists(user)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity.get());
                    alert.setTitle("Add Contact");
                    alert.setMessage("Are you sure you want to add " + message.getSender().getName() + " to your list of contacts?");

                    alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ChatSDK.contact().addContact(user, ConnectionType.Contact)
                                    .subscribe(new Action() {
                                        @Override
                                        public void run() throws Exception {
                                            Toast.makeText(activity.get(), message.getSender().getName() + " has been added to your list of contacts.", Toast.LENGTH_LONG).show();
                                            //This works, but only if the thread does not contain more than one instance of the message containing the contact in question.
                                            setMessage(message);
                                            dialog.cancel();
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(activity.get(), "There was an error, " + message.valueForKey("text") + " could not be added.", Toast.LENGTH_LONG).show();
                                            dialog.cancel();
                                        }
                                    });
                        }
                    });
                    alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                } else {
                    Toast.makeText(activity.get(), "This person is already in your list of contacts", Toast.LENGTH_LONG).show();
                }
            }
        });

        float alpha = message.getMessageStatus() == MessageSendStatus.Sent || message.getMessageStatus() == MessageSendStatus.Delivered ? 1.0f : 0.7f;
        setAlpha(alpha);

        String time = String.valueOf(getTimeFormat(message).format(message.getDate().toDate()));
        timeTextView.setText(time);

        avatarImageView.setImageURI(message.getSender().getAvatarURL());

        if (message.getSender().isMe()) {
            messageBubble.getBackground().setColorFilter(ChatSDK.config().messageColorMe, PorterDuff.Mode.MULTIPLY);
        }
        else {
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
        return activity.get().getResources().getDimensionPixelSize(R.dimen.message_image_max_width);
    }

    public int maxHeight () {
        return activity.get().getResources().getDimensionPixelSize(R.dimen.message_image_max_height);
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
        ((ConstraintLayout.LayoutParams) messageIconView.getLayoutParams()).setMargins(start, top, end, bottom);
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
        } else {
            setIconSize(activity.get().getResources().getDimensionPixelSize(R.dimen.message_icon_max_width), activity.get().getResources().getDimensionPixelSize(R.dimen.message_icon_max_height));
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

    public void setUsernameTextHidden (boolean hidden) {
        usernameTextView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);

        RelativeLayout.LayoutParams textLayoutParams = (RelativeLayout.LayoutParams) usernameTextView.getLayoutParams();
        if(hidden) {
            textLayoutParams.width = 0;
            textLayoutParams.height = 0;
        }
        else {
            textLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            textLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        usernameTextView.setLayoutParams(textLayoutParams);
        usernameTextView.requestLayout();
        messageBubble.requestLayout();
    }

    public void setTimeTextViewHidden (boolean hidden) {
        timeTextView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);

        RelativeLayout.LayoutParams textLayoutParams = (RelativeLayout.LayoutParams) timeTextView.getLayoutParams();
        if(hidden) {
            textLayoutParams.width = 0;
            textLayoutParams.height = 0;
        }
        else {
            textLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            textLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        timeTextView.setLayoutParams(textLayoutParams);
        timeTextView.requestLayout();
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
        else if (interval < 3600 * 24 * 7) {
            simpleDateFormat.applyPattern(dateFormat + " " + DayToWeekFormat);
        }
        else if (interval < 3600 * 24 * 365) {
            simpleDateFormat.applyPattern(dateFormat + " " + WeekToYearFormat);
        }
        else {
            simpleDateFormat.applyPattern(dateFormat + " " + MoreThanYearFormat);
        }
        return simpleDateFormat;
    }

}
