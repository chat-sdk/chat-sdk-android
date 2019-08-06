/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.contacts;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.UserListItemConverter;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.main.BaseActivity;
import io.reactivex.functions.Predicate;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class SelectContactActivity extends BaseActivity {

    protected RecyclerView recyclerView;
    protected UsersListAdapter adapter;
    protected FloatingActionButton doneButton;
    protected boolean multiSelectEnabled;

    /** Set true if you want slide down animation for this context exit. */
    protected boolean animateExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            getDataFromBundle(savedInstanceState);
        } else {
            if (getIntent().getExtras() != null) {
                getDataFromBundle(getIntent().getExtras());
            }
        }

        Predicate<NetworkEvent> contactChanged = ne -> {
            // Make a filter for user update events
            return NetworkEvent.filterContactsChanged().test(ne) || NetworkEvent.filterType(EventType.UserMetaUpdated).test(ne);
        };

        // Refresh the list when the contacts change
        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(contactChanged)
                .subscribe(networkEvent -> loadData()));

        setContentView(activityLayout());

        initViews();
        initList();

        setMultiSelectEnabled(true);
    }

    protected void getDataFromBundle(Bundle bundle){
        animateExit = bundle.getBoolean(Keys.IntentKeyAnimateExit, animateExit);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.IntentKeyAnimateExit, animateExit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, R.id.action_search, 0, getString(R.string.search));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_search_white_36dp);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            ChatSDK.ui().startSearchActivity(this);
        }
        return true;
    }

    protected @LayoutRes int activityLayout() {
        return R.layout.activity_select_contacts;
    }

    protected void initViews() {
        recyclerView = findViewById(R.id.recycler_contacts);
        doneButton = findViewById(R.id.button_done);

        doneButton.setOnClickListener(v -> {
            doneButtonPressed(UserListItemConverter.toUserList(adapter.getSelectedUsers()));
        });
    }

    protected void initList() {
        adapter = new UsersListAdapter(multiSelectEnabled);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadData();

        disposableList.add(adapter.onClickSubject.subscribe(item -> {
            if (item instanceof User) {
                if (multiSelectEnabled) {
                    adapter.toggleSelection(item);
                    userSelectionChanged(getUserList());
                } else {
                    if (item instanceof User) {
                        doneButtonPressed(Arrays.asList((User) item));
                    }
                }
            }
        }));
    }

    protected List<User> getUserList () {
        return UserListItemConverter.toUserList(adapter.getSelectedUsers());
    }

    protected void loadData () {
        adapter.setUsers(new ArrayList<>(ChatSDK.contact().contacts()), true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void userSelectionChanged (List<User> users) {
        refreshDoneButtonVisibility();
    }

    abstract protected void doneButtonPressed (List<User> users);

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (animateExit) {
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
        }
    }

    public void setMultiSelectEnabled (boolean enabled) {
        multiSelectEnabled = enabled;
        refreshDoneButtonVisibility();
        adapter.setMultiSelectEnabled(enabled);
    }

    public void refreshDoneButtonVisibility () {
        boolean visible = false;
        if (multiSelectEnabled) {
            visible = adapter.getSelectedCount() > 0;
        }
        doneButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

}
