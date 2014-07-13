package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/16/2014.
 */
public class UsersWithStatusListAdapter extends BaseAdapter {

    private static final String TAG = UsersWithStatusListAdapter.class.getSimpleName();

    private Activity mActivity;

    private List<UserListItem> listData = new ArrayList<UserListItem>();

    public static final int TYPE_USER = 1991;
    public static final int TYPE_HEADER = 1992;

    private SparseBooleanArray selectedUsersIds = new SparseBooleanArray();

    //View
    private View row;

    class ViewHolder {
         CircleImageView profilePicture;
         TextView textView;
    }

    public UsersWithStatusListAdapter(Activity activity){
        mActivity = activity;
    }

    public UsersWithStatusListAdapter(Activity activity, List<UserListItem> listData){
        mActivity = activity;
        Log.d(TAG, "Contacts: " + listData.size());
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public UserListItem getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).getType();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        row = view;

        final ViewHolder holder;

        // If the row is null or the View inside the row is not good for the current item.
        if (row == null || listData.get(position).getResourceID() != row.getId())
        {
            holder  = new ViewHolder();
            row =  rowForType(holder, position);
        }
        else holder = (ViewHolder) row.getTag();

        holder.textView.setText(listData.get(position).getText());

        if (getItemViewType(position) == TYPE_USER)
        {
            if (listData.get(position).fromURL)
            {
                int size = row.getHeight();
                if (listData.get(position).pictureURL != null)
                    VolleyUtills.getImageLoader().get(listData.get(position).pictureURL, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                // load image into imageview
                                holder.profilePicture.setImageBitmap(response.getBitmap());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Image Load Error: " + error.getMessage());
                        }
                    },size, size);
                else holder.profilePicture.setImageResource(R.drawable.icn_user_x_2);
            }
            else
            {
                Bitmap bitmap = listData.get(position).getPicture();
                if (bitmap != null)
                {
                    holder.profilePicture.setImageBitmap(bitmap);
                }
                else
                {
                    holder.profilePicture.setImageResource(R.drawable.icn_user_x_2);
                }
            }
        }

        if (selectedUsersIds.get(position))
            row.setBackgroundColor(Color.BLUE);
        else row.setBackgroundColor(Color.WHITE);

        return row;
    }

    private View rowForType(ViewHolder holder, int position){
        View row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(listData.get(position).getResourceID(), null);

        holder.textView = (TextView) row.findViewById(R.id.chat_sdk_txt);

        if (getItemViewType(position) == TYPE_USER)
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.img_profile_picture);

        row.setTag(holder);

        return row;
    }

    public void addRow(UserListItem user){

        listData.add(user);

        notifyDataSetChanged();
    }

    public void setListData(List<UserListItem> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    public void clear(){
        listData.clear();
        notifyDataSetChanged();
    }

    public static class UserListItem{
        String entityID;
        String text;
        Bitmap picture;
        String pictureURL;

        private boolean fromURL = false;

        int type, resourceID;

        UserListItem(int resourceID, String entityID, String text, Bitmap picture, int type) {
            this.text = text;
            this.picture = picture;
            this.resourceID  = resourceID;
            this.type = type;
            this.entityID = entityID;
        }

        UserListItem(int resourceID, String entityID, String text, String pictureURL, int type) {
            this.text = text;
            this.pictureURL = pictureURL;
            this.resourceID  = resourceID;
            this.type = type;
            this.entityID = entityID;
            this.fromURL = true;
        }

        UserListItem(int resourceID, String text, int type) {
            this.text = text;
            this.resourceID = resourceID;
            this.type = type;
        }

        public static UserListItem fromBUser(BUser user){
            return new UserListItem(R.layout.chat_sdk_row_contact, user.getEntityID(), user.getMetaName(), user.getMetaPictureUrl(), TYPE_USER);
        }

        public static UserListItem getHeader(String text){
            return new UserListItem(R.layout.chat_sdk_list_header, text, TYPE_HEADER);
        }

        public String getText() {
            return text;
        }

        public Bitmap getPicture() {
            return picture;
        }

        public int getType() {
            return type;
        }

        public int getResourceID() {
            return resourceID;
        }

        public String getEntityID() {
            return entityID;
        }

        public boolean isFromURL() {
            return fromURL;
        }

        public BUser asBUser(){
            return DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, entityID);
        }
    }

    public static List<UserListItem> makeList(List<BUser> users, boolean withHeaders){

        if (users == null)
            return new ArrayList<UserListItem>();

        List<UserListItem> listData = new ArrayList<UserListItem>();

        if (!withHeaders)
        {
            for (BUser user : users){
                listData.add(UserListItem.fromBUser(user));
            }
            return listData;
        }

        List<UserListItem> onlineUsers = new ArrayList<UserListItem>();
        List<UserListItem> offlineUsers = new ArrayList<UserListItem>();

        for (BUser user : users){
            if (user.getOnline() != null && user.getOnline()) {
                onlineUsers.add(UserListItem.fromBUser(user));
            }
            else offlineUsers.add(UserListItem.fromBUser(user));
        }

        listData.add(UserListItem.getHeader("Online"));
        listData.addAll(onlineUsers);
        listData.add(UserListItem.getHeader("Offline"));
        listData.addAll(offlineUsers);

        return listData;
    }

    /*############################################*/
    public void toggleSelection(int position){
        selectView(position, !selectedUsersIds.get(position));
    }

    public void selectView(int position, boolean value){
        if (value)
            selectedUsersIds.put(position, value);
        else
            selectedUsersIds.delete(position);

        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedUsersIds() {
        return selectedUsersIds;
    }

    public int getSelectedCount(){
        return selectedUsersIds.size();
    }

    public void clearSelection(){
        selectedUsersIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }
}