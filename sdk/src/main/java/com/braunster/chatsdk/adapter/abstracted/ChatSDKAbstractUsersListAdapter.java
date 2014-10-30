package com.braunster.chatsdk.adapter.abstracted;

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
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/16/2014.
 */
public abstract class ChatSDKAbstractUsersListAdapter<E extends ChatSDKAbstractUsersListAdapter.AbstractUserListItem> extends BaseAdapter {

    private static final String TAG = ChatSDKAbstractUsersListAdapter.class.getSimpleName();
    private static final boolean DEBUG = Debug.UsersWithStatusListAdapter;

    protected Activity mActivity;

    protected List<E> listData = new ArrayList<E>();
    protected List<E> userItems = new ArrayList<E>();
    protected List<String> userIDs = new ArrayList<String>();

    protected Comparator<E> comparator;

    public static final int TYPE_USER = 1991;
    public static final int TYPE_HEADER = 1992;

    protected static final String H_ONLINE = "ONLINE", H_OFFLINE = "OFFLINE", H_NO_ONLINE = "NO ONLINE CONTACTS", H_NO_OFFLINE = "NO OFFLINE CONTACTS", H_NO_CONTACTS = "NO CONTACTS";

    protected SparseBooleanArray selectedUsersPositions = new SparseBooleanArray();
    protected List<AbstractUserListItem> online = new ArrayList<AbstractUserListItem>(), offline = new ArrayList<AbstractUserListItem>();
    //View
    protected View row;
    protected boolean isMultiSelect = false;

    protected boolean filtering = false;

    protected int textColor =-1991;

    protected ProfilePicClickListener profilePicClickListener;

    public class ViewHolder {
        public CircleImageView profilePicture;
        public TextView textView;
        public CheckBox checkBox;

        public AbstractProfilePicLoader profilePicLoader;
    }

    public ChatSDKAbstractUsersListAdapter(Activity activity){
        mActivity = activity;

        initMaker();

        userItems = new ArrayList<E>();

        isMultiSelect = false;
    }

    public ChatSDKAbstractUsersListAdapter(Activity activity, boolean isMultiSelect){
        mActivity = activity;

        initMaker();

        userItems = new ArrayList<E>();

        this.isMultiSelect = isMultiSelect;
    }

    public ChatSDKAbstractUsersListAdapter(Activity activity, List<E> userItems){
        mActivity = activity;

        initMaker();

        if (userItems == null)
            userItems = new ArrayList<E>();

        setUserItems(userItems);

        isMultiSelect = false;
    }

    public ChatSDKAbstractUsersListAdapter(Activity activity, List<E> userItems, boolean multiSelect){
        mActivity = activity;

        initMaker();

        if (userItems == null)
            userItems = new ArrayList<E>();

        setUserItems(userItems);

        this.isMultiSelect = multiSelect;
    }

    protected UserListItemMaker<E> itemMaker = null;


    @Override
    public int getCount() {
        return userItems.size();
    }

    @Override
    public AbstractUserListItem getItem(int i) {
        return userItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return userItems.get(position).getType();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        row = view;

        final ViewHolder holder;
        final AbstractUserListItem userItem = userItems.get(position);

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

                if (userItem.pictureThumbnailURL != null )
                    VolleyUtils.getImageLoader().get(userItem.pictureThumbnailURL, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (isImmediate && response.getBitmap() == null)
                            {
                                holder.profilePicture.setImageResource(R.drawable.ic_profile);
                                return;
                            }

                            if (response.getBitmap() != null) {

                                if (DEBUG) Log.i(TAG, "Loading profile picture from url");
                                // load image into imageview
                                holder.profilePicture.setImageBitmap(response.getBitmap());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (DEBUG) Log.e(TAG, "Image Load Error: " + error.getMessage());
                            holder.profilePicture.setImageResource(R.drawable.ic_profile);
                        }
                    },size, size);
                else holder.profilePicture.setImageResource(R.drawable.ic_profile);
            }
            else
            {
                if (DEBUG) Log.i(TAG, "Loading profile picture from the db");

                Bitmap bitmap = userItems.get(position).getPicture();
                if (bitmap != null)
                {
                    holder.profilePicture.setImageBitmap(bitmap);
                }
                else
                {
                    holder.profilePicture.setImageResource(R.drawable.ic_profile);
                }
            }
        }

        return row;
    }

    protected View rowForType(ViewHolder holder, final int position){
        View row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(userItems.get(position).getResourceID(), null);

        holder.textView = (TextView) row.findViewById(R.id.chat_sdk_txt);

        if (getItemViewType(position) == TYPE_USER)
        {
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.img_profile_picture);
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
        }

        row.setTag(holder);

        return row;
    }

    /** Disabling the header vies from clicks.*/
    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_USER;
    }

    public void addRow(E user){
        userItems.add(user);

        userIDs.add(user.getEntityID());

        notifyDataSetChanged();
    }

    public void addRow(BUser user){
        userItems.add(itemMaker.fromBUser(user));

        userIDs.add(user.getEntityID());

        notifyDataSetChanged();
    }

    public void addRows(List<E> users){

        userItems.addAll(users);

        notifyDataSetChanged();
    }

    public void setUserItems(List<E> userItems, boolean sort) {
        filtering = false;

        if (DEBUG) Log.v(TAG, "setUserItems, size: " + (userItems==null?"NULL":userItems.size()) );
        this.userItems.clear();
        this.listData.clear();

        userIDs.clear();

        this.userItems = userItems;
        this.listData = userItems;

        if (sort)
            sortList(userItems);

        for (AbstractUserListItem item : userItems)
            userIDs.add(item.getEntityID());

        notifyDataSetChanged();
    }

    public void setUserItems(List<E> userItems) {
        setUserItems(userItems, false);
    }

    public boolean isItemExist(String entityID){
        return userIDs.contains(entityID);
    }

    public List<E> getUserItems() {
        return userItems;
    }

    /** Clear the list.*/
    public void clear(){
        userItems.clear();
        listData.clear();
        clearSelection();
        notifyDataSetChanged();
    }

    public boolean useCustomItems(){
        return itemMaker != null;
    }

    public static class AbstractUserListItem implements Serializable{
        public String entityID;
        public String text;
        public Bitmap picture;
        public String pictureURL;
        public String pictureThumbnailURL;
        public boolean online;


        public boolean fromURL = false;

        public int type, resourceID;

        public AbstractUserListItem(int resourceID, String entityID, String text, String pictureThumbnailURL, String pictureURL, int type, boolean online) {
            this.text = text;
            this.online = online;
            this.pictureURL = pictureURL;
            this.pictureThumbnailURL = pictureThumbnailURL;
            this.resourceID  = resourceID;
            this.type = type;
            this.entityID = entityID;
            this.fromURL = true;
        }

        public AbstractUserListItem(int resourceID, String text, int type) {
            this.text = text;
            this.resourceID = resourceID;
            this.type = type;
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

    /** Make aUserListItem list from BUser list.
     * @param withHeaders If true list will have headers for online and offline users.
     * @param  deleteDuplicates If true any duplicate entity(share same entity id) will be skipped.
     * @return a list with all tge user item for the adapter.*/
    public List<E> makeList(List<BUser> users, boolean withHeaders, boolean deleteDuplicates){
//        if (DEBUG) Log.v(TAG, "makeList" + (withHeaders?", With Headers" : "" )+ (deleteDuplicates? ", Delete duplicates." :".") );
        if (users == null)
            return new ArrayList<E>();

        List<E> listData = new ArrayList<E>();
        List<String> entitiesID = new ArrayList<String>();

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

            listData.add(itemMaker.fromBUser(user));
        }


        if (withHeaders) {
            listData = itemMaker.getListWithHeaders(listData);
        }
        else {
            sortList(listData);
        }

//        if (DEBUG && deleteDuplicates)
//            for (String s : entitiesID)
//                Log.d(TAG, "Report EntityID: " + s);

        return listData;
    }

    protected static String getHeaderWithSize(String header, int size){
        return header + " (" + size + ")";
    }

    /*Filtering option's of the list to make searches.*/
    public void filterStartWith(String startWith){
        filtering = true;

        if (StringUtils.isBlank(startWith) || StringUtils.isEmpty(startWith))
        {
            if (DEBUG) Log.v(TAG, "filterItems, Empty Filter");
            if (DEBUG) Log.d(TAG, "User items size: " + userItems.size() + ", ListData: " + listData.size());
            this.userItems = listData;
        }
        else
        {
            startWith = startWith.trim();

            List<E> filteredUsers = new ArrayList<E>();

            for (E u : listData)
            {
                if (u.getText().toLowerCase().startsWith(startWith.toLowerCase()))
                    filteredUsers.add(u);
            }

            this.userItems = filteredUsers;
        }

        sortList(userItems);

        if (DEBUG) Log.v(TAG, "filterItems, Filtered users amount: " + userItems.size());

        notifyDataSetChanged();
    }

    public List<E> getListData() {
        return listData;
    }

    protected void sortList(List<E> list){
        if (comparator!=null)
        {
            Collections.sort(list, comparator);
        }
    }


    /*############################################*/
    /*Selection*/
    public boolean toggleSelection(int position){
        boolean selected = selectView(position, !selectedUsersPositions.get(position));
        notifyDataSetChanged();
        return selected;
    }

    public boolean selectView(int position, boolean value){
        if (value)
            selectedUsersPositions.put(position, value);
        else
            selectedUsersPositions.delete(position);

        return value;
    }

    public SparseBooleanArray getSelectedUsersPositions() {
        return selectedUsersPositions;
    }

    public int getSelectedCount(){
        return selectedUsersPositions.size();
    }

    public void selectAll(){
        for (int i = 0 ; i < userItems.size() ; i++){
            selectView(i, true);
        }

        notifyDataSetChanged();
    }

    public void clearSelection(){
        selectedUsersPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }



    /*Setters*/

    public void setItemMaker(UserListItemMaker<E> itemMaker){
        this.itemMaker = itemMaker;
    }


    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setProfilePicClickListener(ProfilePicClickListener profilePicClickListener) {
        this.profilePicClickListener = profilePicClickListener;
    }

    /*############################################*/
    public interface UserListItemMaker<E>{
        public E fromBUser(BUser user);
        public E getHeader(String type);
        public List<E> getListWithHeaders(List<E> list);
    }

    public interface ProfilePicClickListener{
        public void onClick(View profilePicView, BUser user);
    }

    public abstract class AbstractProfilePicLoader implements ImageLoader.ImageListener{
        public abstract void kill();
        protected boolean killed = false;
    }

    public class ProfilePicLoader extends AbstractProfilePicLoader{

        private CircleImageView profilePic;

        public ProfilePicLoader(CircleImageView profilePic) {
            this.profilePic = profilePic;
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            if (isImmediate && response.getBitmap() == null)
            {
                profilePic.setImageResource(com.braunster.chatsdk.R.drawable.ic_profile);
                return;
            }

            if (response.getBitmap() != null && !killed) {
                // load image into imageview
                profilePic.setImageBitmap(response.getBitmap());
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (!killed)
                profilePic.setImageResource(com.braunster.chatsdk.R.drawable.ic_profile);
        }

        @Override
        public void kill(){
            killed = true;
        }
    }
    /*############################################*/
    public abstract void initMaker();
}