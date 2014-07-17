package com.braunster.chatsdk.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.view.ChatBubbleImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final boolean DEBUG = true;

    /* Row types */
    private static final int TYPE_TEXT =0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_LOCATION = 2;

    private Activity mActivity;

    private List<MessageListItem> listData = new ArrayList<MessageListItem>();

    //View
    private View row;

    private TextView txtContent, txtTime;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");

    private CircleImageView profilePicImage;
    private ChatBubbleImageView image;

    private Button btnViewLocation;

    private Date date;

    private long userID = 0;
    private String userEntityID = "";

    private LayoutInflater inflater;

    private MessageListItem message;

    int type = -1;

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

        row = view;

        type = getItemViewType(position);

        message = listData.get(position);

        switch (type)
        {
            case TYPE_TEXT:

                if (message.sender == userID)
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_message_user, null);
                }
                else
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_message_friend, null);
                }

                txtContent = (TextView) row.findViewById(R.id.txt_content);

                txtContent.setText(message.text == null ? "ERROR" : listData.get(position).text);

                // Setting the text color to the user text color.
                if (message.textColor != null)
                    txtContent.setTextColor(Color.parseColor(message.textColor));

                // setting the bubble color to the user message color.
                txtContent.post(new Runnable() {
                    @Override
                    public void run() {
                        txtContent = getTextBubble(txtContent);
                    }
                });

//                final int widthSpec = View.MeasureSpec.makeMeasureSpec(((View) txtContent.getParent()).getWidth(), View.MeasureSpec.UNSPECIFIED);
//                final int heightSpec = View.MeasureSpec.makeMeasureSpec(((View) txtContent.getParent()).getHeight(), View.MeasureSpec.UNSPECIFIED);
//                txtContent.measure(widthSpec, heightSpec);
//                txtContent = getTextBubble(txtContent);

                break;

            case TYPE_IMAGE:
                if (message.sender == userID)
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_user, null);
                }
                else
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_friend, null);
                }
                /*FIXME*/
                if (message.text.length() > 200)
                    return row;

                image = getBubleImageViewfromRow(row, message.text);
                image.setTag(message.text);
                image.setOnClickListener(new showImageDialogClickListener());
                break;

            case TYPE_LOCATION:
                if (message.sender == userID) {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_user, null);
                }
                else
                    row = inflater.inflate(R.layout.chat_sdk_row_image_friend, null);

                image = getBubleImageViewfromRow(row, message.text.split("&")[2]);
//
//                // Save the message text to the image tag so it could be found on the onClick.
                image.setTag(message.text);
//
//                // Open google maps on click.
                image.setOnClickListener(new openGoogleMaps());

                break;
        }

        // Load profile picture.
        profilePicImage = (CircleImageView) row.findViewById(R.id.img_user_image);
        loadProfilePic(profilePicImage, message.profilePicUrl);

        // Add click event to image if message is picture or location.
        // Set the time of the sending.
        txtTime = (TextView) row.findViewById(R.id.txt_time);
        txtTime.setText(message.time);

        return row;
    }

    public void addRow(MessageListItem data){
        if (data == null)
            return;

        listData.add(data);

        notifyDataSetChanged();
    }

    public void addRow(BMessage message){
        addRow(MessageListItem.fromBMessage(message));
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
            circleImageView.setImageResource(R.drawable.icn_user_x_2);
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
        image.setImageBitmap(Utils.decodeFrom64(base64Data.getBytes()));
        image.setOnClickListener(new showImageDialogClickListener());

        return image;
    }

    @SuppressLint("NewApi")
    private TextView getTextBubble(TextView txtContent){
        // setting the bubble color to the user message color.
//        txtContent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bubble = ChatBubbleImageView.get_ninepatch(R.drawable.bubble_left_2, txtContent.getWidth(), txtContent.getHeight(), mActivity);
        if (bubble == null)
            Log.e(TAG, "bubble is null");
                        /*FIXME*/
        if (message.color != null && !message.color.equals("Red"))
            bubble = ChatBubbleImageView.setBubbleColor( bubble, Color.parseColor(message.color) );
        else bubble = ChatBubbleImageView.setBubbleColor( bubble,BMessage.randomColor() );

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            ((FrameLayout) txtContent.getParent()).setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bubble));
        } else {
            ((FrameLayout) txtContent.getParent()).setBackground(new BitmapDrawable(mActivity.getResources(), bubble));
        }

        return txtContent;
    }
    /** Get a ready image view for row position. The picture will be loaded to the bubble image view in the background using Volley. */
    private ChatBubbleImageView getBubleImageViewfromRow(final View row, String url){

        row.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        final ChatBubbleImageView image = (ChatBubbleImageView) row.findViewById(R.id.chat_sdk_image);
        image.setVisibility(View.GONE);

        /*FIXME due to old data in firebase this is needed.*/
        if (message.color != null && !message.color.equals("Red"))
            image.loadFromUrl(url, message.color, new ChatBubbleImageView.LoadDone() {
                @Override
                public void onDone() {
                    row.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                }
            });
        else image.loadFromUrl(url, BMessage.randomColor(), new ChatBubbleImageView.LoadDone() {
            @Override
            public void onDone() {
                row.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
            }
        });

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
                DialogUtils.getImageDialog(mActivity, (String) v.getTag(), DialogUtils.LoadTypes.LOAD_FROM_URL).
//                  showAsDropDown(v);
                            showAtLocation(v, Gravity.CENTER, 0, 0);
            }
            else Toast.makeText(mActivity, "Cant show image.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Click listener for the view location button for location messages. The click will open Google Maps for the location.*/
    public class openGoogleMaps implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String[] loc = ((String)v.getTag()).split("&");
            openLocationInGoogleMaps(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        }
        private void openLocationInGoogleMaps(Double latitude, Double longitude){
            try {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mActivity, "This phone does not have google maps.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public List<MessageListItem> makeList(List<BMessage> list){
        return MessageListItem.makeList(list);
    }

    static class MessageListItem{
        private String entityId, profilePicUrl, time, text, color, textColor;
        private int type;
        private long sender;

        MessageListItem(String entityId, int type, long senderID, String profilePicUrl, String time, String text, String color, String textColor) {
            this.type = type;
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
            return new MessageListItem( message.getEntityID(),
                                        message.getType(),
                                        user.getId(),
                                        user.getMetaPictureUrl(),
                                        String.valueOf(simpleDateFormat.format(message.getDate())),
                                        message.getText(),
                                        user.getMessageColor(),
                                        user.getTextColor());
        }

        public static List<MessageListItem> makeList(List<BMessage> messages){
            List<MessageListItem> list = new ArrayList<MessageListItem>();

            for (BMessage message : messages)
                list.add(fromBMessage(message));

            return list;
        }
    }
}
