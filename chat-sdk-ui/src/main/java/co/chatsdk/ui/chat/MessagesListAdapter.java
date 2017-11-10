/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.R;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.MessageViewHolder> {

    public static int ViewTypeMine = 1;
    public static int ViewTypeReply = 2;

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView avatarImageView;
        public TextView timeTextView;
        public SimpleDraweeView messageImageView;
        public TextView messageTextView;
        public LinearLayout extraLayout;
        public ImageView readReceiptImageView;
        public MessageListItem messageItem;

        public MessageViewHolder(View itemView) {
            super(itemView);

            timeTextView = (TextView) itemView.findViewById(R.id.txt_time);
            avatarImageView = (SimpleDraweeView) itemView.findViewById(R.id.img_user_image);
            messageTextView = (TextView) itemView.findViewById(R.id.txt_content);
            messageImageView = (SimpleDraweeView) itemView.findViewById(R.id.chat_sdk_image);
            extraLayout = (LinearLayout) itemView.findViewById(R.id.extra_layout);
            readReceiptImageView = (ImageView) itemView.findViewById(R.id.read_receipt);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (messageItem.getMessage().getMessageType() == MessageType.Location) {
                        LatLng latLng = messageItem.getLatLng();
                        new LocationMessageClickListener(activity, latLng).onClick(view);
                    }
                    else if (messageItem.getMessage().getMessageType() == MessageType.Image) {
                        new ImageMessageClickListener(activity, messageItem.getImageURL()).onClick(view);
                    }
                }
            });
        }

        public void setImageHidden (boolean hidden) {
            messageImageView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
            LinearLayout.LayoutParams imageLayoutParams = (LinearLayout.LayoutParams) messageImageView.getLayoutParams();
            if(hidden) {
                imageLayoutParams.width = 0;
                imageLayoutParams.height = 0;
            }
            else {
                imageLayoutParams.width = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width);
                imageLayoutParams.height = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_height);
            }
            messageImageView.setLayoutParams(imageLayoutParams);
            messageImageView.requestLayout();

        }
        public void setTextHidden (boolean hidden) {
            messageTextView.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
            LinearLayout.LayoutParams textLayoutParams = (LinearLayout.LayoutParams) messageTextView.getLayoutParams();
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
        }
    }

    private AppCompatActivity activity;

    private List<MessageListItem> messageItems = new ArrayList<>();

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

        MessageListItem messageItem = messageItems.get(position);
        holder.messageItem = messageItem;

        holder.setTextHidden(true);
        holder.setImageHidden(true);

        if(holder.readReceiptImageView != null) {
            holder.readReceiptImageView.setVisibility(NM.readReceipts() != null ? View.VISIBLE : View.INVISIBLE);
        }

        if (messageItem.getMessage().getMessageType() == MessageType.Text) {
            holder.messageTextView.setText(messageItem.getMessage().getTextString() == null ? "" : messageItem.getMessage().getTextString());
            holder.setTextHidden(false);
        }
        else if (messageItem.getMessage().getMessageType() == MessageType.Location || messageItem.getMessage().getMessageType() == MessageType.Image) {

            holder.setImageHidden(false);

            int viewWidth = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width);
            int viewHeight = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_height);

            if (messageItem.getMessage().getMessageType() == MessageType.Location) {
                LatLng latLng = messageItem.getLatLng();
                holder.messageImageView.setImageURI(GoogleUtils.getMapImageURL(latLng, viewWidth, viewHeight));
            }

            if (messageItem.getMessage().getMessageType() == MessageType.Image) {

                String url = messageItem.getImageURL();


                if(url != null) {
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
                    holder.messageImageView.setImageURI(url);
                }
            }
        }

        for(CustomMessageHandler handler : InterfaceManager.shared().a.getCustomMessageHandlers()) {
            handler.updateMessageCellView(messageItem.message, holder, activity);
        }

        // Set the time of the sending.
        holder.timeTextView.setText(messageItem.getTime());

        float alpha = messageItem.statusIs(MessageSendStatus.Sent) || messageItem.statusIs(MessageSendStatus.Delivered) ? 1.0f : 0.7f;
        holder.messageImageView.setAlpha(alpha);
        holder.messageTextView.setAlpha(alpha);
        holder.extraLayout.setAlpha(alpha);

        holder.avatarImageView.setImageURI(messageItem.getMessage().getSender().getAvatarURL());

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

    private void updateReadStatus (MessageViewHolder holder, Message message) {
        int resource = R.drawable.ic_message_received;
        ReadStatus status = message.getReadStatus();

        // Hide the read receipt for public threads
        if(message.getThread().typeIs(ThreadType.Public) || NM.readReceipts() == null) {
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

    private boolean addRow(MessageListItem item, boolean sort, boolean notify){
        if (item == null)
            return false;

        // Don't add message that does not have entity id and the status of the message is not sending.
        if (item.getEntityID() == null) {
            return false;
        }

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

    public boolean addRow(Message message, boolean sort, boolean notify){
        if(message != null && !messageExists(message)) {
            MessageListItem item = new MessageListItem(message, maxWidth());
            return addRow(item, sort, notify);
        }
        return false;
    }

    private boolean messageExists (Message message) {
        for(MessageListItem i : messageItems) {
            if(i.message.getEntityID().equals(message.getEntityID())) {
                return true;
            }
        }
        return false;
    }

    public int maxWidth () {
        return activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width);
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
