/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.contacts;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import co.chatsdk.core.dao.User;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class UsersListAdapter extends BaseAdapter {

    private AppCompatActivity mActivity;

    private List<UserListItem> listData = new ArrayList<>();
    private List<UserListItem> userItems = new ArrayList<>();
    private List<String> userIDs = new ArrayList<>();
    private RowClickListener rowClickListener;
    private RowClickListener profileImageClickListener;

    public static final int TYPE_USER = 1991;

    protected SparseBooleanArray selectedUsersPositions = new SparseBooleanArray();

    protected boolean isMultiSelect = false;

    protected boolean filtering = false;

    public class ViewHolder {
        public CircleImageView profilePicture;
        public TextView textView;
        public CheckBox checkBox;
    }

    private static final boolean DEBUG = Debug.UsersWithStatusListAdapter;

    public UsersListAdapter(AppCompatActivity activity){
        this(activity, null, false);
    }

    public UsersListAdapter(AppCompatActivity activity, boolean isMultiSelect){
        this(activity, null, isMultiSelect);
    }

    public UsersListAdapter(AppCompatActivity activity, List<UserListItem> userItems, boolean multiSelect){
        mActivity = activity;

        if (userItems == null) {
            userItems = new ArrayList<>();
        }

        setUserItems(userItems);

        this.isMultiSelect = multiSelect;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        View row = view;

        final ViewHolder holder;
        final UserListItem userItem = userItems.get(position);

        // If the row is null or the View inside the row is not good for the current item.
        if (row == null || userItem.getResourceID() != row.getId()) {
            holder  = new ViewHolder();
            row = rowForType(holder, position);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }

        holder.textView.setText(userItem.getText());

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UsersListAdapter.this.rowClickListener != null) {
                    UsersListAdapter.this.rowClickListener.click(position);
                }
            }
        });

        if (getItemViewType(position) == TYPE_USER) {

            userItem.getUser().putAvatar(holder.profilePicture).subscribe();

            // If this is included then the outer click won't work

            holder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(UsersListAdapter.this.profileImageClickListener != null) {
                        UsersListAdapter.this.profileImageClickListener.click(position);
                    }
                }
            });
        }

        return row;
    }

    public void setRowClickListener (RowClickListener listener) {
        this.rowClickListener = listener;
    }

    public void setProfileImageClickListener (RowClickListener listener) {
        this.profileImageClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return userItems.get(position).getItemType();
    }

    @Override
    public int getCount() {
        return userItems.size();
    }

    @Override
    public UserListItem getItem(int i) {
        return userItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    protected View rowForType(ViewHolder holder, final int position){
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row =  inflater.inflate(userItems.get(position).getResourceID(), null);

        holder.textView = (TextView) row.findViewById(R.id.chat_sdk_txt);

        if (getItemViewType(position) == TYPE_USER) {
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.img_profile_picture);

            if (isMultiSelect) {
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
        return false;
    }

    public void setUsers(List<User> users, boolean sort) {
        ArrayList<UserListItem> items = new ArrayList<>();
        for(User user : users) {
            items.add(new UserListItem(user, R.layout.chat_sdk_row_contact, TYPE_USER));
        }

        setUserItems(items, sort);
    }

    public void setUserItems(List<UserListItem> userItems, boolean sort) {
        filtering = false;

        if (DEBUG) Timber.v("setUserItems, size: %s", (userItems == null ? "NULL" : userItems.size()));
        
        this.userItems.clear();
        this.listData.clear();

        userIDs.clear();

        this.userItems = userItems;
        this.listData = userItems;

        if (sort)
            sortList(userItems);

        for (UserListItem item : userItems)
            userIDs.add(item.getEntityID());

        notifyDataSetChanged();
    }

    public void setUserItems(List<UserListItem> userItems) {
        setUserItems(userItems, false);
    }

    public List<UserListItem> getUserItems() {
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

    /**
     * Filtering the user list by user name
     *
     * @param startWith the search input. This will be matched against user name in the listItems.
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

            List<UserListItem> filteredUsers = new ArrayList<>();

            for (UserListItem u : listData)
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
    protected void sortList(List<UserListItem> list){
        Comparator comparator = new Comparator<UserListItem>() {
            @Override
            public int compare(UserListItem lhs, UserListItem rhs) {
                return lhs.getText().toLowerCase().compareTo(rhs.getText().toLowerCase());
            }
        };
        Collections.sort(list, comparator);
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
     */
    public void selectAll() {
        for (int i = 0; i < userItems.size(); i++) {
            setViewSelected(i, true);
        }

        notifyDataSetChanged();
    }

    /**
     * Clear the selection of all users.
     * 
     * notifyDataSetChanged will be called.
     */
    public void clearSelection(){
        selectedUsersPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public interface RowClickListener {
         void click (int position);
    }

}