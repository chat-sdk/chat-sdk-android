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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.types.SearchActivityType;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.R;
import co.chatsdk.ui.contacts.UsersListAdapter;
import co.chatsdk.ui.main.BaseActivity;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    protected ImageView searchImageView;
    protected Button addContactsButton;
    protected EditText searchTextView;
    protected RecyclerView recyclerView;
    protected UsersListAdapter adapter;

    protected DisposableList disposableList = new DisposableList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activityLayout());

        initViews();

        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    protected @LayoutRes int activityLayout() {
        return R.layout.chat_sdk_activity_search;
    }

    protected void initViews() {
        searchImageView = findViewById(R.id.chat_sdk_btn_search);
        addContactsButton = findViewById(R.id.chat_sdk_btn_add_contacts);
        searchTextView = findViewById(R.id.chat_sdk_et_search_input);
        recyclerView = findViewById(R.id.chat_sdk_list_search_results);
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

        adapter = new UsersListAdapter(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Listening to key press - if they click the ok button on the keyboard
        // we start the search
        searchTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchImageView.callOnClick();
            }

            return false;
        });

        adapter.getItemClicks().subscribe(item -> adapter.toggleSelection(item));

        searchImageView.setOnClickListener(searchOnClickListener);

        addContactsButton.setOnClickListener(v -> {

            if (adapter.getSelectedCount() == 0)
            {
                showToast(getString(R.string.search_activity_no_contact_selected_toast));
                return;
            }

            ArrayList<Completable> completables = new ArrayList<>();

            for (UserListItem u : adapter.getSelectedUsers()) {
                if (u instanceof User && !((User) u).isMe()) {
                    completables.add(ChatSDK.contact().addContact((User) u, ConnectionType.Contact));
                }
            }

            final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
            dialog.setMessage(getString(R.string.alert_save_contact));
            dialog.show();

            Completable.merge(completables)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        showToast(adapter.getSelectedCount() + " " + getString(R.string.search_activity_user_added_as_contact_after_count_toast));

                        disposableList.dispose();

                        dialog.dismiss();
                        finish();
                    }, throwable -> {
                        showToast(throwable.getLocalizedMessage());
                        dialog.dismiss();
                    });
        });

    }
    
    protected View.OnClickListener searchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (searchTextView.getText().toString().isEmpty())
            {
                showToast(getString(R.string.search_activity_no_text_input_toast));
                return;
            }

            final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
            dialog.setMessage(getString(R.string.search_activity_prog_dialog_init_message));
            dialog.show();

            // Clear the list of users
            adapter.clear();

            final List<UserListItem> users = new ArrayList<>();

            final List<User> existingContacts = ChatSDK.contact().contacts();

            ChatSDK.search().usersForIndex(searchTextView.getText().toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<User>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    disposableList.add(d);
                }

                @Override
                public void onNext(@NonNull User user) {

                    if (!existingContacts.contains(user) && !user.isMe()) {
                        users.add(user);
                        adapter.setUsers(users, true);
                        hideSoftKeyboard(SearchActivity.this);
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

            dialog.setOnCancelListener(dialog1 -> disposableList.dispose());

        }
    };

    public static void startSearchActivity (final Context context) {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            final List<SearchActivityType> activities = new ArrayList<>(ChatSDK.ui().getSearchActivities());
            activities.add(new SearchActivityType(ChatSDK.ui().getSearchActivity(), context.getString(R.string.search_with_name)));

            if (activities.size() == 1) {
                ChatSDK.ui().startActivity(context, activities.get(0).className);
                return;
            }

            String [] items = new String [activities.size()];
            int i = 0;

            for (SearchActivityType activity : activities) {
                items[i++] = activity.title;
            }

            builder.setTitle(context.getString(R.string.search)).setItems(items, (dialogInterface, i1) -> {
                // Launch the appropriate context
                ChatSDK.ui().startActivity(context, activities.get(i1).className);
            });

            builder.show();
        }
    }
}
