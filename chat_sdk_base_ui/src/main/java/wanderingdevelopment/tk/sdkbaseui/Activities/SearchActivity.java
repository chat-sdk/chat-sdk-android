/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package wanderingdevelopment.tk.sdkbaseui.Activities;

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

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.DaoDefines;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import wanderingdevelopment.tk.sdkbaseui.R;
import co.chatsdk.core.defines.Debug;
import wanderingdevelopment.tk.sdkbaseui.Adapters.UsersListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.SearchActivity;

    /** Request code for on activity result. For the add when found mode.
     * In the result intent there will be list of all the users entity id that were found and added.*/
    public static final int GET_CONTACTS_ADDED_REQUEST = 10;

    public static final String USER_IDS_LIST = "User_Ids_List";

    private ImageView btnSearch;
    private Button btnAddContacts;
    private EditText etInput;
    private ListView listResults;
    private UsersListAdapter adapter;
    private CheckBox chSelectAll;

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
        chSelectAll = (CheckBox) findViewById(R.id.chat_sdk_chk_select_all);

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

                BUser currentUser = NM.currentUser();
                String[] entitiesIDs = new String[adapter.getSelectedCount()];
                BUser user;
                for (int i = 0; i < adapter.getSelectedCount(); i++) {
                    int pos = -1;
                    if (adapter.getSelectedUsersPositions().valueAt(i))
                        pos = adapter.getSelectedUsersPositions().keyAt(i);

                    user = adapter.getUserItems().get(pos).asBUser();
                    currentUser.addContact(user);
                    entitiesIDs[i] = user.getEntityID();
                }

                DaoCore.updateEntity(currentUser);

                showToast(adapter.getSelectedCount() + " " + getString(R.string.search_activity_user_added_as_contact_after_count_toast));

                Intent intent = new Intent(MainActivity.Action_Contacts_Added);
                intent.putExtra(USER_IDS_LIST, entitiesIDs);
                sendBroadcast(intent);

                if(disposable != null) {
                    disposable.dispose();
                }

                finish();
            }
        });

        chSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    adapter.selectAll();
                else adapter.clearSelection();
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

            adapter.clear();

            final List<String> userIds = new ArrayList<>();
            final List<BUser> users = new ArrayList<>();

            disposable = NM.search().usersForIndex(DaoDefines.Keys.Name, etInput.getText().toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(new Consumer<BUser>() {
                        @Override
                        public void accept(BUser u) throws Exception {
                            NM.currentUser().addContact(u);
                            userIds.add(u.getEntityID());
                            users.add(u);
                            adapter.setBUserItems(users, true);
                            chSelectAll.setEnabled(true);

                            hideSoftKeyboard(SearchActivity.this);

                            dialog.dismiss();
                        }
                    }).doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            showToast(getString(R.string.search_activity_no_user_found_toast));
                            dialog.dismiss();

                            chSelectAll.setEnabled(false);

                        }
                    }).subscribe();

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    disposable.dispose();
                }
            });

        }
    };
}
