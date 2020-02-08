/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import butterknife.BindView;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.adapters.UsersListAdapter;
import co.chatsdk.ui.databinding.ActivitySearchBinding;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    protected UsersListAdapter adapter;
    protected Disposable searchDisposable;

    protected ActivitySearchBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());
        initViews();
    }

    protected void initViews() {
        super.initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        setActionBarTitle(R.string.search);

        adapter = new UsersListAdapter(true);

        b.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerView.setAdapter(adapter);
        b.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_TOUCH_SCROLL) {
                    b.floatingActionButton.setVisibility(View.INVISIBLE);
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

        b.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    search(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    search(newText);
                }
                return false;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    protected @LayoutRes int getLayout() {
        return R.layout.activity_search;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_search_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        b.searchView.setMenuItem(item);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        dm.add(adapter.onClickObservable().subscribe(item -> adapter.toggleSelection(item)));

        b.floatingActionButton.setOnClickListener(v -> done());

        refreshDoneButton();
        b.searchView.showSearch(false);
    }

    protected void search (String text) {
        // Clear the list of users

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
                    }

                    @Override
                    public void onComplete() {
                        adapter.setUsers(users, true);
                        if (users.size() == 0) {
                            showSnackbar(R.string.search_activity_no_user_found_toast, Snackbar.LENGTH_LONG);
                        }
                    }
                });

    }

    protected void done () {

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

    public void refreshDoneButton () {
        b.floatingActionButton.setImageResource(R.drawable.ic_check_white_48dp);
        b.floatingActionButton.setVisibility(adapter.getSelectedCount() > 0 ? View.VISIBLE : View.INVISIBLE);
    }

}
