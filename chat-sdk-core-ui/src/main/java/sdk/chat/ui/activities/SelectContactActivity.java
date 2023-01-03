/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import io.reactivex.functions.Predicate;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.provider.MenuItemProvider;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class SelectContactActivity extends BaseActivity {

    protected UsersListAdapter adapter;
    protected boolean multiSelectEnabled;

    protected Toolbar toolbar;
    protected RecyclerView recyclerView;
    protected FloatingActionButton fab;
    protected ConstraintLayout root;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_select_contacts;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Predicate<NetworkEvent> contactChanged = ne -> {
            // Make a filter for user update events
            return NetworkEvent.filterContactsChanged().test(ne) || NetworkEvent.filterType(EventType.UserMetaUpdated).test(ne);
        };

        // Refresh the list when the contacts change
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(contactChanged)
                .subscribe(networkEvent -> loadData()));

        initViews();
        initList();

        setMultiSelectEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ChatSDKUI.provider().menuItems().addSearchItem(this, menu, 0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MenuItemProvider.searchItemId) {
            ChatSDK.ui().startSearchActivity(this);
        }
        return true;
    }

    protected void initViews() {
        super.initViews();

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        root = findViewById(R.id.root);

        fab.setOnClickListener(v -> {
            fab.setEnabled(false);
            doneButtonPressed(adapter.getSelectedUsers());
        });
        fab.setImageDrawable(ChatSDKUI.icons().get(this, ChatSDKUI.icons().check, R.color.fab_icon_color));
    }

    protected void initList() {
        adapter = new UsersListAdapter(multiSelectEnabled);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setEnabled(true);
    }

    protected void loadData() {
        adapter.setUsers(new ArrayList<>(ChatSDK.contact().contacts()), true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    abstract protected void doneButtonPressed(List<UserListItem> users);

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setMultiSelectEnabled(boolean enabled) {
        multiSelectEnabled = enabled;
        refreshDoneButtonVisibility();
        adapter.setMultiSelectEnabled(enabled);
        if(enabled) {
            dm.add(adapter.onToggleObserver().subscribe(items -> {
                refreshDoneButtonVisibility();
            }));
        } else {
            dm.add(adapter.onClickObservable().subscribe(item -> {
                if (!multiSelectEnabled) {
                    doneButtonPressed(Arrays.asList(item));
                } else {
                    refreshDoneButtonVisibility();
                }
            }));
        }
    }

    public void refreshDoneButtonVisibility() {
        boolean visible = false;
        if (multiSelectEnabled) {
            visible = adapter.getSelectedCount() > 0;
        }
        fab.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

}
