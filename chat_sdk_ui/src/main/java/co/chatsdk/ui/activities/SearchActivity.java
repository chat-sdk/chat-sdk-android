/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.ConnectionType;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.R;
import co.chatsdk.ui.contacts.UsersListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    private ImageView btnSearch;
    private Button btnAddContacts;
    private EditText etInput;
    private ListView listResults;
    private UsersListAdapter adapter;

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_search);

        initViews();

        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void initViews(){
        btnSearch = (ImageView) findViewById(R.id.chat_sdk_btn_search);
        btnAddContacts = (Button) findViewById(R.id.chat_sdk_btn_add_contacts);
        etInput = (EditText) findViewById(R.id.chat_sdk_et_search_input);
        listResults = (ListView) findViewById(R.id.chat_sdk_list_search_results);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
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

        adapter = new UsersListAdapter(this, true);
        listResults.setAdapter(adapter);

        // Listening to key press - if they click the ok button on the keyboard
        // we start the search
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    btnSearch.callOnClick();
                }

                return false;
            }
        });

        // Selection
        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.toggleSelection(position);
            }
        });

        btnSearch.setOnClickListener(searchOnClickListener);

        btnAddContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (adapter.getSelectedCount() == 0)
                {
                    showToast(getString(R.string.search_activity_no_contact_selected_toast));
                    return;
                }

                ArrayList<Completable> completables = new ArrayList<>();

                for (int i = 0; i < adapter.getSelectedCount(); i++) {
                    if (adapter.getSelectedUsersPositions().valueAt(i)) {
                        int pos = adapter.getSelectedUsersPositions().keyAt(i);
                        User user = adapter.getUserItems().get(pos).getUser();

                        completables.add(NM.contact().addContact(user, ConnectionType.Contact));
                    }
                }

                final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
                dialog.setMessage(getString(R.string.alert_save_contact));
                dialog.show();

                Completable.merge(completables).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                })
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        showToast(adapter.getSelectedCount() + " " + getString(R.string.search_activity_user_added_as_contact_after_count_toast));

                        if(disposable != null) {
                            disposable.dispose();
                        }

                        dialog.dismiss();
                        finish();
                    }
                });
            }
        });

    }
    
    private View.OnClickListener searchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etInput.getText().toString().isEmpty())
            {
                showToast(getString(R.string.search_activity_no_text_input_toast));
                return;
            }

            final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
            dialog.setMessage(getString(R.string.search_activity_prog_dialog_init_message));
            dialog.show();

            // Clear the list of users
            adapter.clear();

            final List<User> users = new ArrayList<>();

            NM.search().usersForIndex(Keys.Name, etInput.getText().toString()).subscribe(new Observer<User>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    disposable = d;
                }

                @Override
                public void onNext(@NonNull User user) {
                    users.add(user);
                    adapter.setUsers(users, true);

                    hideSoftKeyboard(SearchActivity.this);
                    dialog.dismiss();
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    showToast(getString(R.string.search_activity_no_user_found_toast));
                    dialog.dismiss();
                }

                @Override
                public void onComplete() {
                    dialog.dismiss();
                    if(users.size() == 0) {
                        showToast(getString(R.string.search_activity_no_user_found_toast));
                    }
                }
            });

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if(disposable != null) {
                        disposable.dispose();
                        disposable = null;
                    }
                }
            });

        }
    };
}
