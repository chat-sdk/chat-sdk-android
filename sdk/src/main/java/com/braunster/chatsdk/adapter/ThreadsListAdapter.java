package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by itzik on 6/16/2014.
 */
public class ThreadsListAdapter extends BaseAdapter {

    private static final String TAG = ThreadsListAdapter.class.getSimpleName();

    private Activity mActivity;

    private List<BThread> listData = new ArrayList<BThread>();

    private int textColor = -1;

    //View
    private View row;

    private TextView textView;

    public ThreadsListAdapter(Activity activity){
        mActivity = activity;
    }

    public ThreadsListAdapter(Activity activity, List<BThread> listData){
        mActivity = activity;
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public BThread getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        row = view;


        if ( row == null)
        {
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.row_threads, null);

        }

        textView = (TextView) row.findViewById(R.id.txt_name);

        if (textColor != -1)
            textView.setTextColor(textColor);

        textView.setText(listData.get(position).getName());

        return row;
    }

    public void addRow(BThread thread){

        listData.add(thread);

        notifyDataSetChanged();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setListData(List<BThread> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }
}