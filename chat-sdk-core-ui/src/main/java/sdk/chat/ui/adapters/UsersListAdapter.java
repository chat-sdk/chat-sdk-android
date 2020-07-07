/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.adapters;


import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.ui.R;
import sdk.chat.ui.view_holders.UserViewHolder;

public class UsersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<UserListItem> items = new ArrayList<>();

    protected SparseBooleanArray selectedUsersPositions = new SparseBooleanArray();

    protected boolean multiSelectEnabled;
    protected final PublishRelay<UserListItem> onClickRelay = PublishRelay.create();
    protected final PublishRelay<UserListItem> onLongClickRelay = PublishRelay.create();
    protected final PublishRelay<List<UserListItem>> onToggleRelay = PublishRelay.create();

    public interface SubtitleProvider {
        String subtitle(UserListItem user);
    }

    protected SubtitleProvider subtitleProvider;

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
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_user_row, parent, false);
        return new UserViewHolder(view, multiSelectEnabled, subtitleProvider);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        UserViewHolder viewHolder = (UserViewHolder) holder;
        UserListItem item = items.get(position);

        viewHolder.bind(item);

        if (multiSelectEnabled) {
            if (!selectedUsersPositions.get(position)) {
                viewHolder.getCheckbox().setChecked(false);
            }

            viewHolder.getCheckbox().setOnClickListener(v -> {
                if (viewHolder.getCheckbox().isChecked()) {
                    selectedUsersPositions.put(position, true);
                } else {
                    selectedUsersPositions.delete(position);
                }
                onToggleRelay.accept(getSelectedUsers());
            });
        }

        viewHolder.itemView.setOnClickListener(view -> {
            if (!multiSelectEnabled) {
                onClickRelay.accept(item);
            }
        });
        viewHolder.itemView.setOnLongClickListener(view -> {
            if (!multiSelectEnabled) {
                onLongClickRelay.accept(item);
            }
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
        Comparator<UserListItem> comparator = (u1, u2) -> {
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
     * Clear the selection of all users.
     * <p>
     * notifyDataSetChanged will be called.
     */
    public void clearSelection() {
        selectedUsersPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public Observable<UserListItem> onClickObservable() {
        return onClickRelay;
    }

    public Observable<List<UserListItem>> onToggleObserver() {
        return onToggleRelay;
    }

    public Observable<UserListItem> onLongClickObservable() {
        return onLongClickRelay;
    }

    public void setMultiSelectEnabled(boolean enabled) {
        multiSelectEnabled = enabled;
        notifyDataSetChanged();
    }

}
