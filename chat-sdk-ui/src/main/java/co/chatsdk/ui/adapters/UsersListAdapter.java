/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.chat.binders.OnlineStatusBinder;
import co.chatsdk.ui.utils.AvailabilityHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;


public class UsersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<UserListItem> items = new ArrayList<>();
    protected List<String> headers = new ArrayList<>();

    protected SparseBooleanArray selectedUsersPositions = new SparseBooleanArray();

    protected boolean multiSelectEnabled;
    protected final PublishSubject<Object> onClickSubject = PublishSubject.create();
    protected final PublishSubject<Object> onLongClickSubject = PublishSubject.create();
    protected final PublishSubject<List<UserListItem>> onToggleSubject = PublishSubject.create();

    protected class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.image_avatar) protected ImageView avatarImageView;
        @BindView(R2.id.text_name) protected TextView nameTextView;
        @BindView(R2.id.checkbox) protected CheckBox checkBox;
        @BindView(R2.id.text_status) protected TextView statusTextView;
        @BindView(R2.id.image_availability) protected ImageView availabilityImageView;
        @BindView(R2.id.onlineIndicator) protected View onlineIndicator;

        public UserViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            // Clicks are handled at the list item level
            checkBox.setClickable(false);
        }

        public void setMultiSelectEnabled (boolean enabled) {
            if (enabled) {
                checkBox.setVisibility(View.VISIBLE);
                availabilityImageView.setVisibility(View.INVISIBLE);
            } else {
                availabilityImageView.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.INVISIBLE);
            }
        }
    }

    public UsersListAdapter() {
        this(null, false);
    }

    public UsersListAdapter(boolean multiSelectEnabled) {
        this(null, multiSelectEnabled);
    }

    public UsersListAdapter(List<UserListItem> users, boolean multiSelect) {
        if (users == null) {
            users = new ArrayList<>();
        }

        setUsers(users);

        this.multiSelectEnabled = multiSelect;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.view_user_row, parent, false);
        return new UserViewHolder(row);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        UserViewHolder viewHolder = (UserViewHolder) holder;
        UserListItem item = items.get(position);

        viewHolder.setMultiSelectEnabled(multiSelectEnabled);

        viewHolder.nameTextView.setText(item.getName());

        if (StringChecker.isNullOrEmpty(item.getAvailability())) {
            viewHolder.availabilityImageView.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.availabilityImageView.setVisibility(View.VISIBLE);
            viewHolder.availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(item.getAvailability()));
        }

        OnlineStatusBinder.bind(viewHolder.onlineIndicator, item.getIsOnline());

        viewHolder.statusTextView.setText(item.getStatus());

        Logger.debug("User: " + item.getName() + " Availability: " + item.getAvailability());

        Context context = ChatSDK.shared().context();

        int width = Dimen.from(context, R.dimen.small_avatar_width);
        int height = Dimen.from(context, R.dimen.small_avatar_height);

        if (item instanceof User) {
            ((User) item).loadAvatar(viewHolder.avatarImageView, width, height);
        } else {
            Picasso.get().load(item.getAvatarURL()).resize(width, height).into(viewHolder.avatarImageView);
        }


        if (multiSelectEnabled) {
            viewHolder.checkBox.setChecked(selectedUsersPositions.get(position));
        }


        holder.itemView.setOnClickListener(view -> onClickSubject.onNext(item));
        holder.itemView.setOnLongClickListener(view -> {
            onLongClickSubject.onNext(item);
            return true;
        });

    }

    public UserListItem getItem(int i) {
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

    public List<UserListItem> getItems () {
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

    public List<UserListItem> getSelectedUsers () {
        List<UserListItem> users = new ArrayList<>();
        for (int i = 0 ; i < getSelectedCount() ; i++) {
            int pos = getSelectedUsersPositions().keyAt(i);
            users.add((items.get(pos)));
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

    /**
     * Sorting a given list using the internal comparator.
     * 
     * This will be used each time after setting the user item
     * * */
    protected void sortList(List<UserListItem> list) {
        Comparator comparator = (Comparator<UserListItem>) (u1, u2) -> {
            boolean u1online = u1.getIsOnline();
            boolean u2online = u2.getIsOnline();
            if (u1online != u2online) {
                if (u1online) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                String s1 = u1.getName() != null ? u1.getName() : "";
                String s2 = u2.getName() != null ? u2.getName() : "";

                return s1.compareToIgnoreCase(s2);
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
    public boolean toggleSelection(int position) {
        boolean selected = setViewSelected(position, !selectedUsersPositions.get(position));
        onToggleSubject.onNext(getSelectedUsers());
        return selected;
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
        UserListItem user = getItem(position);
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

    public Observable<List<UserListItem>> onToggleObserver () {
        return onToggleSubject;
    }

    public Observable<Object> onLongClickObservable () {
        return onLongClickSubject;
    }

    public void setMultiSelectEnabled (boolean enabled) {
        multiSelectEnabled = enabled;
        notifyDataSetChanged();
    }

}
