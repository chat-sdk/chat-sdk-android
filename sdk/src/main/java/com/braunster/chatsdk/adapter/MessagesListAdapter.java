package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.dao.BMessage;

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

    /* Row types */
    private static final int TYPE_TEXT =0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_LOCATION = 2;

    private Activity mActivity;

    private List<BMessage> listData = new ArrayList<BMessage>();

    private int textColor = -1;

    //View
    private View row;

    private TextView txtContent, txtTime;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");

    private ImageView image;

    private Button btnViewLocation;

    private Date date;

    private String userID = "";

    private LayoutInflater inflater;

    private BMessage message;

    int type = -1;

    public MessagesListAdapter(Activity activity, String userID){
        mActivity = activity;
        this.userID = userID;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }

    public MessagesListAdapter(Activity activity, List<BMessage> listData){
        mActivity = activity;

        if (listData == null)
            listData = new ArrayList<BMessage>();

        this.listData = listData;
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

                if (message.getSender().equals(userID))
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
                if (message.getSender().equals(userID))
                    row = inflater.inflate(R.layout.chat_sdk_row_image_user, null);
                else
                    row = inflater.inflate(R.layout.chat_sdk_row_image_friend, null);

                image = (ImageView) row.findViewById(R.id.image);
                // TODO save image to cache
                image.setImageBitmap(Utils.decodeFrom64(message.getText().getBytes()));
                break;

            case TYPE_LOCATION:
                if (message.getSender().equals(userID))
                    row = inflater.inflate(R.layout.chat_sdk_row_location_user, null);
                else
                    row = inflater.inflate(R.layout.chat_sdk_row_location_user, null);

                btnViewLocation = (Button) row.findViewById(R.id.chat_sdk_btn_show_location);
                btnViewLocation.setTag(message.getText());
                btnViewLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] loc = ((String)v.getTag()).split("&");
                        openLocationInGoogleMaps(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
                    }
                });

                image = (ImageView) row.findViewById(R.id.chat_sdk_image);
                break;
        }

        // Set the time of the sending.
        txtTime = (TextView) row.findViewById(R.id.txt_time);
        date = listData.get(position).getDate();
        txtTime.setText(String.valueOf(simpleDateFormat.format(date)));

        return row;
    }

    public void addRow(BMessage data){
        listData.add(data);

        notifyDataSetChanged();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setListData(List<BMessage> listData) {
        this.listData = listData;
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
}
