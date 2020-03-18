/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.adapters;


import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.ui.R;
import co.chatsdk.ui.view_holders.UserViewHolder;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import co.chatsdk.core.dao.User;

public class UsersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<UserListItem> items = new ArrayList<>();

    protected SparseBooleanArray selectedUsersPositions = new SparseBooleanArray();

    protected boolean multiSelectEnabled;
    protected final PublishSubject<UserListItem> onClickSubject = PublishSubject.create();
    protected final PublishSubject<UserListItem> onLongClickSubject = PublishSubject.create();
    protected final PublishSubject<List<UserListItem>> onToggleSubject = PublishSubject.create();

    public interface SubtitleProvider {
        String subtitle(UserListItem user);
    }

    protected SubtitleProvider subtitleProvider = null;

    public UsersListAdapter() {
        this(null, false, null);
    }

    public UsersListAdapter(boolean multiSelectEnabled) {
        this(null, multiSelectEnabled, null);
    }

    public UsersListAdapter(List<UserListItem> users, boolean multiSelect, SubtitleProvider subtitleProvider) {
        if (users == null) {
            users = new ArrayList<>();
        }
        this.subtitleProvider = subtitleProvider;

        setUsers(users);

        this.multiSelectEnabled = multiSelect;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_user_row, parent, false);
        return new UserViewHolder(view, multiSelectEnabled, subtitleProvider);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        UserViewHolder viewHolder = (UserViewHolder) holder;
        UserListItem item = items.get(position);

        viewHolder.bind(item);

        if (multiSelectEnabled) {
            viewHolder.setChecked(selectedUsersPositions.get(position));
        }

        viewHolder.itemView.setOnClickListener(view -> onClickSubject.onNext(item));
        viewHolder.itemView.setOnLongClickListener(view -> {
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

    public void addUser(UserListItem user) {
        addUser(user, false);
    }

    public List<UserListItem> getItems() {
        return items;
    }

    public void addUser(UserListItem user, boolean notify) {
        addUser(user, -1, notify);
    }

    public void addUser(UserListItem user, int atIndex, boolean notify) {
        if (!items.contains(user)) {
            if (atIndex >= 0) {
                items.add(atIndex, user);
            } else {
                items.add(user);
            }
            if (notify) {
                notifyDataSetChanged();
            }
        }
    }

    public List<UserListItem> getSelectedUsers() {
        List<UserListItem> users = new ArrayList<>();
        for (int i = 0; i < getSelectedCount(); i++) {
            int pos = getSelectedUsersPositions().keyAt(i);
            users.add((items.get(pos)));
        }
        return users;
    }

    public void setUsers(List<UserListItem> userItems) {
        setUsers(userItems, false);
    }

    /**
     * Clear the list.
     * <p>
     * Calls notifyDataSetChanged.
     * *
     */
    public void clear() {
        items.clear();
        clearSelection();
        notifyDataSetChanged();
    }

    /**
     * Sorting a given list using the internal comparator.
     * <p>
     * This will be used each time after setting the user item
     * *
     */
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
     *
     * @param position the position in the list of the item that need to be toggled
     *                 <p>
     *                 notifyDataSetChanged will be called.
     *                 *
     */
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
     *
     * @param position the position in the list of the item that need to be toggled.
     * @param selected pass true for selecting the view, false will remove the view from the selectedUsersPositions
     *                 *
     */
    public boolean setViewSelected(int position, boolean selected) {
        UserListItem user = getItem(position);
        if (user != null) {
            if (selected) {
                selectedUsersPositions.put(position, true);
            } else {
                selectedUsersPositions.delete(position);
            }
            // Is this needed?
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
     * * *
     */
    public int getSelectedCount() {
        return selectedUsersPositions.size();
    }

    /**
     * Select all users
     * <p>
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
     * <p>
     * notifyDataSetChanged will be called.
     */
    public void clearSelection() {
        selectedUsersPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public Observable<UserListItem> onClickObservable() {
        return onClickSubject;
    }

    public Observable<List<UserListItem>> onToggleObserver() {
        return onToggleSubject;
    }

    public Observable<UserListItem> onLongClickObservable() {
        return onLongClickSubject;
    }

    public void setMultiSelectEnabled(boolean enabled) {
        multiSelectEnabled = enabled;
        notifyDataSetChanged();
    }

}
