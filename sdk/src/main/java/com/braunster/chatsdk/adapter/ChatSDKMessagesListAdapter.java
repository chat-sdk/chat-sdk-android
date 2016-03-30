/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.Utils.sorter.MessageItemSorter;
import com.braunster.chatsdk.Utils.sorter.MessageSorter;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BMessageEntity;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.view.ChatBubbleImageView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

/**
 * Created by itzik on 6/5/2014.
 */
public class ChatSDKMessagesListAdapter extends BaseAdapter{

    private static final String TAG = ChatSDKMessagesListAdapter.class.getSimpleName();
    private static final boolean DEBUG = Debug.MessagesListAdapter;

    /* Row types
     * 
     * User type should always be even number and friend and odd one 
     * * so it would be easy to check if current user is the sender.
     **/
    private static final int TYPE_TEXT_USER = 0;
    private static final int TYPE_TEXT_FRIEND = 1;
    private static final int TYPE_IMAGE_USER = 2;
    private static final int TYPE_IMAGE_FRIEND = 3;
    private static final int TYPE_LOCATION_USER = 4;
    private static final int TYPE_LOCATION_FRIEND = 5;

    private int textUserRowResId = R.layout.chat_sdk_row_text_message_user, textFriendRowResId = R.layout.chat_sdk_row_text_message_friend,
            imageUserRowResId = R.layout.chat_sdk_row_image_message_user, imageFriendRowResId = R.layout.chat_sdk_row_image_message_friend,
            locationUserResId = R.layout.chat_sdk_row_image_message_user, locationFriendRowResId = R.layout.chat_sdk_row_image_message_friend;

    int maxWidth;

    private boolean hideMultipleImages = false;

    private Activity mActivity;

    private List<MessageListItem> listData = new ArrayList<MessageListItem>();

    private SimpleDateFormat customDateFormat = null;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
    private List<String> cacheKeys = new ArrayList<String>();

    private long userID = 0;

    private boolean isScrolling = false;

    private LayoutInflater inflater;

    private int type = -1;

    private int textColor = -1991;

    private ChatSDKUiHelper chatSDKUiHelper;

    /**
     * if true each image cache key will be saved in a list.
     * @see #cacheKeys
     * * */
    private boolean saveCacheKeys = false;
    
    /**
     * Builder that will be use to create the chache keys.
     * * */
    private StringBuilder builder = new StringBuilder();

    public ChatSDKMessagesListAdapter(Activity activity, Long userID){
        mActivity = activity;
        this.userID = userID;

        init();
    }

    public ChatSDKMessagesListAdapter(Activity activity, Long userID, List<MessageListItem> listData){
        mActivity = activity;

        this.userID = userID;

        if (listData == null)
            listData = new ArrayList<MessageListItem>();

        this.listData = listData;

        init();

    }



    private void init(){
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        maxWidth =  (mActivity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width));

        chatSDKUiHelper = ChatSDKUiHelper.getInstance().get(mActivity);
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).rowType;
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
        return listData.get(i).id;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View row;
        row = view;
        ViewHolder holder;
        type = getItemViewType(position);
        final MessageListItem message = listData.get(position);
        final boolean sender = message.rowType % 2 == 0;

        if (row == null)
        {
            holder = new ViewHolder();

            row = inflateRow(holder);

            inflateDefaults(holder, row, sender, message);

            row.setTag(holder);
        }
        else
            holder = (ViewHolder) row.getTag();

//        if (DEBUG) Log.d(TAG, "Message, Type: " + message.type  + " Row Type: " + type + ", Text: " + message.text);

        // Load the message data.
        loadMessageData(holder, message);

        loadDefaults(row, holder, position, message, sender);

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
    protected View inflateRow(ViewHolder holder){
        View row = null;
        switch (type)
        {
            case TYPE_TEXT_USER:
                row = inflater.inflate(textUserRowResId, null);

                holder.txtContent = (TextView) row.findViewById(R.id.txt_content);

                break;

            case TYPE_TEXT_FRIEND:

                row = inflater.inflate(textFriendRowResId, null);

                holder.txtContent = (TextView) row.findViewById(R.id.txt_content);

                break;

            case TYPE_IMAGE_USER:
                row = inflater.inflate(imageUserRowResId, null);

                holder.progressBar = (ProgressBar) row.findViewById(R.id.chat_sdk_progress_bar);
                holder.image = (ChatBubbleImageView) row.findViewById(R.id.chat_sdk_image);

                break;

            case TYPE_IMAGE_FRIEND:
                row = inflater.inflate(imageFriendRowResId, null);

                holder.progressBar = (ProgressBar) row.findViewById(R.id.chat_sdk_progress_bar);
                holder.image = (ChatBubbleImageView) row.findViewById(R.id.chat_sdk_image);
                break;

            case TYPE_LOCATION_USER:
                row = inflater.inflate(locationUserResId, null);

                holder.progressBar = (ProgressBar) row.findViewById(R.id.chat_sdk_progress_bar);
                holder.image = (ChatBubbleImageView) row.findViewById(R.id.chat_sdk_image);

                break;

            case TYPE_LOCATION_FRIEND:
                row = inflater.inflate(locationFriendRowResId, null);

                holder.progressBar = (ProgressBar) row.findViewById(R.id.chat_sdk_progress_bar);
                holder.image = (ChatBubbleImageView) row.findViewById(R.id.chat_sdk_image);

                break;
        }

        return row;
    }

    /**
     * Load the default components that will be in each message no matter what.
     *
     * By Overriding this you can stop default loading of the profile image and time.
     * You can also add your own defaults here.
     *
     * */
    protected void inflateDefaults(ViewHolder holder, View row, boolean sender, MessageListItem message){
        // Load profile picture.
        holder.profilePicImage = (CircleImageView) row.findViewById(R.id.img_user_image);
        holder.txtTime = (TextView) row.findViewById(R.id.txt_time);
   
    }

    /**
     * Load the data for each message, The data will be loaded for each message type and would be animated if needed.
     *
     * By Overriding this function you can load data for your custom messages type, For example video message or audio.
     * You can also just change one of the default loading type.
     *
     * */
    protected void loadMessageData(ViewHolder holder, MessageListItem message){
        switch (type)
        {
            case TYPE_TEXT_USER:
            case TYPE_TEXT_FRIEND:

                holder.txtContent.setText(message.text == null ? "ERROR" : message.text);

                // Show links in text view if has any.
                holder.txtContent.setMovementMethod(LinkMovementMethod.getInstance());
                Linkify.addLinks(holder.txtContent, Linkify.ALL);

                if (textColor != -1991)
                    holder.txtContent.setTextColor(textColor);

                animateContent((View) holder.txtContent.getParent(), null, message.delivered != BMessage.Delivered.No);

                break;

            case TYPE_IMAGE_USER:
            case TYPE_IMAGE_FRIEND:

                getBubbleImageViewFromRow(holder.image, holder.progressBar, message);

                // Show the image in a dialog on click.
                holder.image.setOnClickListener(new showImageDialogClickListener(message));

                break;

            case TYPE_LOCATION_USER:
            case TYPE_LOCATION_FRIEND:

                getBubbleImageViewFromRow(holder.image, holder.progressBar,  message);

                // Open google maps on click.
                holder.image.setOnClickListener(new openGoogleMaps());

                break;
        }
    }

    /**
     * Load the default data for each message, The data will be loaded for each message and be animated if needed.
     *
     * By Overriding this function you change or add logic for your default message data load,
     * For example load online status for each user.
     *
     * */
    protected void loadDefaults(View row, ViewHolder holder, int position, MessageListItem message, boolean sender){
        // Load profile picture.
        // If we want to hide multiple images we check to see that it is not the first image,
        // And that it is from the same sender as the previous message sender.
        if (!hideMultipleImages || position == 0 || message.sender != listData.get(position-1).sender) {
            loadProfilePic(holder.profilePicImage, message.profilePicUrl, sender);
        } else holder.profilePicImage.setVisibility(View.INVISIBLE);

        // Set the time of the sending.
        holder.txtTime.setText(message.time);
        animateSides(holder.txtTime, sender, null);


        switch (message.delivered)
        {
            case BMessage.Delivered.Yes:
                row.setAlpha(1.0f);
                break;

            case BMessage.Delivered.No:
                row.setAlpha(0.5f);
                break;
        }
    }

    /** Load profile picture for given url and image view.*/
    private void loadProfilePic(final CircleImageView circleImageView, final String url, final boolean sender){

        if (url == null)
        {
            circleImageView.setImageResource(R.drawable.ic_profile);
            return;
        }

        circleImageView.setTag(url);

        VolleyUtils.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {

                // Checking to see that there is no new rewuest on this image.
                if (circleImageView.getTag() != null && !circleImageView.getTag().equals(url))
                    return;

                if (isImmediate && response.getBitmap() == null)
                {
                    circleImageView.setImageResource(R.drawable.ic_profile);
                    return;
                }

                if (response.getBitmap() != null)
                {
                    if (!isScrolling)
                    {
                        circleImageView.setImageBitmap(response.getBitmap());
                    }
                    else
                    {
                        animateSides(circleImageView, !sender, new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                circleImageView.setImageBitmap(response.getBitmap());
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        circleImageView.getAnimation().start();
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                circleImageView.setImageResource(R.drawable.ic_profile);
            }
        }, circleImageView.getWidth(), circleImageView.getWidth());
    }

    /** Get a ready image view for row position. The picture will be loaded to the bubble image view in the background using Volley. */
    private ChatBubbleImageView getBubbleImageViewFromRow(final ChatBubbleImageView image, final ProgressBar progressBar, final MessageListItem message){
        if (DEBUG) Timber.v("getBubbleImageViewFromRow");
        
        // Save the message text to the image tag so it could be found on the onClick.
        image.setTag(message.text);

        // Loading the url.
        final ChatBubbleImageView.LoadDone loadDone = new ChatBubbleImageView.LoadDone() {

            @Override
            public void onDone() {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void immediate(boolean immediate) {
                if (immediate){
                    if (progressBar.getVisibility() == View.VISIBLE) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    animateContent(image, null, message.delivered != BMessage.Delivered.No);
                }
                else
                {
                    image.clearCanvas();
                    if (progressBar.getVisibility() == View.INVISIBLE) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        if (message.dimensions != null)
        {
            // Getting the dimensions of the image so we can calc it final size and prepare room for it in the list view.
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image.getLayoutParams();
            params.width = (int) (message.dimensions[0] + image.getImagePadding() + image.getTipSize());
            params.height = message.dimensions[1] + image.getImagePadding();
            image.setLayoutParams(params);

            if (StringUtils.isNotEmpty(message.resourcePath) && new File(message.resourcePath).exists())
            {
                // Saving the url so we could remove it later on.
                if (saveCacheKeys)
                    saveCacheKey(message.resourcePath + ChatBubbleImageView.URL_FIX);
                
                image.loadFromPath(message.resourcePath, loadDone, message.dimensions[0], message.dimensions[1]);
            }
            else
            {
                // Saving the url so we could remove it later on.
                if (saveCacheKeys)
                    saveCacheKey(message.url + ChatBubbleImageView.URL_FIX);

                image.loadFromUrl(message.url, loadDone, message.dimensions[0], message.dimensions[1]);
            }
        }
        else if (DEBUG) Timber.d("ImageMessage dimensions is null");

        return image;
    }

    private void saveCacheKey(String key){
        // Saving the url so we could remove it later on.
        
        if (!saveCacheKeys)
            return;
        
        String cacheKey = VolleyUtils.BitmapCache.getCacheKey(builder, key, 0, 0);
        if (!cacheKeys.contains(cacheKey))
            cacheKeys.add(cacheKey);
    }

    /**
     * Add a new message to the list.
     * @return true if the item is added to the list.
     * */
    public boolean addRow(MessageListItem newItem){
        // Bad data.
        if (newItem == null)
            return false;
        
        if (DEBUG) Timber.v("addRow, ID: %s, Delivered: %s", newItem.id, newItem.delivered);

        // Dont add message that does not have entity id and the status of the message is not sending.
        if (newItem.entityId == null && (newItem.delivered != BMessage.Delivered.No
                || newItem.status != BMessage.Status.SENDING))
        {
            if (DEBUG) Timber.d("Message has no entity and was sent.: ", newItem.text);
            return false;
        }


        Timber.d("Checking if exist");
        // Check for duplicates, And update the message status if its already exist.
        for (MessageListItem item : listData)
        {
            Timber.d("OldId: %s, NewId: %s", item.id, newItem.id);
            
            if (item.id == newItem.id)
            {
                item.entityId = newItem.entityId;
                item.status = newItem.status;
                item.delivered = newItem.delivered;
                item.url = newItem.url;
                item.time = newItem.time;
                item.dimensions = newItem.getDimensions(maxWidth);
                
                Timber.d("Updating old item");
                
                notifyDataSetChanged();
                
                return false;
            }
        }

        listData.add(newItem);

        Collections.sort(listData, new MessageItemSorter(MessageSorter.ORDER_TYPE_DESC));

        notifyDataSetChanged();

        return true;
    }

    /**
     * Add a new message to the list.
     * @return true if the item is added to the list.
     * */
    public boolean addRow(BMessage message){
        return addRow(MessageListItem.fromBMessage(message, userID, maxWidth, customDateFormat));
    }

    /**
     * Clear the messages list.
     * */
    public void clear(){
        listData.clear();
        notifyDataSetChanged();
    }



    /**
     *  Click listener for an image view, A dialog that show the image will show for each click.
     *  */
    public class showImageDialogClickListener implements View.OnClickListener{

        private MessageListItem message;

        public showImageDialogClickListener(MessageListItem message) {
            this.message = message;
        }

        @Override
        public void onClick(View v) {
            ChatSDKUiHelper.hideSoftKeyboard(mActivity);

            if (StringUtils.isNotBlank(message.resourcePath))
            {
                PopupWindow popupWindow;
            
                popupWindow = DialogUtils.getImageDialog(mActivity, message.resourcePath, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_PATH);

                if (popupWindow == null)
                    chatSDKUiHelper.showAlertToast(mActivity.getString(R.string.message_adapter_load_image_fail));
                else popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
            else if (StringUtils.isNotBlank(message.text))
            {
                String imageUrl = message.text.split(BDefines.DIVIDER)[0];
                // Saving the url so we could remove it later on.
                saveCacheKey(imageUrl);

                PopupWindow popupWindow;

                // Telling the popup window to save the image after it was open.
                if (!BDefines.Options.SaveImagesToDir)
                    popupWindow = DialogUtils.getImageDialog(mActivity, imageUrl, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_URL);
                else
                    popupWindow = DialogUtils.getImageMessageDialog(mActivity, imageUrl, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_URL, message);

                if (popupWindow == null)
                    chatSDKUiHelper.showAlertToast(mActivity.getString(R.string.message_adapter_load_image_fail));
                else popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
            else
            {
                chatSDKUiHelper.showAlertToast(mActivity.getString(R.string.message_adapter_load_image_fail));
            }
        }
    }

    /**
     * Click listener for the view location button for location messages. The click will open Google Maps for the location.
     * */
    public class openGoogleMaps implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getTag() == null)
                return;

            String[] loc = ((String)v.getTag()).split(BDefines.DIVIDER);

            openLocationInGoogleMaps(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        }
        private void openLocationInGoogleMaps(Double latitude, Double longitude){
            try {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f (%s)", latitude, longitude, latitude, longitude, "Mark");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                chatSDKUiHelper.showAlertToast(mActivity.getString(R.string.message_adapter_no_google_maps));
            }
        }
    }

    /**
     * Converts a the messages list from BMessage to MessageListItem list.
     *
     * Here the custom date format is used to create the modified date format.
     *
     * @see #setCustomDateFormat(java.text.SimpleDateFormat)
     * */
    public List<MessageListItem> makeList(List<BMessage> list){
        return MessageListItem.makeList(mActivity, userID, list, customDateFormat);
    }




    /**
     * The MessageListItem holds the BMessage object so we wont need to query data about the message each time we inflate a row.
     * */
    public static class MessageListItem{
        public String entityId, profilePicUrl, time, text, resourcePath;
        private int type, status, color, rowType;
        private long sender, timeInMillis;
        private long id;
        private int[] dimensions = null;
        private String url;
        private int delivered = BMessage.Delivered.No;
        private String dimensionsString;
        
        private MessageListItem(long id, String entityId, int type, int rowType, int status,
                                long senderID, String profilePicUrl, String time,
                                String text, String color, long timeInMillis, int delivered, String resourcePath, String dimensionsString) {
            this.type = type;
            this.id = id;
            this.timeInMillis = timeInMillis;
            this.status = status;
            this.sender = senderID;
            this.entityId = entityId;
            this.profilePicUrl = profilePicUrl;
            this.time = time;
            this.text = text;
            this.color = setColor(color);
            this.rowType = rowType;
            this.delivered = delivered;
            this.resourcePath = resourcePath;
            this.dimensionsString = dimensionsString;
            
            if (type == BMessage.Type.IMAGE || type == BMessage.Type.LOCATION)
                url = getUrl(text, type);
        }


        public static MessageListItem fromBMessage(BMessage message, Long userID, int maxWidth, SimpleDateFormat simpleDateFormat){

            // If null that means no custom format was added to the adapter so we use the default.
            if (simpleDateFormat == null)
                simpleDateFormat = getFormat(message);

            BUser user = message.getBUserSender();

            MessageListItem msg = new MessageListItem( message.getId(),
                    message.getEntityID(),
                    message.getType(),
                    getRowType(message.getType(), user.getId(), userID),
                    message.getStatusOrNull(),
                    user.getId(),
                    user.getThumbnailPictureURL(),
                    String.valueOf(simpleDateFormat.format(message.getDate())),
                    message.getText(),
                    user.getMessageColor(),
                    message.getDate().getTime(), 
                    message.wasDelivered(),
                    message.getResourcesPath(), 
                    message.getImageDimensions());

            msg.setDimension(maxWidth);

            return msg;
        }

        public static List<MessageListItem> makeList(Activity activity, Long userID, List<BMessage> messages, SimpleDateFormat simpleDateFormat){
            if (DEBUG) Timber.v("makeList, messagesSize: %s, ", messages.size());
            
            List<MessageListItem> list = new ArrayList<MessageListItem>();

            int maxWidth =  (activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width));

            MessageListItem i;
            for (BMessage message : messages)
            {
                i = fromBMessage(message, userID, maxWidth, simpleDateFormat);

                    /*Fixme Due to old data*/
                if (i.type != BMessage.Type.TEXT && i.dimensions == null)
                {
                    Timber.d("Cant find dimensions, path: %s, dimensionsString: %s", i.resourcePath, i.dimensionsString);
                    continue;
                }

                    /*Skip messages with no date.*/
                if (message.getDate() == null)
                    continue;

                list.add(i);
            }

            // We need to reverse the list so the newest data would be on the top again.
            Collections.reverse(list);

            return list;
        }

        private static SimpleDateFormat getFormat(BMessage message){

            Date curTime = new Date();
            long interval = (curTime.getTime() - message.getDate().getTime()) / 1000L;

            // More then a day ago
            if (interval > 3600 * 24)
            {
                // More then a year
                if (interval > 3600 * 24 * 365)
                {
                    simpleDateFormat.applyPattern(BDefines.MessageDateFormat.YearOldMessageFormat);
                    return simpleDateFormat;
                }
                else {
                    simpleDateFormat.applyPattern(BDefines.MessageDateFormat.DayOldFormat);
                    return simpleDateFormat;
                }
            }
            else
            {
                simpleDateFormat.applyPattern(BDefines.MessageDateFormat.LessThenDayFormat);
                return simpleDateFormat;
            }
        }

        private static int getColorFromDec(String color){
            String[] split = color.split(" ");

            if (split.length != 4)
                return BMessage.randomColor();

            int bubbleColor = -1;

            bubbleColor = Color.argb(Integer.parseInt(split[3]), (int) (255 * Float.parseFloat(split[0])), (int) (255 * Float.parseFloat(split[1])), (int) (255 * Float.parseFloat(split[2])));

            return bubbleColor;
        }

        private static int setColor(String color){
            // Coloring the message
            int bubbleColor = -1;
            if (color != null && !color.equals("Red"))
            {
                try{
                    bubbleColor = Color.parseColor(color);
                }
                catch (Exception e){}

                if (bubbleColor == -1)
                {
                    bubbleColor = getColorFromDec(color);
                }
            }
            else bubbleColor = BMessage.randomColor();

            return bubbleColor;
        }

        private static int getRowType(int messageType, long senderId, long curUserID){
            // Setting the row type.
            int type;
            switch (messageType)
            {
                case BMessage.Type.TEXT:
                    if (senderId == curUserID)
                        type = TYPE_TEXT_USER;
                    else type = TYPE_TEXT_FRIEND;
                    break;

                case BMessage.Type.LOCATION:
                    if (senderId == curUserID)
                        type = TYPE_LOCATION_USER;
                    else type = TYPE_LOCATION_FRIEND;
                    break;

                case BMessage.Type.IMAGE:
                    if (senderId == curUserID)
                        type = TYPE_IMAGE_USER;
                    else type = TYPE_IMAGE_FRIEND;
                    break;

                default:
                    type = -1;
            }

            return type;
        }

        private static String getUrl(String text, int type){
            
            if (StringUtils.isBlank(text))
                return "";
            
            String url = "";
            String [] urls = text.split(BDefines.DIVIDER);
            if (type == BMessageEntity.Type.IMAGE)
            {
                if (urls.length > 1)
                {
                    url = urls[1];
                }
                else url = urls[0];
            }
            else if (type == BMessageEntity.Type.LOCATION)
            {
                if (urls.length == 1)
                    urls = text.split("&");

                try {
                    if (urls.length > 3)
                        url = urls[3];
                    else url = urls[2];
                } catch (Exception e) {
//                e.printStackTrace();
                }
            }

            return url;
        }

        private void setDimension(int maxWidth){
            dimensions = getDimensions(maxWidth);
        }

        private int[] getDimensions(int maxWidth){

            if (StringUtils.isNotEmpty(text))
            {
//                if (DEBUG) Timber.d("Getting dimensions from url");

                try {
                    String[] data = text.split(BDefines.DIVIDER);
                    dimensions = ImageUtils.getDimensionsFromString(data[data.length - 1]);
                    dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);

                    if (dimensions.length != 2)
                        dimensions = null;

                }catch (Exception e){  dimensions = null;}

            }
            else if (StringUtils.isNotEmpty(dimensionsString))
            {
//                if (DEBUG) Timber.d("Getting dimensions from dimensionsString");
                dimensions = ImageUtils.getDimensionsFromString(dimensionsString);
                dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);

//                if (DEBUG) Timber.d("dimensions[0]: %s, dimensions[1]: %s", dimensions[0], dimensions[1]);
            }
            else if (VolleyUtils.getBitmapCache().contains(VolleyUtils.BitmapCache.getCacheKey(resourcePath)))
            {
                dimensionsString = ImageUtils.getDimensionAsString(VolleyUtils.getBitmapCache().getBitmap(VolleyUtils.BitmapCache.getCacheKey(resourcePath)));
                dimensions = ImageUtils.getDimensionsFromString(dimensionsString);
                dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);
            }

            return dimensions;
        }

        public long getTimeInMillis() {
            return timeInMillis;
        }

        public BMessage asBMessage(){
            return DaoCore.fetchEntityWithEntityID(BMessage.class, entityId);
        }
    }

    /**
     * Class to hold the row child views so we wont have to inflate more them once per row view.
     * */
    class ViewHolder{
        CircleImageView profilePicImage;
        TextView txtTime;
        ChatBubbleImageView image;
        TextView txtContent;
        ProgressBar progressBar;
    }





    /**
     * Set the messages list data.
     * */
    public void setListData(List<MessageListItem> listData) {
        if (DEBUG) Timber.v("setListData, Size: %s", listData == null ? "null" : listData.size());
        this.listData = listData;
        notifyDataSetChanged();
    }

    /**
     * Get the messages list data.
     * */
    public List<MessageListItem> getListData() {
        return listData;
    }

    /**
     * If true the user profile image will be hidden if the message above is also from the same user.
     * */
    public void hideMultipleImages(boolean hide){
        this.hideMultipleImages = hide;
    }

    /**
     * Get the cache keys that where used when loading images for this current messages list.
     *
     * This could be used for cleaning the cache after user exit a chat room,
     * The SDK does not use this feature but it can be found (commented out) in the {@link com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractChatActivity ChatSDKAbstractChatActivity} onDestroy() function.
     * */
    public List<String> getCacheKeys() {
        return cacheKeys;
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
     * Animating the sides of the row, For example animating the user profile image and the message date.
     * */
    private void animateSides(View view, boolean fromLeft, Animation.AnimationListener animationListener){
        if (!isScrolling)
            return;

        if (fromLeft)
            view.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.expand_slide_form_left));
        else view.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.expand_slide_form_right));

        view.getAnimation().setAnimationListener(animationListener);
        view.animate();
    }

    /**
     *  Animating the center part of the row, For example the image in an image message or the text in text message.
     * */
    private void animateContent(View view, Animation.AnimationListener animationListener, boolean showFull){
        if (!isScrolling)
            return;

        view.setAnimation(AnimationUtils.loadAnimation(mActivity, showFull ? R.anim.fade_in_expand : R.anim.fade_in_half_and_expand));
        view.getAnimation().setAnimationListener(animationListener);
        view.animate();
    }

    /**
     * Set the message text color.
     *
     * The default is the color defined in the layout file that will be inflated.
     *
     * */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    /**
     * Set the layout id that will be inflated for each user text message row.
     * */
    public void setTextUserRowResId(int textUserRowResId) {
        this.textUserRowResId = textUserRowResId;
    }

    /**
     * Set the layout id that will be inflated for each friend text message row.
     * */
    public void setTextFriendRowResId(int textFriendRowResId) {
        this.textFriendRowResId = textFriendRowResId;
    }

    /**
     * Set the layout id that will be inflated for each user image message row.
     * */
    public void setImageUserRowResId(int imageUserRowResId) {
        this.imageUserRowResId = imageUserRowResId;
    }

    /**
     * Set the layout id that will be inflated for each friend image message row.
     * */
    public void setImageFriendRowResId(int imageFriendRowResId) {
        this.imageFriendRowResId = imageFriendRowResId;
    }

    /**
     * Set the layout id that will be inflated for each user location message row.
     * */
    public void setLocationUserResId(int locationUserResId) {
        this.locationUserResId = locationUserResId;
    }

    /**
     * Set the layout id that will be inflated for each friends location message row.
     * */
    public void setLocationFriendRowResId(int locationFriendRowResId) {
        this.locationFriendRowResId = locationFriendRowResId;
    }

    /**
     * Set the date format that will be used to format the message date.
     * */
    public void setCustomDateFormat(SimpleDateFormat customDateFormat) {
        this.customDateFormat = customDateFormat;
    }

    public void setSaveCacheKeys(boolean saveCacheKeys) {
        this.saveCacheKeys = saveCacheKeys;
    }
}
