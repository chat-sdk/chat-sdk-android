/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.adapters.UsersListAdapter;
import co.chatsdk.ui.icons.Icons;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observer;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

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

    protected @LayoutRes
    int getLayout() {
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
                    search(query);
                } else {
                    adapter.clear();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                text = newText;
                if (newText.length() > 2) {
                    search(newText);
                } else {
                    adapter.clear();
                    addUserButton.setVisibility(View.GONE);
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
            dm.add(ChatSDK.core()
                    .getUserForEntityID(text)
                    .flatMapCompletable(user -> ChatSDK.contact().addContact(user, ConnectionType.Contact))
                    .subscribe(this::finish, this));
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
        item.setIcon(Icons.get(Icons.choose().search, R.color.app_bar_icon_color));

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

        dm.add(adapter.onClickObservable().subscribe(item -> adapter.toggleSelection(item)));

        fab.setOnClickListener(v -> done());

        refreshDoneButton();
        searchView.showSearch(false);

    }

    protected void search(String text) {

        final List<UserListItem> users = new ArrayList<>();
        final List<User> existingContacts = ChatSDK.contact().contacts();

        if (searchDisposable != null) {
            searchDisposable.dispose();
        }

        users.clear();

        ChatSDK.search().usersForIndex(text)
                .observeOn(AndroidSchedulers.mainThread())
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
                        addUserButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        adapter.setUsers(users, true);
                        if (users.size() == 0) {
                            showSnackbar(R.string.search_activity_no_user_found_toast, Snackbar.LENGTH_LONG);
                            if (!text.isEmpty()) {
                                addUserButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    protected void done() {

        ArrayList<Completable> completables = new ArrayList<>();

        for (UserListItem u : adapter.getSelectedUsers()) {
            if (u instanceof User && !((User) u).isMe()) {
                completables.add(ChatSDK.contact().addContact((User) u, ConnectionType.Contact));
            }
        }

        dm.add(Completable.merge(completables)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::finish, this));
    }

    public void refreshDoneButton() {
        fab.setImageDrawable(Icons.get(Icons.choose().check, R.color.white));
        fab.setVisibility(adapter.getSelectedCount() > 0 ? View.VISIBLE : View.INVISIBLE);
    }

}
