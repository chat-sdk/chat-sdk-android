package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/16/2014.
 */
public class UsersWithStatusListAdapter extends BaseAdapter {

    private static final String TAG = UsersWithStatusListAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;

    private Activity mActivity;

    private List<UserListItem> listData = new ArrayList<UserListItem>();

    public static final int TYPE_USER = 1991;
    public static final int TYPE_HEADER = 1992;

    private static final String H_ONLINE = "ONLINE", H_OFFLINE = "OFFLINE";

    private SparseBooleanArray selectedUsersPositions = new SparseBooleanArray();

    //View
    private View row;
    private boolean isMultiSelect = false;

    class ViewHolder {
         CircleImageView profilePicture;
         TextView textView;
         CheckBox checkBox;
    }

    public UsersWithStatusListAdapter(Activity activity){
        mActivity = activity;

        listData = new ArrayList<UserListItem>();

        isMultiSelect = false;
    }

    public UsersWithStatusListAdapter(Activity activity, boolean isMultiSelect){
        mActivity = activity;

        listData = new ArrayList<UserListItem>();

        this.isMultiSelect = isMultiSelect;
    }

    public UsersWithStatusListAdapter(Activity activity, List<UserListItem> listData){
        mActivity = activity;

        if (listData == null)
            listData = new ArrayList<UserListItem>();

        this.listData = listData;

        isMultiSelect = false;
    }

    public UsersWithStatusListAdapter(Activity activity, List<UserListItem> listData, boolean multiSelect){
        mActivity = activity;

        if (listData == null)
            listData = new ArrayList<UserListItem>();

        this.listData = listData;

        this.isMultiSelect = multiSelect;
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
        final UserListItem userItem = listData.get(position);

        // If the row is null or the View inside the row is not good for the current item.
        if (row == null || userItem.getResourceID() != row.getId())
        {
            holder  = new ViewHolder();
            row =  rowForType(holder, position);
        }
        else holder = (ViewHolder) row.getTag();

        holder.textView.setText(userItem.getText());

        if (getItemViewType(position) == TYPE_USER)
        {
           if (userItem.fromURL)
            {
                int size = holder.profilePicture.getHeight();
//                final String path = userItem.getThumbnail(mActivity);

                if (userItem.pictureThumbnailURL != null )
                    VolleyUtills.getImageLoader().get(userItem.pictureThumbnailURL, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {

                                if (DEBUG) Log.i(TAG, "Loading profile picture from url");

                                // load image into imageview
                                holder.profilePicture.setImageBitmap(response.getBitmap());

//                                userItem.saveThumbnail(mActivity, response.getBitmap());
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
                if (DEBUG) Log.i(TAG, "Loading profile picture from the db");

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

//        if (selectedUsersPositions.get(position))
//            row.setBackgroundColor(Color.BLUE);
//        else row.setBackgroundColor(Color.WHITE);

        return row;
    }

    private View rowForType(ViewHolder holder, final int position){
        View row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(listData.get(position).getResourceID(), null);

        holder.textView = (TextView) row.findViewById(R.id.chat_sdk_txt);

        if (isMultiSelect)
        {
            holder.checkBox = (CheckBox) row.findViewById(R.id.checkbox);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedUsersPositions.get(position));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selectView(position, isChecked);
                }
            });
        }

        if (getItemViewType(position) == TYPE_USER)
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.img_profile_picture);

        row.setTag(holder);

        return row;
    }




    public void addRow(UserListItem user){

        listData.add(user);

        notifyDataSetChanged();
    }

    public void addRows(List<UserListItem> users){

        listData.addAll(users);

        notifyDataSetChanged();
    }

    public void setListData(List<UserListItem> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    public List<UserListItem> getListData() {
        return listData;
    }

    /** Clear the list.*/
    public void clear(){
        listData.clear();
        clearSelection();
        notifyDataSetChanged();
    }




    public static class UserListItem{
        String entityID;
        String text;
        Bitmap picture;
        String pictureURL;
        String pictureThumbnailURL;


        private boolean fromURL = false;

        int type, resourceID;

        UserListItem(int resourceID, String entityID, String text, Bitmap picture, int type) {
            this.text = text;
            this.picture = picture;
            this.resourceID  = resourceID;
            this.type = type;
            this.entityID = entityID;
        }

        UserListItem(int resourceID, String entityID, String text,String pictureThumbnailURL, String pictureURL, int type) {
            this.text = text;
            this.pictureURL = pictureURL;
            this.pictureThumbnailURL = pictureThumbnailURL;
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
            return new UserListItem(R.layout.chat_sdk_row_contact,
                    user.getEntityID(),
                    user.getMetaName(),
                    user.getThumbnailPictureURL(),
                    user.getMetaPictureUrl(),
                    TYPE_USER);
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

/*        public boolean hasThumbnail(Context context){
            File file = Utils.ThumbnailsHandler.getThumbnail(context, entityID);
            return file != null && file.exists();
        }

        public String getThumbnail(Context context){
            File file = Utils.ThumbnailsHandler.getThumbnail(context, entityID);
            if (file != null && file.exists())
                return file.getAbsolutePath();

            return null;
        }

        public void saveThumbnail(Context context, Bitmap bitmap){
            Utils.ThumbnailsHandler.saveImageThumbnail(context, bitmap, entityID);
        }*/
    }

    /** Make aUserListItem list from BUser list.
     * @param withHeaders If true list will have headers for online and offline users.
     * @return a list with all the user item for the adapter.*/
    public static  List<UserListItem> makeList(List<BUser> users, boolean withHeaders){
        return makeList(users, withHeaders, false);
    }

    /** Make aUserListItem list from BUser list.
     * @param withHeaders If true list will have headers for online and offline users.
     * @param  deleteDuplicates If true any duplicate entity(share same entity id) will be skipped.
     * @return a list with all tge user item for the adapter.*/
    public static List<UserListItem> makeList(List<BUser> users, boolean withHeaders, boolean deleteDuplicates){
//        if (DEBUG) Log.v(TAG, "makeList" + (withHeaders?", With Headers" : "" )+ (deleteDuplicates? ", Delete duplicates." :".") );
        if (users == null)
            return new ArrayList<UserListItem>();

        List<UserListItem> listData = new ArrayList<UserListItem>();
        List<String> entitiesID = new ArrayList<String>();

        List<UserListItem> onlineUsers = new ArrayList<UserListItem>();
        List<UserListItem> offlineUsers = new ArrayList<UserListItem>();

        for (BUser user : users){

            // Not showing users that has no name.
            if (StringUtils.isEmpty(user.getMetaName()))
                continue;

            if (deleteDuplicates)
            {
//                if (DEBUG) Log.d(TAG, "EntityID: " + user.getEntityID());
                if (entitiesID.contains(user.getEntityID()))
                {
//                    if (DEBUG) Log.d(TAG, "EntityExist");
                    continue;
                }
            }

            entitiesID.add(user.getEntityID());

            if (withHeaders)
            {
                if (user.getOnline() != null && user.getOnline()) {
                    onlineUsers.add(UserListItem.fromBUser(user));
                }
                else offlineUsers.add(UserListItem.fromBUser(user));
            }
            else
            {
                listData.add(UserListItem.fromBUser(user));
            }
        }

        if (withHeaders) {
            listData.add(UserListItem.getHeader(H_ONLINE));
            listData.addAll(onlineUsers);
            listData.add(UserListItem.getHeader(H_OFFLINE));
            listData.addAll(offlineUsers);
        }

//        if (DEBUG && deleteDuplicates)
//            for (String s : entitiesID)
//                Log.d(TAG, "Report EntityID: " + s);

        return listData;
    }






















    /*############################################*/
    public void toggleSelection(int position){
        selectView(position, !selectedUsersPositions.get(position));
    }

    public void selectView(int position, boolean value){
        if (value)
            selectedUsersPositions.put(position, value);
        else
            selectedUsersPositions.delete(position);

//        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedUsersPositions() {
        return selectedUsersPositions;
    }

    public int getSelectedCount(){
        return selectedUsersPositions.size();
    }

    public void clearSelection(){
        selectedUsersPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }
}