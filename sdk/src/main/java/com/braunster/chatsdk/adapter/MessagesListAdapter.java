package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BMessage;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by itzik on 6/5/2014.
 */
public class MessagesListAdapter extends BaseAdapter{

    // FIXME  fix content overlap the hour.
    private static final String TAG = MessagesListAdapter.class.getSimpleName();

    private Activity mActivity;

    private List<BMessage> listData = new ArrayList<BMessage>();

    private int textColor = -1;

    //View
    private View row;

    private TextView txtContent, txtTime;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");

    private Date date;

    public MessagesListAdapter(Activity activity){
        mActivity = activity;
    }

    public MessagesListAdapter(Activity activity, List<BMessage> listData){
        mActivity = activity;
        this.listData = listData;
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

/*
        if (listData.get(position).direction.equals(Message.IN))
        {
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.row_message_friend, null);
            row.setBackgroundColor(Color.CYAN);
        }
        else
        {
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.row_message_user, null);
            row.setBackgroundColor(Color.WHITE);
        }
*/

        txtContent = (TextView) row.findViewById(R.id.txt_content);
        txtTime = (TextView) row.findViewById(R.id.txt_time);

        if (textColor != -1)
        {
            txtContent.setTextColor(textColor);
            txtTime.setTextColor(textColor);
        }

//        date = new Date(listData.get(position).time);
//        txtContent.setText(listData.get(position).content == null ? "ERROR" : listData.get(position).content);
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
}
