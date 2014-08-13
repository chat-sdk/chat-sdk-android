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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.ChatSDKToast;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.entities.BMessageEntity;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.view.ChatBubbleImageView;
import com.braunster.chatsdk.view.ChatBubbleTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/5/2014.
 */
public class MessagesListAdapter extends BaseAdapter{

    // FIXME  fix content overlap the hour.
    private static final String TAG = MessagesListAdapter.class.getSimpleName();
    private static final boolean DEBUG = Debug.MessagesListAdapter;

    /* Row types */
    private static final int TYPE_TEXT =0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_LOCATION = 2;

    private ChatBubbleTextView txtContent;

    private Activity mActivity;

    private List<MessageListItem> listData = new ArrayList<MessageListItem>();

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    private long userID = 0;

    private LayoutInflater inflater;

    private int type = -1;

    public MessagesListAdapter(Activity activity, Long userID){
        mActivity = activity;
        this.userID = userID;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }

    public MessagesListAdapter(Activity activity, List<MessageListItem> listData){
        mActivity = activity;

        if (listData == null)
            listData = new ArrayList<MessageListItem>();

        this.listData = (List<MessageListItem>) listData;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
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
//View
        View row;
        CircleImageView profilePicImage;
        TextView txtTime;
        row = view;
        ChatBubbleImageView image;

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

                txtContent = (ChatBubbleTextView) row.findViewById(R.id.txt_content);

                txtContent.setText( message.text == null ? "ERROR" : message.text);

                txtContent.setMovementMethod(LinkMovementMethod.getInstance());

                // Show links in text view if has any.
                Linkify.addLinks(txtContent, Linkify.ALL);

                // setting the bubble color to the user message color.
                final TextView textView  = txtContent;

                if (message.color != null && !message.color.equals("Red"))
                {
                    if (DEBUG) Log.d(TAG, "Color: " + message.color);
                    int bubbleColor = -1;

                    if (message.status == BMessageEntity.Status.SENDING){
                        bubbleColor = Color.parseColor("#C3C2C4");
                    }
                    else try{
                        bubbleColor = Color.parseColor(message.color);
                    }
                    catch (Exception e){}

                    if (bubbleColor == -1)
                    {
//                        if (DEBUG) Log.d(TAG, "Color As Float: " + Float.parseFloat(message.color));
//                        if (DEBUG) Log.d(TAG, "Color As Hex: " + Float.toHexString(Float.parseFloat(message.color)));
//                        bubbleColor = Color.HSVToColor(new Float[]{Float.parseFloat(message.color), 0.0f, 0.0f})
                    }

                    txtContent.setBubbleColor(bubbleColor);
                }
                else txtContent.setBubbleColor( BMessage.randomColor() );

                break;

            case TYPE_IMAGE:
                if (message.sender == userID)
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_message_user, null);
                }
                else
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_message_friend, null);
                }
         /*       *//*FIXME*//*
                if (message.text.length() > 200)
                    return row;*/

                image = getBubbleImageViewFromRow(row, message);
                image.setTag(message.text);
                image.setOnClickListener(new showImageDialogClickListener());
                break;

            case TYPE_LOCATION:
                if (message.sender == userID) {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_message_user, null);
                }
                else
                    row = inflater.inflate(R.layout.chat_sdk_row_image_message_friend, null);

                image = getBubbleImageViewFromRow(row, message);
//                // Save the message text to the image tag so it could be found on the onClick.
                image.setTag(message.text);
//                // Open google maps on click.
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

    /** @return true if the item is added to the list.*/
    public boolean addRow(MessageListItem data){
        if (DEBUG) Log.d(TAG, "AddRow");

        // Bad data.
        if (data == null)
            return false;

        // Dont add message that does not have entity id.
        if (data.entityId == null)
        {
            if (DEBUG) Log.d(TAG, "Entity id is null, Message text: "  + data.text);
            return false;
        }

        // Check for duplicates.
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
        return addRow(MessageListItem.fromBMessage(message));
    }

    public void setListData(List<MessageListItem> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    public List<MessageListItem> getListData() {
        return listData;
    }

    /** Load profile picture for given url and image view.*/
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

    private ImageView getImageViewfromRow(View row, String base64Data){
        ImageView image = (ImageView) row.findViewById(R.id.chat_sdk_image);
        image.setTag(base64Data);
        image.setImageBitmap(ImageUtils.decodeFrom64(base64Data.getBytes()));
        image.setOnClickListener(new showImageDialogClickListener());

        return image;
    }

    /** Get a ready image view for row position. The picture will be loaded to the bubble image view in the background using Volley. */
    private ChatBubbleImageView getBubbleImageViewFromRow(final View row, final MessageListItem message){

        final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progress_bar);
        final ChatBubbleImageView image = (ChatBubbleImageView) row.findViewById(R.id.chat_sdk_image);

        int[] dimensions = null;

        /*FIXME due to old data in firebase this is needed.*/
        String url = "";
        String [] urls = message.text.split(BDefines.DIVIDER);
        if (message.type == BMessageEntity.Type.IMAGE)
        {
            if (urls.length > 1)
            {
                url = urls[1];
            }
            else url = urls[0];
        }
        else if (message.type == BMessageEntity.Type.LOCATION)
        {
            if (urls.length == 1)
                urls = message.text.split("&");

            try {
                if (urls.length > 3)
                    url = urls[3];
                else url = urls[2];
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        final String finalUrl = url;
        if (DEBUG) Log.d(TAG, "Final URl: " + finalUrl);

        try {
            dimensions = ImageUtils.getDimentionsFromString(urls[urls.length - 1]);
            if (DEBUG) Log.d(TAG, "Img Dimensions, url: " + finalUrl + ", Width: " + dimensions[0] + ", Height: " + dimensions[1]);
            dimensions = ImageUtils.calcNewImageSize(dimensions, (int) image.MAX_WIDTH);
            if (DEBUG) Log.d(TAG, "Img Dimensions After Calc, " +  "url: " + finalUrl +",  Width: " + dimensions[0] + ", Height: " + dimensions[1]);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image.getLayoutParams();
            params.width = (int) (dimensions[0] + image.getImagePadding() + image.getPointSize());
            params.height = dimensions[1] + image.getImagePadding();
            image.setLayoutParams(params);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        final String color;
        if (message.status == BMessageEntity.Status.SENDING){
            color = "#C3C2C4";
        }
        else if (message.color != null && !message.color.equals("Red"))
            color = message.color;
        else color = Integer.toHexString(BMessage.randomColor());

        final ChatBubbleImageView.LoadDone loadDone = new ChatBubbleImageView.LoadDone() {
            @Override
            public void onDone() {
                progressBar.setVisibility(View.INVISIBLE);
                image.setVisibility(View.VISIBLE);
            }

            @Override
            public void immediate(boolean immediate) {
                if (immediate){
                    progressBar.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.VISIBLE);
                }
                else
                {
//                    progressBar.setVisibility(View.VISIBLE);
//                    image.setVisibility(View.INVISIBLE);
                }
            }
        };

        if (dimensions != null)
        {
            image.loadFromUrl(finalUrl, color, dimensions[0], loadDone);
        }
        else
        {
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    /*FIXME due to old data in firebase this is needed.*/

                    image.loadFromUrl(finalUrl, color, progressBar.getMeasuredWidth(), loadDone);
                }
            });

        }


        return image;
    }

    /** Click listener for an image view, A dialog that show the image will show for each click.*/
    public class showImageDialogClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (DEBUG) Log.v(TAG, "OnClick - Location");
            // Show the location image.
            if (v.getTag() != null)
            {
                        /*FIXME due to old data in firebase this is needed.*/
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

    /** Click listener for the view location button for location messages. The click will open Google Maps for the location.*/
    public class openGoogleMaps implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String[] loc = ((String)v.getTag()).split(BDefines.DIVIDER);

            /*FIXME due to old data*/
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
        return MessageListItem.makeList(list);
    }

    static class MessageListItem{
        private String entityId, profilePicUrl, time, text, color, textColor;
        private int type, status;
        private long sender;
        private long id;

        private MessageListItem(long id, String entityId, int type, int status, long senderID, String profilePicUrl, String time, String text, String color, String textColor) {
            this.type = type;
            this.id = id;
            this.status = status;
            this.sender = senderID;
            this.entityId = entityId;
            this.profilePicUrl = profilePicUrl;
            this.time = time;
            this.text = text;
            this.color = color;
            this.textColor = textColor;
        }

        public static MessageListItem fromBMessage(BMessage message){
            BUser user = message.getBUserSender();
            return new MessageListItem( message.getId(),
                                        message.getEntityID(),
                                        message.getType(),
                                        message.getStatusOrNull(),
                                        user.getId(),
                                        user.getMetaPictureUrl(),
                                        String.valueOf(getFormat(message).format(message.getDate())),
                                        message.getText(),
                                        user.getMessageColor(),
                                        user.getTextColor());
        }

        public static List<MessageListItem> makeList(List<BMessage> messages){
            List<MessageListItem> list = new ArrayList<MessageListItem>();

            for (BMessage message : messages)
                if (message.getEntityID() != null)
                    list.add(fromBMessage(message));

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
    }
}


/*    @SuppressLint("NewApi")
    private TextView getTextBubble(TextView txtContent, MessageListItem message){
        // Setting the text color to the user text color.
        if (message.textColor != null)
            txtContent.setTextColor(Color.parseColor(message.textColor));

        // setting the bubble color to the user message color.
        Bitmap bubble = ChatBubbleImageView.get_ninepatch(R.drawable.bubble_left_2, txtContent.getWidth(), txtContent.getHeight(), mActivity);
        if (bubble == null)
            Log.e(TAG, "bubble is null");
                        *//*FIXME*//*
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
                        *//*FIXME*//*
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
    }*/

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