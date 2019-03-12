/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.contacts;

import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.AvailabilityHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class UsersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final int TYPE_USER = 0;
    protected static final int TYPE_HEADER = 1;

    protected List<Object> items = new ArrayList<>();
    protected List<String> headers = new ArrayList<>();

    protected SparseBooleanArray selectedUsersPositions = new SparseBooleanArray();

    protected boolean isMultiSelect = false;
    protected final PublishSubject<Object> onClickSubject = PublishSubject.create();
    protected final PublishSubject<Object> onLongClickSubject = PublishSubject.create();

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.header_text);
        }
    }

    protected class UserViewHolder extends RecyclerView.ViewHolder {

        protected SimpleDraweeView avatarImageView;
        protected TextView nameTextView;
        protected CheckBox checkBox;
        protected TextView statusTextView;
        protected ImageView availabilityImageView;

        public UserViewHolder(View view) {
            super(view);

            nameTextView = view.findViewById(R.id.chat_sdk_txt);
            statusTextView = view.findViewById(R.id.tvStatus);
            availabilityImageView = view.findViewById(R.id.ivAvailability);
            avatarImageView = view.findViewById(R.id.img_profile_picture);
            checkBox = view.findViewById(R.id.checkbox);

            // Clicks are handled at the list item level
            checkBox.setClickable(false);
        }
    }

    public UsersListAdapter() {
        this(null, false);
    }

    public UsersListAdapter(boolean isMultiSelect) {
        this(null, isMultiSelect);
    }

    public UsersListAdapter(List<UserListItem> users, boolean multiSelect) {
        if (users == null) {
            users = new ArrayList<>();
        }

        setUsers(users);

        this.isMultiSelect = multiSelect;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (headers.contains(item)) {
            return TYPE_HEADER;
        }
        else {
            return TYPE_USER;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View row = inflater.inflate(R.layout.chat_sdk_row_header, null);
            return new HeaderViewHolder(row);
        }
        else if (viewType == TYPE_USER) {
            View row = inflater.inflate(R.layout.chat_sdk_row_contact, null);
            return new UserViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        int type = getItemViewType(position);
        final Object item = items.get(position);

        holder.itemView.setOnClickListener(view -> onClickSubject.onNext(item));
        holder.itemView.setOnLongClickListener(view -> {
            onLongClickSubject.onNext(item);
            return true;
        });

        if (type == TYPE_HEADER) {
            HeaderViewHolder hh = (HeaderViewHolder) holder;
            String header = (String) item;
            hh.textView.setText(header);
        }

        if (type == TYPE_USER) {
            UserViewHolder uh = (UserViewHolder) holder;
            UserListItem user = (UserListItem) item;

            uh.nameTextView.setText(user.getName());

            uh.availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(user.getAvailability()));
            uh.statusTextView.setText(user.getStatus());

            Timber.v("User: " + user.getName() + " Availability: " + user.getAvailability());

            uh.avatarImageView.setImageURI(user.getAvatarURL());

            if (isMultiSelect && user instanceof User) {
                uh.checkBox.setVisibility(View.VISIBLE);
                uh.checkBox.setChecked(selectedUsersPositions.get(position));
                uh.availabilityImageView.setVisibility(View.INVISIBLE);
            }
            else {
                uh.availabilityImageView.setVisibility(View.VISIBLE);
            }
        }

    }

    public Object getItem(int i) {
        return items.get(i);
    }

    public void setUsers(List<UserListItem> users, boolean sort) {
        this.items.clear();

        if (sort) {
            sortList(users);
        }

        for (UserListItem item : users) {
            addUser(item);
        }

        notifyDataSetChanged();
    }

    public void addUser (UserListItem user) {
        addUser(user, false);
    }

    public List<Object> getItems () {
        return items;
    }

    public void addUser (UserListItem user, boolean notify) {
        addUser(user, -1, notify);
    }

    public void addUser (UserListItem user, int atIndex, boolean notify) {
        if (!items.contains(user)) {
            if (atIndex >= 0) {
                items.add(atIndex, user);
            }
            else {
                items.add(user);
            }
            if (notify) {
                notifyDataSetChanged();
            }
        }
    }

    public void addHeader (String header) {
        if (!items.contains(header)) {
            items.add(header);
            headers.add(header);
        }
    }

    public List<UserListItem> getSelectedUsers () {
        List<UserListItem> users = new ArrayList<>();
        for (int i = 0 ; i < getSelectedCount() ; i++) {
            int pos = getSelectedUsersPositions().keyAt(i);
            if (items.get(pos) instanceof UserListItem) {
                users.add(((UserListItem) items.get(pos)));
            }
        }
        return users;
    }

    public void setUsers(List<UserListItem> userItems) {
        setUsers(userItems, false);
    }

    /**
     *  Clear the list.
     * 
     *  Calls notifyDataSetChanged.
     * * */
    public void clear() {
        items.clear();
        clearSelection();
        notifyDataSetChanged();
    }

    public UserListItem userAtPosition (int position) {
        Object item = getItem(position);
        if (item instanceof UserListItem) {
            return (UserListItem) item;
        }
        else {
            return null;
        }
    }

    /**
     * Sorting a given list using the internal comparator.
     * 
     * This will be used each time after setting the user item
     * * */
    protected void sortList(List<UserListItem> list) {
        Comparator comparator = (Comparator<UserListItem>) (u1, u2) -> {
            String s1 = "";
            if (u1 != null && u1.getName() != null) {
                s1 = u1.getName();
            }
            String s2 = "";
            if (u2 != null && u2.getName() != null) {
                s2 = u2.getName();
            }

            return s1.compareToIgnoreCase(s2);
        };
        Collections.sort(list, comparator);
    }

    /**
     * Toggle the selection state of a list item for a given position
     * @param position the position in the list of the item that need to be toggled
     *                 
     * notifyDataSetChanged will be called.
     * * */
    public boolean toggleSelection(int position) {
        return setViewSelected(position, !selectedUsersPositions.get(position));
    }

    public boolean toggleSelection(Object object) {
        int position = items.indexOf(object);
        return toggleSelection(position);
    }

    /**
     * Set the selection state of a list item for a given position and value
     * @param position the position in the list of the item that need to be toggled.
     * @param selected pass true for selecting the view, false will remove the view from the selectedUsersPositions
     * * */
    public boolean setViewSelected(int position, boolean selected) {
        UserListItem user = userAtPosition(position);
        if (user != null) {
            if (selected) {
                selectedUsersPositions.put(position, true);
            }
            else {
                selectedUsersPositions.delete(position);
            }
            notifyItemChanged(position);

            return selected;
        }
        return false;
    }

    public SparseBooleanArray getSelectedUsersPositions() {
        return selectedUsersPositions;
    }

    /**
     * Get the amount of selected users.
     * * * */
    public int getSelectedCount() {
        return selectedUsersPositions.size();
    }

    /**
     * Select all users
     * 
     * notifyDataSetChanged will be called.
     */
    public void selectAll() {
        for (int i = 0; i < items.size(); i++) {
            setViewSelected(i, true);
        }

        notifyDataSetChanged();
    }

    /**
     * Clear the selection of all users.
     * 
     * notifyDataSetChanged will be called.
     */
    public void clearSelection() {
        selectedUsersPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public Observable<Object> onClickObservable () {
        return onClickSubject;
    }

    public Observable<Object> onLongClickObservable () {
        return onLongClickSubject;
    }

    @Deprecated
    public Observable<Object> getItemClicks () {
        return onClickObservable();
    }

}
