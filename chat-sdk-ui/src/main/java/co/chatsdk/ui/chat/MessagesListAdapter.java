/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.CustomMessageHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.Progress;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.ui.R;
import timber.log.Timber;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.MessageViewHolder> {

    public static int ViewTypeMine = 1;
    public static int ViewTypeReply = 2;

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView avatarImageView;
        public TextView timeTextView;
        public SimpleDraweeView messageImageView;
        public ConstraintLayout messageBubble;
        public TextView messageTextView;
        public ImageView messageIconView;
        public LinearLayout extraLayout;
        public ImageView readReceiptImageView;
        public MessageListItem messageItem;
        public ProgressBar progressBar;
        protected View.OnClickListener onClickListener = null;
        protected View.OnLongClickListener onLongClickListener = null;

        public MessageViewHolder(View itemView) {
            super(itemView);

            timeTextView = itemView.findViewById(R.id.txt_time);
            avatarImageView = itemView.findViewById(R.id.avatar);
            messageBubble = itemView.findViewById(R.id.message_bubble);
            messageTextView = itemView.findViewById(R.id.txt_content);
            messageIconView = itemView.findViewById(R.id.icon);
            messageImageView = itemView.findViewById(R.id.image);
            extraLayout = itemView.findViewById(R.id.extra_layout);
            readReceiptImageView = itemView.findViewById(R.id.read_receipt);
            progressBar = itemView.findViewById(R.id.progress_bar);

            itemView.setOnClickListener(view -> {
                if (onClickListener != null) {
                    onClickListener.onClick(view);
                }
                else if (messageItem.getMessage().getMessageType() == MessageType.Location) {
                    LocationMessageOnClickHandler.onClick(activity, messageItem.getLatLng());
                }
                else if (messageItem.getMessage().getMessageType() == MessageType.Image) {
                    ImageMessageOnClickHandler.onClick(activity, view, messageItem.getImageURL());
                }
            });

            itemView.setOnLongClickListener(v -> {

                if (onLongClickListener != null) {
                    return onLongClickListener.onLongClick(v);
                }
                else {

                    if (!messageItem.message.getSender().isMe()) {
                        return false;
                    }

                    Context context = v.getContext();

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(itemView.getContext().getString(R.string.delete_message));

                    // Set up the buttons
                    builder.setPositiveButton(context.getString(R.string.delete), (dialog, which) -> {
                        try {
                            ChatSDK.thread().deleteMessage(messageItem.message).subscribe( new CrashReportingCompletableObserver());
                        }
                        catch (NoSuchMethodError e) {
                            ChatSDK.logError(e);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

                    builder.show();

                    return false;
                }
            });

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

        public void setOnClickListener (View.OnClickListener listener) {
            onClickListener = listener;
        }

        public void setOnLongClickListener (View.OnLongClickListener listener) {
            onLongClickListener = listener;
        }

        public void setIconSize(int width, int height) {
            messageIconView.getLayoutParams().width = width;
            messageIconView.getLayoutParams().height = height;
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
                setIconSize(activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_icon_message_width), activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_icon_message_height));
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
    }

    protected AppCompatActivity activity;

    protected List<MessageListItem> messageItems = new ArrayList<>();

    public MessagesListAdapter(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = null;
        if(viewType == ViewTypeMine) {
            row = inflater.inflate(R.layout.chat_sdk_row_message_me , null);
        }
        if(viewType == ViewTypeReply) {
            row = inflater.inflate(R.layout.chat_sdk_row_message_reply , null);
        }
        return new MessageViewHolder(row);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        MessageListItem messageItem = getMessageItems().get(position);
        holder.messageItem = messageItem;

        // Enable linkify
        holder.messageTextView.setAutoLinkMask(Linkify.ALL);

        holder.setBubbleHidden(true);
        holder.setTextHidden(true);
        holder.setIconHidden(true);
        holder.setImageHidden(true);

        if (messageItem.status().equals(MessageSendStatus.Uploading) || (messageItem.progress > 0 && messageItem.progress < 1)) {
            holder.showProgressBar(messageItem.progress);
        }
        else {
            holder.hideProgressBar();
        }

        if(holder.readReceiptImageView != null) {
            holder.readReceiptImageView.setVisibility(ChatSDK.readReceipts() != null ? View.VISIBLE : View.INVISIBLE);
        }

        if (messageItem.getMessage().getMessageType() == MessageType.Text) {
            holder.messageTextView.setText(messageItem.getMessage().getTextString() == null ? "" : messageItem.getMessage().getTextString());
            holder.setBubbleHidden(false);
            holder.setTextHidden(false);
        }
        else if (messageItem.getMessage().messageTypeIs(MessageType.Location, MessageType.Image)) {

            holder.setImageHidden(false);

            int viewWidth = maxWidth();
            int viewHeight = maxHeight();

            if (messageItem.getMessage().getMessageType() == MessageType.Location) {
                LatLng latLng = messageItem.getLatLng();
                holder.messageImageView.setImageURI(GoogleUtils.getMapImageURL(latLng, viewWidth, viewHeight));
            }

            if (messageItem.getMessage().getMessageType() == MessageType.Image) {

                String url = messageItem.getImageURL();

                Timber.d(messageItem.status().name());

                if(url != null && url.length() > 0) {
                    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                            .setResizeOptions(new ResizeOptions(viewWidth, viewHeight))
                            .build();

                    holder.messageImageView.setController(
                            Fresco.newDraweeControllerBuilder()
                                    .setOldController(holder.messageImageView.getController())
                                    .setImageRequest(request)
                                    .build());
                }
                else {
                    // Loads the placeholder
                    holder.messageImageView.setActualImageResource(R.drawable.icn_200_image_message_loading);
//                    holder.messageImageView.setImageURI(url);
                }
            }
        }
        else if (messageItem.getMessage().getMessageType() == MessageType.File) {
            holder.setBubbleHidden(false);
            holder.setTextHidden(false);
            holder.setIconHidden(false);
        }

        for(CustomMessageHandler handler : ChatSDK.ui().getCustomMessageHandlers()) {
            handler.updateMessageCellView(messageItem.message, holder, activity);
        }

        // Set the time of the sending.
        holder.timeTextView.setText(messageItem.getTime());

        float alpha = messageItem.statusIs(MessageSendStatus.Sent) || messageItem.statusIs(MessageSendStatus.Delivered) ? 1.0f : 0.7f;
        holder.messageImageView.setAlpha(alpha);
        holder.messageTextView.setAlpha(alpha);
        holder.extraLayout.setAlpha(alpha);

        holder.avatarImageView.setImageURI(messageItem.getMessage().getSender().getAvatarURL());

        if (messageItem.message.getSender().isMe()) {
            holder.messageTextView.setTextColor(ChatSDK.config().messageTextColorMe);
            holder.messageBubble.getBackground().setColorFilter(ChatSDK.config().messageColorMe, PorterDuff.Mode.MULTIPLY);
        }
        else {
            holder.messageTextView.setTextColor(ChatSDK.config().messageTextColorReply);
            holder.messageBubble.getBackground().setColorFilter(ChatSDK.config().messageColorReply, PorterDuff.Mode.MULTIPLY);
        }

        updateReadStatus(holder, messageItem.message);
    }

    @Override
    public int getItemViewType(int position) {
        return messageItems.get(position).getMessage().getSender().isMe() ? ViewTypeMine : ViewTypeReply;
    }

    @Override
    public long getItemId(int i) {
        return messageItems.get(i).getId();
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    protected void updateReadStatus (MessageViewHolder holder, Message message) {
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
        if(holder.readReceiptImageView != null) {
            holder.readReceiptImageView.setImageResource(resource);
            holder.readReceiptImageView.setVisibility(status.is(ReadStatus.hide()) ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public List<MessageListItem> getMessageItems() {
        return messageItems;
    }

    protected boolean addRow(MessageListItem item, boolean sort, boolean notify){
        if (item == null)
            return false;

        // Don't add message that does not have entity id and the status of the message is not sending.
        if (item.getEntityID() == null) {
            return false;
        }

        Timber.d("Add Message Item: " + item.message.getTextString());
        messageItems.add(item);

        if(sort) {
            sort();
        }

        if(notify) {
            notifyDataSetChanged();
        }

        return true;
    }

    public void sort () {
        Collections.sort(messageItems, new MessageItemSorter(DaoCore.ORDER_DESC));
    }

    public void sortAndNotify () {
        sort();
        notifyDataSetChanged();
    }

    /**
     * Add a new message to the list.
     * @return true if the item is added to the list.
     * */
    public boolean addRow(Message message){
        return addRow(message, true, true);
    }

    public boolean addRow(Message message, boolean sort, boolean notify, Progress progress){
        MessageListItem item = messageItemForMessage(message);
        boolean returnStatus = false;
        if (item == null) {
            item = new MessageListItem(message);
            returnStatus = addRow(item, sort, notify);
        }
        if (progress != null) {
            item.progress = progress.asFraction();
        }
        return returnStatus;
    }

    public boolean addRow(Message message, boolean sort, boolean notify) {
        return addRow(message, sort, notify, null);
    }

    public boolean removeRow (Message message, boolean notify) {
        MessageListItem item = messageItemForMessage(message);
        if (item != null) {
            messageItems.remove(item);
            if (notify) {
                notifyDataSetChanged();
            }
            return true;
        }
        return false;
    }

    protected boolean messageExists (Message message) {
        return messageItemForMessage(message) != null;
    }

    protected MessageListItem messageItemForMessage (Message message) {
        for(MessageListItem i : messageItems) {
            if(i.message.getEntityID().equals(message.getEntityID())) {
                return i;
            }
        }
        return null;
    }

    public int maxWidth () {
        return activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width);
    }

    public int maxHeight () {
        return activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_height);
    }

    /**
     * Clear the messages list.
     * */
    public void clear() {
        clear(true);
    }

    public void clear(boolean notify) {
        messageItems.clear();
        if(notify) {
            notifyDataSetChanged();
        }
    }

    public void setMessages(List<Message> messages) {
        clear(false);
        for (Message message : messages) {
            addRow(message, false, false);
        }
        sortAndNotify();
    }

    // Untested because upload progress doesn't work
    public void setProgressForMessage (Message message, float progress) {
        MessageListItem item = messageListItemForMessage(message);
        if(item != null) {
            item.progress = progress;
        }
        notifyDataSetChanged();
    }

    public MessageListItem messageListItemForMessage (Message message) {
        for(MessageListItem i : messageItems) {
            if(i.message.equals(message)) {
                return i;
            }
        }
        return null;
    }

    public int size () {
        return messageItems.size();
    }

}
