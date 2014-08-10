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
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.volley.RoundedCornerNetworkImageView;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.network.BFacebookManager;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by itzik on 6/16/2014.
 */
public class FBFriendsListVolleyAdapter extends BaseAdapter {

    private static final String TAG = FBFriendsListVolleyAdapter.class.getSimpleName();
    private static final boolean DEBUG = Debug.FBFriendsListVolleyAdapter;

    private Activity mActivity;

    private List<GraphUser> listData = new ArrayList<GraphUser>();

    //View
    private View row;

    private class ViewHolder{
        TextView textView;
         RoundedCornerNetworkImageView circleNetworkImageView;
    }

    public FBFriendsListVolleyAdapter(Activity activity){
        mActivity = activity;
    }

    public FBFriendsListVolleyAdapter(Activity activity, List<GraphUser> listData){
        mActivity = activity;
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public GraphUser getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        row = view;

        ViewHolder holder;
        GraphUser user = listData.get(position);

        if (DEBUG) Log.d(TAG, "User details, Name: " + user.getName() + ", ID: " + user.getId());

        if ( row == null)
        {
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.chat_sdk_row_volley_contact, null);
            holder = new ViewHolder();
            holder.textView = (TextView) row.findViewById(R.id.chat_sdk_txt);
            holder.circleNetworkImageView = (RoundedCornerNetworkImageView) row.findViewById(R.id.img_profile_picture);
            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) row.getTag();
            holder.circleNetworkImageView.setImageBitmap(null);
        }

        holder.textView.setText(user.getName());

        // Load user profile pic.
        holder.circleNetworkImageView.setImageUrl(BFacebookManager.getPicUrl(user.getId()),
                            VolleyUtills.getImageLoader());

        return row;
    }

    public void addRow(GraphUser user){

        listData.add(user);

        notifyDataSetChanged();
    }

    public void setListData(List<GraphUser> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }
}