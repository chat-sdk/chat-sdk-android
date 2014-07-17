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
import com.braunster.chatsdk.dao.BUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/16/2014.
 */
public class UsersListAdapter extends BaseAdapter {

    private static final String TAG = UsersListAdapter.class.getSimpleName();

    private Activity mActivity;

    private List<BUser> listData = new ArrayList<BUser>();

    private int textColor = -1;

    //View
    private View row;

    class ViewHolder {
         CircleImageView profilePicture;
         TextView textView;
    }

    public UsersListAdapter(Activity activity){
        mActivity = activity;
    }

    public UsersListAdapter(Activity activity, List<BUser> listData){
        mActivity = activity;
        Log.d(TAG, "Contacts: " + listData.size());
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public BUser getItem(int i) {
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
        if ( row == null)
        {
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.chat_sdk_row_contact, null);
            holder = new ViewHolder();
            holder.textView = (TextView) row.findViewById(R.id.chat_sdk_txt);
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.img_profile_picture);
            row.setTag(holder);
        }
        else holder = (ViewHolder) row.getTag();

        holder.textView.setText(listData.get(position).getMetaName());

        /*FIXME work with url
        Bitmap bitmap = listData.get(position).getMetaPicture();
        if (bitmap != null)
        {
            holder.profilePicture.setImageBitmap(bitmap);
        }*/
        return row;
    }

    public void addRow(BUser user){

        listData.add(user);

        notifyDataSetChanged();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setListData(List<BUser> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    public void clear(){
        listData.clear();
        notifyDataSetChanged();
    }
}