package com.braunster.chatsdk.archive;/*
package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BFacebookManager;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

*/
/**
 * Created by itzik on 6/16/2014.
 *//*

public class FBFriendsListAdapter extends BaseAdapter {

    private static final String TAG = FBFriendsListAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;

    private Activity mActivity;

    private List<GraphUser> listData = new ArrayList<GraphUser>();

    //View
    private View row;

    private class ViewHolder{
        TextView textView;
        CircleImageView circleImageView;
    }

    public FBFriendsListAdapter(Activity activity){
        mActivity = activity;
    }

    public FBFriendsListAdapter(Activity activity, List<GraphUser> listData){
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
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.chat_sdk_row_contact, null);
            holder = new ViewHolder();
            holder.textView = (TextView) row.findViewById(R.id.txt_name);
            holder.circleImageView = (CircleImageView) row.findViewById(R.id.img_profile_picture);
            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) row.getTag();
            holder.circleImageView.setImageBitmap(null);
        }

        holder.textView.setText(user.getName());

        // Load user profile pic.
        VolleyUtills.getImageLoader().get(BFacebookManager.getPicUrl(user.getId()),
                VolleyUtills.getImageLoader().getImageListener(
                        holder.circleImageView,
                        R.drawable.ic_user,
                        R.drawable.ic_user));

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
}*/
