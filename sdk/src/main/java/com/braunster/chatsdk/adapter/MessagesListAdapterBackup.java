/*
package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.UiUtils;
import com.braunster.chatsdk.Utils.volley.ChatSDKToast;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.entities.BMessageEntity;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.view.ChatBubbleImageView2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

*/
/**
 * Created by itzik on 6/5/2014.
 *//*

public class MessagesListAdapterBackup extends BaseAdapter{

    // FIXME  fix content overlap the hour.
    private static final String TAG = MessagesListAdapterBackup.class.getSimpleName();
    private static final boolean DEBUG = Debug.MessagesListAdapter;

    */
/* Row types *//*

    private static final int TYPE_TEXT_USER = 0;
    private static final int TYPE_TEXT_FRIEND = 1;
    private static final int TYPE_IMAGE_USER = 2;
    private static final int TYPE_IMAGE_FRIEND = 3;
    private static final int TYPE_LOCATION_USER = 4;
    private static final int TYPE_LOCATION_FRIEND = 5;

    int maxWidth;

    private Activity mActivity;

    private List<MessageListItem> listData = new ArrayList<MessageListItem>();

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    private long userID = 0;

    private LayoutInflater inflater;

    private int type = -1;

    public MessagesListAdapterBackup(Activity activity, Long userID){
        mActivity = activity;
        this.userID = userID;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        maxWidth = (int) (activity.getResources().getDisplayMetrics().density * 200);
    }

    public MessagesListAdapterBackup(Activity activity, Long userID, List<MessageListItem> listData){
        mActivity = activity;

        this.userID = userID;

        if (listData == null)
            listData = new ArrayList<MessageListItem>();

        this.listData = (List<MessageListItem>) listData;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        maxWidth = (int) (activity.getResources().getDisplayMetrics().density * 200);
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).type;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        CircleImageView profilePicImage;
        TextView txtTime;
        ChatBubbleImageView2 image;
        TextView txtContent;

        View row;
        row = view;

        type = getItemViewType(position);

        final MessageListItem message = listData.get(position);

        switch (type)
        {
            case TYPE_TEXT:

                if (message.sender == userID)
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_text_message_user, null);
                }
                else
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_text_message_friend, null);
                }

                txtContent = (TextView) row.findViewById(R.id.txt_content);

                txtContent.setText(message.text == null ? "ERROR" : message.text);

                txtContent.setMovementMethod(LinkMovementMethod.getInstance());

                // Show links in text view if has any.
                Linkify.addLinks(txtContent, Linkify.ALL);

             */
/*  int bubbleColor = message.getColor();
                txtContent.setBubbleColor(bubbleColor);*//*


                break;

            case TYPE_IMAGE:
                row = inflateImageRow(message);

                image = getBubbleImageViewFromRow(row, message);

                // Show the image in a dialog on click.
                image.setOnClickListener(new showImageDialogClickListener());
                break;

            case TYPE_LOCATION:
                row = inflateImageRow(message);

                image = getBubbleImageViewFromRow(row, message);

                // Open google maps on click.
                image.setOnClickListener(new openGoogleMaps());

                break;
        }

        // Load profile picture.
        profilePicImage = (CircleImageView) row.findViewById(R.id.img_user_image);
//        if (position == 0 || message.sender != listData.get(position-1).sender) {
            loadProfilePic(profilePicImage, message.profilePicUrl);
//        } else profilePicImage.setVisibility(View.INVISIBLE);

        // Add click event to image if message is picture or location.
        // Set the time of the sending.
        txtTime = (TextView) row.findViewById(R.id.txt_time);
        txtTime.setText(message.time);

        return row;
    }

    */
/** @return true if the item is added to the list.*//*

    public boolean addRow(MessageListItem data){
        if (DEBUG) Log.d(TAG, "AddRow");

        // Bad data.
        if (data == null)
            return false;

        // Dont add message that does not have entity id and the status of the message is not sending.
        if (data.entityId == null && data.status != BMessage.Status.SENDING)
        {
            if (DEBUG) Log.d(TAG, "Entity id is null, Message text: "  + data.text);
            return false;
        }

        // Check for duplicates, And update the message status if its already exist.
        for (MessageListItem item : listData)
        {
            if (item.entityId != null && item.entityId.equals(data.entityId) || item.id == data.id)
            {
                item.status = data.status;
                notifyDataSetChanged();
                return false;
            }
        }

        listData.add(data);

        notifyDataSetChanged();

        return true;
    }

    public boolean addRow(BMessage message){
        return addRow(MessageListItem.fromBMessage(message, userID, maxWidth));
    }

    public void setListData(List<MessageListItem> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    public List<MessageListItem> getListData() {
        return listData;
    }

    */
/** Load profile picture for given url and image view.*//*

    private void loadProfilePic(final CircleImageView circleImageView, String url){
        if (url == null)
        {
            circleImageView.setImageResource(R.drawable.ic_profile);
            return;
        }

        VolleyUtills.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null)
                    circleImageView.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }, circleImageView.getWidth(), circleImageView.getWidth());
    }

    private View inflateImageRow(MessageListItem message){
        View row;
        if (message.sender == userID) {
            row = inflater.inflate(R.layout.chat_sdk_row_image_message_user, null);
        }
        else
            row = inflater.inflate(R.layout.chat_sdk_row_image_message_friend, null);

        return row;
    }

    */
/** Get a ready image view for row position. The picture will be loaded to the bubble image view in the background using Volley. *//*

    private ChatBubbleImageView2 getBubbleImageViewFromRow(final View row, final MessageListItem message){

        final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progress_bar);
        final ChatBubbleImageView2 image = (ChatBubbleImageView2) row.findViewById(R.id.chat_sdk_image);

        // Save the message text to the image tag so it could be found on the onClick.
        image.setTag(message.text);

        // Coloring the message
        int bubbleColor = message.color;

        // Loading the url.
        final ChatBubbleImageView2.LoadDone loadDone = new ChatBubbleImageView2.LoadDone() {
            @Override
            public void onDone() {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void immediate(boolean immediate) {
                if (immediate){
                    if (progressBar.getVisibility() == View.VISIBLE) {
                        progressBar.setVisibility(View.INVISIBLE);
                        image.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    if (progressBar.getVisibility() == View.INVISIBLE) {
                        progressBar.setVisibility(View.VISIBLE);
                        image.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };

        if (message.dimensions != null)
        {
            // Getting the dimensions of the image so we can calc it final size and prepare room for it in the list view.
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image.getLayoutParams();
            params.width = (int) (message.dimensions[0] + image.getImagePadding() + image.getPointSize());
            params.height = message.dimensions[1] + image.getImagePadding();
            image.setLayoutParams(params);

            image.loadFromUrl(message.url, message.dimensions[0], message.dimensions[1], loadDone);
        }
        else
        {
            final int finalColor = bubbleColor;
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    */
/*FIXME due to old data in firebase this is needed.*//*

                    image.loadFromUrl(message.url, finalColor, progressBar.getMeasuredWidth(), loadDone);
                }
            });

        }

        return image;
    }

    */
/** Click listener for an image view, A dialog that show the image will show for each click.*//*

    public class showImageDialogClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            UiUtils.hideSoftKeyboard(mActivity);

            if (DEBUG) Log.v(TAG, "OnClick - Location");
            // Show the location image.
            if (v.getTag() != null)
            {
                        */
/*FIXME due to old data in firebase this is needed.*//*

                String url;
                String [] urls = ((String) v.getTag()).split(BDefines.DIVIDER);
                url = urls[0];

                DialogUtils.getImageDialog(mActivity, url, DialogUtils.LoadTypes.LOAD_FROM_URL).
                            showAtLocation(v, Gravity.CENTER, 0, 0);
            }
            else
            {
                ChatSDKToast.toastAlert(mActivity, "Cant show image.");
            }


        }
    }

    */
/** Click listener for the view location button for location messages. The click will open Google Maps for the location.*//*

    public class openGoogleMaps implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String[] loc = ((String)v.getTag()).split(BDefines.DIVIDER);

            */
/*FIXME due to old data*//*

            if (loc.length == 1)
                loc = ((String)v.getTag()).split("&");

            openLocationInGoogleMaps(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        }
        private void openLocationInGoogleMaps(Double latitude, Double longitude){
            try {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f (%s)", latitude, longitude, latitude, longitude, "Mark");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                ChatSDKToast.toastAlert(mActivity, "This phone does not have google maps.");
            }
        }
    }

    public List<MessageListItem> makeList(List<BMessage> list){
        return MessageListItem.makeList(mActivity, userID, list);
    }

    static class MessageListItem{
        private String entityId, profilePicUrl, time, text, textColor;
        private int type, status, color;
        private long sender;
        private long id;
        private int[] dimensions;
        private String url;

        private MessageListItem(long id, String entityId, int type, int status,
                                long senderID, String profilePicUrl, String time,
                                String text, String color, String textColor) {
            this.type = type;
            this.id = id;
            this.status = status;
            this.sender = senderID;
            this.entityId = entityId;
            this.profilePicUrl = profilePicUrl;
            this.time = time;
            this.text = text;
            this.color = setColor(color);
            this.textColor = textColor;

            if (type == BMessage.Type.IMAGE || type == BMessage.Type.LOCATION)
                url = getUrl();
        }


        public static MessageListItem fromBMessage(BMessage message, Long userID, int maxWidth){
            BUser user = message.getBUserSender();

            // Setting the row type.
            int type;
            switch (message.getType())
            {
                case BMessage.Type.TEXT:
                    if (user.getId().longValue() == userID.longValue())
                        type = TYPE_TEXT_USER;
                    else type = TYPE_TEXT_FRIEND;

                    break;

                case BMessage.Type.LOCATION:
                    if (user.getId().longValue() == userID.longValue())
                        type = TYPE_LOCATION_USER;
                    else type = TYPE_LOCATION_FRIEND;
                    break;

                case BMessage.Type.IMAGE:
                    if (user.getId().longValue() == userID.longValue())
                        type = TYPE_IMAGE_USER;
                    else type = TYPE_IMAGE_FRIEND;
                    break;

                default:
                    type = -1;
            }

            MessageListItem msg = new MessageListItem( message.getId(),
                    message.getEntityID(),
                    type,
                    message.getStatusOrNull(),
                    user.getId(),
                    user.getThumbnailPictureURL(),
                    String.valueOf(getFormat(message).format(message.getDate())),
                    message.getText(),
                    user.getMessageColor(),
                    user.getTextColor());

            msg.setDimension(maxWidth);

            return msg;
        }

        public static List<MessageListItem> makeList(Activity activity, Long userID, List<BMessage> messages){
            List<MessageListItem> list = new ArrayList<MessageListItem>();

            int maxWidth = (int) (activity.getResources().getDisplayMetrics().density * 200);

            for (BMessage message : messages)
                if (message.getEntityID() != null)
                    list.add(fromBMessage(message, userID, maxWidth));

            // We need to reverse the list so the newest data would be on the top again.
            Collections.reverse(list);

            return list;
        }

        public static SimpleDateFormat getFormat(BMessage message){
            Date curTime = new Date();
            long interval = (curTime.getTime() - message.getDate().getTime()) / 1000L;

            // More then a day ago
            if (interval > 3600 * 24)
            {
                // More then a year
                if (interval > 3600 * 24 * 365)
                {
                    simpleDateFormat.applyPattern("MM/yy");
                    return simpleDateFormat;
                }
                else {
                    simpleDateFormat.applyPattern("MMM dd");
                    return simpleDateFormat;
                }
            }
            else
            {
                simpleDateFormat.applyPattern("HH:mm");
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

        private int setColor(String color){
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
                    if (DEBUG) Log.d(TAG, "Color: " + bubbleColor);
                }
            }
            else bubbleColor = BMessage.randomColor();

            return bubbleColor;
        }

        public String getUrl(){
            */
/*FIXME because of old data we need to do some testing adn extra checking*//*

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

        public void setDimension(int maxWidth){
            try {
                String[] data = text.split(BDefines.DIVIDER);
                dimensions = ImageUtils.getDimentionsFromString(data[data.length - 1]);
                dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);

                if (DEBUG) Log.d(TAG, "dim: " + dimensions[0] + ", " + dimensions[1]);
            }catch (Exception e){}
        }

        public int getColor(){
            if (status == BMessageEntity.Status.SENDING)
               return Color.parseColor(BDefines.Defaults.MessageSendingColor);

            return color;
        }
    }

    class ViewHolder{

    }
}


*/
/*    private ImageView getImageViewfromRow(View row, String base64Data){
        ImageView image = (ImageView) row.findViewById(R.id.chat_sdk_image);
        image.setTag(base64Data);
        image.setImageBitmap(ImageUtils.decodeFrom64(base64Data.getBytes()));
        image.setOnClickListener(new showImageDialogClickListener());

        return image;
    }*//*

*/
/*    @SuppressLint("NewApi")
    private TextView getTextBubble(TextView txtContent, MessageListItem message){
        // Setting the text color to the user text color.
        if (message.textColor != null)
            txtContent.setTextColor(Color.parseColor(message.textColor));

        // setting the bubble color to the user message color.
        Bitmap bubble = ChatBubbleImageView.get_ninepatch(R.drawable.bubble_left_2, txtContent.getWidth(), txtContent.getHeight(), mActivity);
        if (bubble == null)
            Log.e(TAG, "bubble is null");
                        *//*
*/
/*FIXME*//*
*/
/*
        if (message.color != null && !message.color.equals("Red"))
            bubble = ChatBubbleImageView.setBubbleColor( bubble, Color.parseColor(message.color) );
        else bubble = ChatBubbleImageView.setBubbleColor( bubble,BMessage.randomColor() );

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            ((FrameLayout) txtContent.getParent()).setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bubble));
        } else {
            ((FrameLayout) txtContent.getParent()).setBackground(new BitmapDrawable(mActivity.getResources(), bubble));
        }

        txtContent.setVisibility(View.VISIBLE);

        return txtContent;
    }

    private TextView getTextBubble(TextView txtContent, MessageListItem message, int width, int height){
        // Setting the text color to the user text color.
        if (message.textColor != null)
            txtContent.setTextColor(Color.parseColor(message.textColor));

        // setting the bubble color to the user message color.
//        txtContent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bubble = ChatBubbleImageView.get_ninepatch(R.drawable.bubble_left_2, width, height, mActivity);
        if (bubble == null)
            Log.e(TAG, "bubble is null");
                        *//*
*/
/*FIXME*//*
*/
/*
        if (message.color != null && !message.color.equals("Red"))
            bubble = ChatBubbleImageView.setBubbleColor( bubble, Color.parseColor(message.color) );
        else bubble = ChatBubbleImageView.setBubbleColor( bubble,BMessage.randomColor() );

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            ((FrameLayout) txtContent.getParent()).setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bubble));
        } else {
            ((FrameLayout) txtContent.getParent()).setBackground(new BitmapDrawable(mActivity.getResources(), bubble));
        }

        txtContent.setVisibility(View.VISIBLE);

        return txtContent;
    }*//*


  */
/*  private String arrangeStringForUrl(String s){
        // separate input by spaces ( URLs don't have spaces )
        String [] parts = s.split("\\s+");
        String urlString = "";
        String newUrlString = "";

        // Attempt to convert each item into an URL.
        for( String item : parts )
            try {
                URL url = new URL(item);


                urlString = item;
                newUrlString = String.valueOf(Html.fromHtml("<a href=" + url + ">" + "The Link" + "</a> "));

                // If possible then replace with anchor...
                if (DEBUG) Log.d(TAG, "New url:  " + "<a href=" + url + ">"+ "Link" + "</a> " );

                break;

            } catch (MalformedURLException e) {
                // If there was an URL that was not it!...
                System.out.print( item + " " );
            }


        if (!urlString.equals(""))
            s = s.replace(urlString, newUrlString);

        return s;
    }*/
