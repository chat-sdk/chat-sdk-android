/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.icons.Icons;
import sdk.guru.common.RX;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    protected UsersListAdapter adapter;
    protected Disposable searchDisposable;

    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.addUserButton) protected Button addUserButton;
    @BindView(R2.id.recyclerView) protected RecyclerView recyclerView;
    @BindView(R2.id.fab) protected FloatingActionButton fab;
    @BindView(R2.id.searchView) protected MaterialSearchView searchView;
    @BindView(R2.id.root) protected FrameLayout root;

    protected String text = "";

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_search;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    protected void initViews() {
        super.initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        setActionBarTitle(R.string.search);

        adapter = new UsersListAdapter(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_TOUCH_SCROLL) {
                    fab.setVisibility(View.INVISIBLE);
                } else {
                    refreshDoneButton();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        dm.add(adapter.onToggleObserver().subscribe(userListItem -> refreshDoneButton()));

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    text = query.trim();
                    search(query.trim());
                } else {
                    adapter.clear();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                addUserButton.setVisibility(View.GONE);
                if (newText.length() > 2) {
                    text = newText.trim();
                    search(newText.trim());
                } else {
                    adapter.clear();
                }
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                finish();
            }
        });

        addUserButton.setOnClickListener(v -> {
            addUserButton.setEnabled(false);
            dm.add(ChatSDK.core()
                    .getUserForEntityID(text)
                    .flatMapCompletable(user -> ChatSDK.contact().addContact(user, ConnectionType.Contact))
                    .subscribe(this::finish, error -> {
                        addUserButton.setEnabled(true);
                        Logger.debug("Errr");
                    }));
        });

        adapter.onToggleObserver().doOnNext(userListItems -> {
            refreshDoneButton();
        }).ignoreElements().subscribe(this);

        fab.setOnClickListener(v -> {
            fab.setEnabled(false);
            done();
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_search_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        item.setIcon(Icons.get(this, Icons.choose().search, Icons.shared().actionBarIconColor));

        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDoneButton();
        searchView.showSearch(false);
        addUserButton.setEnabled(true);
        fab.setEnabled(true);

    }

    protected void search(String searchText) {

        final List<UserListItem> users = new ArrayList<>();
        final List<User> existingContacts = ChatSDK.contact().contacts();

        if (searchDisposable != null) {
            searchDisposable.dispose();
        }

        users.clear();

        ChatSDK.search().usersForIndex(searchText)
                .observeOn(RX.main())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        searchDisposable = d;
                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        if (!existingContacts.contains(user) && !user.isMe()) {
                            users.add(user);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        showSnackbar(e.getLocalizedMessage());
                        showAddUserButton();
                    }

                    @Override
                    public void onComplete() {
                        adapter.setUsers(users, true);
                        if (users.size() == 0) {
                            showSnackbar(R.string.search_activity_no_user_found_toast, Snackbar.LENGTH_LONG);
                            if (!searchText.isEmpty()) {
                                showAddUserButton();
                            }
                        }
                    }
                });
    }

    public void showAddUserButton() {
        if (ChatSDK.search().canAddUserById()) {
            addUserButton.setVisibility(View.VISIBLE);
        }
    }

    protected void done() {

        ArrayList<Completable> completables = new ArrayList<>();

        for (User u : User.convertIfPossible(adapter.getSelectedUsers())) {
            if (!u.isMe()) {
                completables.add(ChatSDK.contact().addContact(u, ConnectionType.Contact));
            }
        }

        dm.add(Completable.merge(completables)
                .observeOn(RX.main())
                .subscribe(this::finish, this));
    }

    public void refreshDoneButton() {
        fab.setImageDrawable(Icons.get(this, Icons.choose().check, R.color.white));
        fab.setVisibility(adapter.getSelectedCount() > 0 ? View.VISIBLE : View.INVISIBLE);
    }

}
