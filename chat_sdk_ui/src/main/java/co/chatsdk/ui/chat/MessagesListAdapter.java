/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.sorter.MessageSorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.chatsdk.ui.utils.UserAvatarHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MessagesListAdapter extends BaseAdapter{

    /**
     * Class to hold the row child views so we wont have to inflate more them once per row view.
     **/
    class ViewHolder {
        CircleImageView profileImageView;
        TextView timeTextView;
        RoundedImageView messageImageView;
        TextView messageTextView;
        LinearLayout extraLayout;
    }

    private AppCompatActivity activity;

    private List<MessageListItem> messageItems = new ArrayList<>();

    private boolean isScrolling = false;

    private LayoutInflater inflater;

    public MessagesListAdapter(AppCompatActivity activity) {
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return messageItems.get(position).isMine() ? 0 : 1;
    }

    @Override
    public int getCount() {
        return messageItems.size();
    }

    @Override
    public MessageListItem getItem(int i) {
        return messageItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return messageItems.get(i).getId();
    }

    @Override
    public View getView(int position, View row, ViewGroup viewGroup) {

        final MessageListItem messageItem = messageItems.get(position);

        if (row == null) {
            row = inflateRow(messageItem);
        }
        ViewHolder holder = (ViewHolder) row.getTag();

        updateMessageCell(row, holder, messageItem);

        return row;
    }

    /**
     * Inflating the row for type.
     *
     * By Overriding this function you can inflate a custom type of a message.
     *
     * You can keep the result for the super call and see if it is null if so check for your custom inflation or just check for type.
     *
     * */
    protected View inflateRow(MessageListItem item){
        ViewHolder holder = new ViewHolder();

        int resource = item.isMine() ? R.layout.chat_sdk_row_message_me : R.layout.chat_sdk_row_message_reply;

        View row = inflater.inflate(resource, null);

        holder.timeTextView = (TextView) row.findViewById(R.id.txt_time);
        holder.profileImageView = (CircleImageView) row.findViewById(R.id.img_user_image);
        holder.messageTextView = (TextView) row.findViewById(R.id.txt_content);
        holder.messageImageView = (RoundedImageView) row.findViewById(R.id.chat_sdk_image);
        holder.extraLayout = (LinearLayout) row.findViewById(R.id.extra_layout);

        row.setTag(holder);
        return row;
    }

    /**
     * Load the default bundle for each message, The bundle will be loaded for each message and be animated if needed.
     *
     * By Overriding this function you change or add logic for your default message bundle load,
     * For example load online status for each user.
     *
     * */
    protected void updateMessageCell(View row, final ViewHolder holder, MessageListItem messageItem){

        holder.messageImageView.setVisibility(View.INVISIBLE);
        holder.messageTextView.setVisibility(View.INVISIBLE);

        Picasso.with(holder.messageImageView.getContext()).cancelRequest(holder.messageImageView);
        Picasso.with(holder.profileImageView.getContext()).cancelRequest(holder.profileImageView);

        LinearLayout.LayoutParams imageLayoutParams = (LinearLayout.LayoutParams) holder.messageImageView.getLayoutParams();
        LinearLayout.LayoutParams textLayoutParams = (LinearLayout.LayoutParams) holder.messageTextView.getLayoutParams();

        imageLayoutParams.width = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width);
        imageLayoutParams.height = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_height);

        textLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        textLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        if (messageItem.messageType() == MessageType.Text) {

            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(messageItem.getText() == null ? "" : messageItem.getText());

            imageLayoutParams.width = 0;
            imageLayoutParams.height = 0;
        }
        else {
            textLayoutParams.width = 0;
            textLayoutParams.height = 0;
        }

        if (messageItem.messageType() == MessageType.Location || messageItem.messageType() == MessageType.Image) {

            holder.messageImageView.setVisibility(View.VISIBLE);

            int width = messageItem.width();
            int height = messageItem.height();

            if (messageItem.messageType() == MessageType.Location) {

                double longitude = (Double) messageItem.message.valueForKey(Keys.MessageLongitude);
                double latitude = (Double) messageItem.message.valueForKey(Keys.MessageLatitude);

                LatLng latLng = new LatLng(latitude, longitude);

                Picasso.with(holder.messageImageView.getContext()).load(GoogleUtils.getMapImageURL(latLng, width, height)).placeholder(R.drawable.icn_200_image_message_placeholder).into(holder.messageImageView);
//                holder.imageViewFuture = Ion.with(holder.messageImageView).placeholder(R.drawable.icn_200_image_message_placeholder)
//                        .load(GoogleUtils.getMapImageURL(latLng, width, height));

                // Open google maps on click.
                holder.messageImageView.setOnClickListener(new LocationMessageClickListener(activity, latLng));
            }

            if (messageItem.messageType() == MessageType.Image) {

                String url = (String) messageItem.message.valueForKey(Keys.MessageImageURL);

                if(url == null || url.isEmpty()) {
                    holder.messageImageView.setImageResource(R.drawable.icn_200_image_message_placeholder);
                }
                else {
                    Picasso.with(holder.messageImageView.getContext()).load(url).placeholder(R.drawable.icn_200_image_message_placeholder).into(holder.messageImageView);
                }

                // Show the messageImageView in a dialog on click.
                holder.messageImageView.setOnClickListener(new ImageMessageClickListener(activity, url, messageItem.message.getEntityID()));
            }
        }

        if(messageItem.messageType() == MessageType.Audio && NM.audioMessage() != null) {

            imageLayoutParams.height = 0;
            imageLayoutParams.width = 0;
            textLayoutParams.width = 0;
            textLayoutParams.height = 0;

            NM.audioMessage().updateMessageCellView(messageItem.message, holder.extraLayout, activity);
        }

        holder.messageImageView.setLayoutParams(imageLayoutParams);
        holder.messageImageView.requestLayout();
        holder.messageTextView.setLayoutParams(textLayoutParams);
        holder.messageTextView.requestLayout();

        UserAvatarHelper.loadAvatar(messageItem.getMessage().getSender(), holder.profileImageView)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        // Set the time of the sending.
        holder.timeTextView.setText(messageItem.getTime());

        row.setAlpha(messageItem.statusIs(MessageSendStatus.Sent) || messageItem.statusIs(MessageSendStatus.Delivered) ? 1.0f : 0.7f);
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
        Collections.sort(messageItems, new MessageItemSorter(MessageSorter.ORDER_TYPE_DESC));
    }

    public void sortItemsAndNotify () {
        Collections.sort(messageItems, new MessageItemSorter(MessageSorter.ORDER_TYPE_DESC));
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

            // It's possible that if
            if(item.isValid()) {
                return addRow(item, sort, notify);
            }
            else {
                Timber.v("Invalid message - the message will be deleted");
                DaoCore.deleteEntity(message);
            }
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
        messageItems.clear();
        notifyDataSetChanged();
    }

    public void setMessages(List<Message> messages) {

        clear();

        for (Message message : messages) {
            addRow(message, false, false);
        }
        sortItemsAndNotify();
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

    /**
     * Set the scrolling mode of the list view.
     *
     * We need to keep track of it so we wont animate rows when list view does not scroll.
     * If we do animate when list view does not scroll then there would be multiple animation each time notifyDataSetChanged called.
     * */
    public void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public int size () {
        return messageItems.size();
    }

}
