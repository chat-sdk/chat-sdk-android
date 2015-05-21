/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.adapter.abstracted;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

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

    protected UserListItemMaker<E> itemMaker = null;

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
                        setViewSelected(position, isChecked);
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

    public void addBUsersRows(List<BUser> users){
        addRows(makeList(users, false, true));
    }

    public void setBUserItems(List<BUser> userItems, boolean sort) {
        setUserItems(makeList(userItems, false, true), sort);
    }

    public void setUserItems(List<E> userItems, boolean sort) {
        filtering = false;

        if (DEBUG) Timber.v("setUserItems, size: %s", (userItems == null ? "NULL" : userItems.size()));
        
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

    /** 
     *  Clear the list.
     * 
     *  Calls notifyDataSetChanged.
     * * */
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

            if (deleteDuplicates && entitiesID.contains(user.getEntityID()))
            {
                continue;
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

    public List<E> getListData() {
        return listData;
    }



    /**
     * Filtering the user list by user name
     *
     * @param startWith the search input. This will be matched against user name in the listData.
     * * */
    public void filterStartWith(String startWith){
        filtering = true;

        if (StringUtils.isBlank(startWith) || StringUtils.isEmpty(startWith))
        {
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

        if (DEBUG) Timber.v("filterItems, Filtered users amount: %s", userItems.size());

        notifyDataSetChanged();
    }

    /**
     * Sorting a given list using the internal comparator.
     * 
     * This will be used each time after setting the user item
     * * */
    protected void sortList(List<E> list){
        if (comparator!=null)
        {
            Collections.sort(list, comparator);
        }
    }


    
    
    /**
     * Toggle the selection state of a list item for a given position
     * @param position the position in the list of the item that need to be toggled
     *                 
     * notifyDataSetChanged will be called.
     * * */
    public boolean toggleSelection(int position){
        boolean selected = setViewSelected(position, !selectedUsersPositions.get(position));
        notifyDataSetChanged();
        return selected;
    }

    /**
     * Set the selection state of a list item for a given position and value
     * @param position the position in the list of the item that need to be toggled.
     * @param selected pass true for selecting the view, false will remove the view from the selectedUsersPositions
     * * */
    public boolean setViewSelected(int position, boolean selected){
        if (selected)
            selectedUsersPositions.put(position, true);
        else
            selectedUsersPositions.delete(position);

        return selected;
    }

    public SparseBooleanArray getSelectedUsersPositions() {
        return selectedUsersPositions;
    }

    /**
     * Get the amount of selected users.
     * * * */
    public int getSelectedCount(){
        return selectedUsersPositions.size();
    }

    /**
     * Select all users
     * 
     * notifyDataSetChanged will be called.
     * * * *
     * * * */
    public void selectAll(){
        for (int i = 0 ; i < userItems.size() ; i++){
            setViewSelected(i, true);
        }

        notifyDataSetChanged();
    }

    /**
     * Clear the selection of all users.
     * 
     * notifyDataSetChanged will be called.
     * * * */
    public void clearSelection(){
        selectedUsersPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }


    
    

    /** 
     * Set the item maker of the list, The item maker will be used for converting the Buser items to list Items.
     * * * */
    public void setItemMaker(UserListItemMaker<E> itemMaker){
        this.itemMaker = itemMaker;
    }

    /**
     * Text color that should be used for text bubbles
     * * * */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    /**
     * Click listener for a click on a user profile image.
     * 
     * This is useful if you want to do different task like open different activities when an
     * profile image or a user item is clicked.
     * * * */
    public void setProfilePicClickListener(ProfilePicClickListener profilePicClickListener) {
        this.profilePicClickListener = profilePicClickListener;
    }


    /**
     * An interface that is used to convert the Buser objects to AbstractUserListItem or one of his descendants.
     * 
     * The maker also take care of making headers and converting a list of items to list with headers.
     * * * */
    protected interface UserListItemMaker<E extends ChatSDKAbstractUsersListAdapter.AbstractUserListItem> {
        public E fromBUser(BUser user);
        public E getHeader(String type);
        public List<E> getListWithHeaders(List<E> list);
    }

    public interface ProfilePicClickListener{
        
        /**
         * @param profilePicView the profile pic view that was clicked
         * @param user the user that own this profile pic
         * */
        public void onClick(View profilePicView, BUser user);
    }

    protected abstract class AbstractProfilePicLoader implements ImageLoader.ImageListener{
        public abstract void kill();
        protected boolean killed = false;
    }

    protected class ProfilePicLoader extends AbstractProfilePicLoader{

        private ImageView profilePic;

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


    
    
    /**
     * Initialize the item maker of the list, The item maker will be used for converting the Buser items to list Items.
     * * * */
    protected abstract void initMaker();


    protected static String getHeaderWithSize(String header, int size){
        return header + " (" + size + ")";
    }
}