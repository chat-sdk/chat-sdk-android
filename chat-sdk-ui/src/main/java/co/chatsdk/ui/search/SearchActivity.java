/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.search;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.types.SearchActivityType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.contacts.UsersListAdapter;
import co.chatsdk.ui.main.BaseActivity;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    protected FloatingActionButton floatingActionButton;
    protected TextInputEditText searchEditText;
    protected RecyclerView recyclerView;
    protected UsersListAdapter adapter;
    protected Disposable searchDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activityLayout());

        initViews();

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
                    floatingActionButton.setVisibility(View.INVISIBLE);
                } else {
                    refreshDoneButton();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        disposableList.add(adapter.onToggleObserver().subscribe(userListItem -> refreshDoneButton()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    protected @LayoutRes int activityLayout() {
        return R.layout.activity_search;
    }

    protected void initViews() {
        floatingActionButton = findViewById(R.id.floating_action_button);
        searchEditText = findViewById(R.id.search_text_input_edit_text);
        recyclerView = findViewById(R.id.result_list_recycler_view);
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


        // Listening to key press - if they click the ok button on the keyboard
        // we start the search
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search();
                return false;
            }
        });
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!searchText().isEmpty()) {
                    search();
                }
            }
            return false;
        });

        disposableList.add(adapter.onClickObservable().subscribe(item -> adapter.toggleSelection(item)));

        floatingActionButton.setOnClickListener(v -> done());

        refreshDoneButton();
    }

    protected void search () {
        final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
        dialog.setMessage(getString(R.string.search_activity_prog_dialog_init_message));
        dialog.show();

        // Clear the list of users
        adapter.clear();

        final List<UserListItem> users = new ArrayList<>();
        final List<User> existingContacts = ChatSDK.contact().contacts();

        ChatSDK.search().usersForIndex(searchText())
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
                            adapter.setUsers(users, true);
                            hideKeyboard();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        showToast(e.getLocalizedMessage());
                        dialog.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        dialog.dismiss();
                        if (users.size() == 0) {
                            showToast(getString(R.string.search_activity_no_user_found_toast));
                        }
                    }
                });

        dialog.setOnCancelListener(dialog1 -> {
            if (searchDisposable != null) {
                searchDisposable.dispose();
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

        showProgressDialog(R.string.alert_save_contact);

        disposableList.add(Completable.merge(completables)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dismissProgressDialog)
                .subscribe(this::finish, toastOnErrorConsumer()));
    }

    public void refreshDoneButton () {
        floatingActionButton.setImageResource(R.drawable.ic_check_white_48dp);
        floatingActionButton.setVisibility(adapter.getSelectedCount() > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    public String searchText () {
        if (searchEditText.getText() != null) {
            return searchEditText.getText().toString();
        }
        return "";
    }

}
