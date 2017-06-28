/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.ion.Ion;
import com.makeramen.roundedimageview.RoundedImageView;

import at.grabner.circleprogress.CircleProgressView;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.DaoDefines;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.sorter.MessageSorter;
import co.chatsdk.ui.Adapters.MessageItemSorter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class MessagesListAdapter extends BaseAdapter{

    private static final String TAG = MessagesListAdapter.class.getSimpleName();
    private static final boolean DEBUG = Debug.MessagesListAdapter;

    /**
     * Class to hold the row child views so we wont have to inflate more them once per row view.
     **/
    class ViewHolder {
        CircleImageView profilePicImageView;
        TextView timeTextView;
        RoundedImageView imageView;
        TextView messageTextView;
        CircleProgressView progressView;
    }

    int maxWidth;

    private AppCompatActivity activity;

    private List<MessageListItem> listData = new ArrayList<>();

    private boolean isScrolling = false;

    private LayoutInflater inflater;

    public MessagesListAdapter(AppCompatActivity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        maxWidth =  (activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width));
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).isMine() ? 0 : 1;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public MessageListItem getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return listData.get(i).getId();
    }

    @Override
    public View getView(int position, View row, ViewGroup viewGroup) {

        final MessageListItem messageItem = listData.get(position);

        if (row == null) {
            row = inflateRow(messageItem);
        }
        ViewHolder holder = (ViewHolder) row.getTag();

        loadData(row, holder, messageItem);

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
        holder.profilePicImageView = (CircleImageView) row.findViewById(R.id.img_user_image);
        holder.messageTextView = (TextView) row.findViewById(R.id.txt_content);
        holder.progressView = (CircleProgressView) row.findViewById(R.id.chat_sdk_progress_view);
        holder.imageView = (RoundedImageView) row.findViewById(R.id.chat_sdk_image);

        switch (item.messageType())
        {
            case BMessage.Type.TEXT:
                holder.messageTextView = (TextView) row.findViewById(R.id.txt_content);
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.progressView.setVisibility(View.INVISIBLE);
                holder.imageView.setVisibility(View.INVISIBLE);
                break;
            case BMessage.Type.IMAGE:
            case BMessage.Type.LOCATION:
                holder.messageTextView.setVisibility(View.INVISIBLE);
                holder.progressView.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.VISIBLE);
                break;
        }
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
    protected void loadData(View row, ViewHolder holder, MessageListItem messageItem){

        holder.progressView.setVisibility(View.INVISIBLE);
        holder.imageView.setVisibility(View.INVISIBLE);
        holder.messageTextView.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();

        params.width = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width);
        params.height = activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width);

        if (messageItem.messageType() == BMessage.Type.TEXT) {

            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(messageItem.text == null ? "" : messageItem.text);

            // Show links in text view if has any.
            holder.messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
            Linkify.addLinks(holder.messageTextView, Linkify.ALL);

            params.width = 0;
            params.height = 0;

            animateContent((View) holder.messageTextView.getParent(), null, messageItem.delivered());
        }

        if (messageItem.messageType() == BMessage.Type.LOCATION || messageItem.messageType() == BMessage.Type.IMAGE) {

            holder.imageView.setVisibility(View.VISIBLE);

            int width = messageItem.width();
            int height = messageItem.height();

            if (messageItem.messageType() == BMessage.Type.LOCATION) {
                double longitude = (Double) messageItem.message.valueForKey(DaoDefines.Keys.MessageLongitude);
                double latitude = (Double) messageItem.message.valueForKey(DaoDefines.Keys.MessageLatitude);

                LatLng latLng = new LatLng(latitude, longitude);

                Ion.with(holder.imageView).placeholder(R.drawable.icn_200_image_message_placeholder).load(GoogleUtils.getMapImageURL(latLng, width, height));

                // Open google maps on click.
                holder.imageView.setOnClickListener(new LocationMessageClickListener(activity, latLng));
            }

            if (messageItem.messageType() == BMessage.Type.IMAGE) {

                String url = (String) messageItem.message.valueForKey(DaoDefines.Keys.MessageImageURL);

                Ion.with(holder.imageView).placeholder(R.drawable.icn_200_image_message_placeholder).load(url);

                // Show the imageView in a dialog on click.
                holder.imageView.setOnClickListener(new ImageMessageClickListener(activity, messageItem));
            }
        }

        holder.imageView.setLayoutParams(params);
        holder.imageView.requestLayout();


        // Load the user's profile image
        Ion.with(holder.profilePicImageView).placeholder(R.drawable.icn_32_profile_placeholder).load(messageItem.profilePicUrl);

        // Set the time of the sending.
        holder.timeTextView.setText(messageItem.time);
        animateSides(holder.timeTextView, messageItem.isMine(), null);

        row.setAlpha(messageItem.delivered() ? 1.0f : 0.5f);

    }

    public List<MessageListItem> getMessageItems() {
        return listData;
    }


    /**
     * Add a new message to the list.
     * @return true if the item is added to the list.
     * */
    public boolean addRow(MessageListItem newItem){
        // Bad bundle.
        if (newItem == null)
            return false;

        // Dont add message that does not have entity id and the status of the message is not sending.
        if (newItem.entityId == null && (newItem.delivered() || newItem.status() != BMessage.Status.SENDING))
        {
            if (DEBUG) Timber.d("CoreMessage has no entity and was sent.: ", newItem.text);
            return false;
        }

        Timber.d("Checking if exist");

        if(!listData.contains(newItem)) {
            listData.add(newItem);
        }

        Collections.sort(listData, new MessageItemSorter(MessageSorter.ORDER_TYPE_DESC));

        notifyDataSetChanged();

        return true;
    }

    /**
     * Add a new message to the list.
     * @return true if the item is added to the list.
     * */
    public boolean addRow(BMessage message){
        return addRow(new MessageListItem(message, maxWidth));
    }

    /**
     * Clear the messages list.
     * */
    public void clear(){
        listData.clear();
        notifyDataSetChanged();
    }

    /**
     * Converts a the messages list from BMessage to MessageListItem list.
     *
     * Here the custom date format is used to create the modified date format.
     *
     * */
    public List<MessageListItem> makeList(List<BMessage> list){
        return MessageListItem.makeList(activity, list);
    }

    /**
     * Set the messages list bundle.
     * */

    public void setMessages(List<BMessage> messages) {
        this.listData = makeList(messages);
        notifyDataSetChanged();
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

    /**
     * Animating the sides of the row, For example animating the user profile imageView and the message date.
     * */
    private void animateSides(View view, boolean fromLeft, Animation.AnimationListener animationListener){
        if (!isScrolling)
            return;

        if (fromLeft)
            view.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.expand_slide_form_left));
        else view.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.expand_slide_form_right));

        view.getAnimation().setAnimationListener(animationListener);
        view.animate();
    }

    /**
     *  Animating the center part of the row, For example the imageView in an imageView message or the text in text message.
     * */
    private void animateContent(View view, Animation.AnimationListener animationListener, boolean showFull){
        if (!isScrolling)
            return;

        view.setAnimation(AnimationUtils.loadAnimation(activity, showFull ? R.anim.fade_in_expand : R.anim.fade_in_half_and_expand));
        view.getAnimation().setAnimationListener(animationListener);
        view.animate();
    }

}
