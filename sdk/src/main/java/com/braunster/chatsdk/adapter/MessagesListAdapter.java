package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.view.ChatBubbleImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private List<BMessage> listData = new ArrayList<BMessage>();

    //View
    private View row;

    private TextView txtContent, txtTime;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");

    private ImageView image;
    private ChatBubbleImageView chatBubbleImageView;

    private Button btnViewLocation;

    private Date date;

    private long userID = 0;
    private String userEntityID = "";

    private LayoutInflater inflater;

    private BMessage message;

    int type = -1;

    public MessagesListAdapter(Activity activity, Long userID){
        mActivity = activity;
        this.userID = userID;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }

    public MessagesListAdapter(Activity activity, List<BMessage> listData){
        mActivity = activity;

        if (listData == null)
            listData = new ArrayList<BMessage>();

        this.listData = (List<BMessage>) listData;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }

    @Override
    public int getViewTypeCount() {
        return BMessage.Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).getType();
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

                if (message.getSender() == userID)
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_message_user, null);
                    row.setBackgroundColor(Color.CYAN);
                }
                else
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_message_friend, null);
                    row.setBackgroundColor(Color.WHITE);
                }

                txtContent = (TextView) row.findViewById(R.id.txt_content);
                txtContent.setText(message.getText() == null ? "ERROR" : listData.get(position).getText());
                break;

            case TYPE_IMAGE:
                if (message.getSender() == userID)
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_user, null);
                    image = getImageViewfromRow(row, message.getText());                }
                else
                {
                    row = inflater.inflate(R.layout.chat_sdk_row_image_friend, null);
                    image = getBubleImageViewfromRow(row, message.getText());
                }

                // TODO save image to cache or to app directory
                break;

            case TYPE_LOCATION:
                if (message.getSender() == userID) {
                    row = inflater.inflate(R.layout.chat_sdk_row_location_user, null);
                }
                else
                    row = inflater.inflate(R.layout.chat_sdk_row_location_friend, null);

                btnViewLocation = (Button) row.findViewById(R.id.chat_sdk_btn_show_location);

                // Save the message text to the button tag so it could be found on the onClick.
                btnViewLocation.setTag(message.getText());

                // Open google maps on click.
                btnViewLocation.setOnClickListener(new openGoogleMaps());

                // Show the location image. Base64 code image.
                String[] textArr = message.getText().split("&");
                if (textArr.length == 3)
                    image = getImageViewfromRow(row, textArr[2]);

                break;
        }

        // Add click event to image if message is picture or location.
        // Set the time of the sending.
        txtTime = (TextView) row.findViewById(R.id.txt_time);
        date = listData.get(position).getDate();
        txtTime.setText(String.valueOf(simpleDateFormat.format(date)));

        return row;
    }

    public void addRow(BMessage data){
        if (data == null)
            return;

        listData.add(data);

        notifyDataSetChanged();
    }

    public void setListData(List<BMessage> listData) {
        this.listData = (List<BMessage>)  listData;
        notifyDataSetChanged();
    }

    public List<BMessage> getListData() {
        return listData;
    }

    private void openLocationInGoogleMaps(Double latitude, Double longitude){
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mActivity.startActivity(intent);
    }

    private ImageView getImageViewfromRow(View row, String base64Data){
        ImageView image = (ImageView) row.findViewById(R.id.chat_sdk_image);
        image.setTag(base64Data);
        image.setImageBitmap(Utils.decodeFrom64(base64Data.getBytes()));
        image.setOnClickListener(new locationClickListener());

        return image;
    }

    private ChatBubbleImageView getBubleImageViewfromRow(View row, String base64Data){
        final ChatBubbleImageView image = (ChatBubbleImageView) row.findViewById(R.id.chat_sdk_image);
        image.setTag(base64Data);
     /*   VolleyUtills.getImageLoader().get(base64Data, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    image.setImageBitmap(response.getBitmap());
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Image Load Error: " + error.getMessage());
            }
        },image.getWidth(), image.getWidth());*/
        image.loadFromUrl(base64Data);
        image.setData(base64Data);
        image.setOnClickListener(new locationClickListener());

        return image;
    }

    public class locationClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (DEBUG) Log.v(TAG, "OnClick - Location");
            // Show the location image.
            if (v.getTag() != null)
            {
                DialogUtils.getImageDialog(mActivity, (String) v.getTag(), DialogUtils.LoadTypes.LOAD_FROM_BASE64).
//                  showAsDropDown(v);
                            showAtLocation(v, Gravity.CENTER, 0, 0);
            }
            else Toast.makeText(mActivity, "Cant show image.", Toast.LENGTH_SHORT).show();
        }
    }

    public class openGoogleMaps implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String[] loc = ((String)v.getTag()).split("&");
            openLocationInGoogleMaps(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        }
    }
    // TODO if picture is need to be collected from url or from file, Currently only using Base64. Dialog already support just need listener and logic.
}
