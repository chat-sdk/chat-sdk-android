package com.braunster.chatsdk.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.adapter.UsersWithStatusListAdapter;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListener;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.SearchActivity;


    public static final String MODE = "mode";

    /** Add each user found as a contact automatically.*/
    public static final String ACTION_ADD_WHEN_FOUND = "com.braunster.chatsdk.ACTION_SEARCH_AND_ADD_USERS";

    /** Request code for on activity result. For the add when found mode.
     * In the result intent there will be list of all the users entity id that were found and added.*/
    public static final int GET_CONTACTS_ADDED_REQUEST = 10;

    public static final String USER_IDS_LIST = "User_Ids_List";

    private Button btnSearch, btnAddContacts;
    private EditText etInput;
    private ListView listResults;
    private UsersWithStatusListAdapter adapter;

    private String action = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_search);

        initViews();

        initToast();

        if (getIntent().getAction() != null)
            action = getIntent().getAction();

        getActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getAction() != null)
            action = getIntent().getAction();
    }

    private void initViews(){
        btnSearch = (Button) findViewById(R.id.chat_sdk_btn_search);
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

        adapter = new UsersWithStatusListAdapter(this, !action.equals(ACTION_ADD_WHEN_FOUND));
        listResults.setAdapter(adapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etInput.getText().toString().isEmpty())
                {
                    showAlertToast("Please enter some text for the search.");
                    return;
                }

                final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
                dialog.setMessage("Fetching...");
                dialog.show();

                adapter.clear();

                BNetworkManager.sharedManager().getNetworkAdapter().usersForIndex(etInput.getText().toString(), new RepetitiveCompletionListener<BUser>() {
                    private int usersFoundCount = 0;
                    private List<String> userIds = new ArrayList<String>();

                    @Override
                    public boolean onItem(BUser item) {
                        if (DEBUG) Log.d(TAG, "User found name: " + item.getMetaName());
                        usersFoundCount++;
                        dialog.setMessage("Fetching...Found: " + usersFoundCount);

                        adapter.addRow(UsersWithStatusListAdapter.UserListItem.fromBUser(item));

                        if (action.equals(ACTION_ADD_WHEN_FOUND))
                        {
                            BNetworkManager.sharedManager().getNetworkAdapter().currentUser().addContact(item);
                            userIds.add(item.getEntityID());
                        }

                        return false;
                    }

                    @Override
                    public void onDone() {
                        dialog.dismiss();

                        if (usersFoundCount == 0)
                        {
                            showAlertToast("No match found.");
                            return;
                        }
                        if (action.equals(ACTION_ADD_WHEN_FOUND))
                        {

                            Intent resultIntent = new Intent();
                            if (usersFoundCount == 0)
                            {
                                setResult(RESULT_CANCELED, resultIntent);
                                finish();
                            }

                            String ids[] = userIds.toArray(new String[userIds.size()]);
                            Bundle extras = new Bundle();
                            extras.putStringArray(USER_IDS_LIST, ids);
                            resultIntent.putExtras(extras);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }

                        hideSoftKeyboard(SearchActivity.this);
                    }

                    @Override
                    public void onItemError(Object object) {

                    }
                });
            }
        });

/*        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Adding the picked user as a contact to the current user.
                BNetworkManager.sharedManager().getNetworkAdapter().currentUser().addContact(adapter.getItem(position).asBUser());
                createAndOpenThreadWithUsers(adapter.getItem(position).getText(),
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser(), adapter.getItem(position).asBUser());
            }
        });*/

        btnAddContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (action.equals(ACTION_ADD_WHEN_FOUND))
                    return;

                if (adapter.getSelectedCount() == 0)
                {
                    showAlertToast("No contacts were selected.");
                    return;
                }

                BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
                String[] entitiesIDs = new String[adapter.getSelectedCount()];
                BUser user;
                for (int i = 0; i < adapter.getSelectedCount(); i++) {
                    int pos = -1;
                    if (adapter.getSelectedUsersPositions().valueAt(i))
                        pos = adapter.getSelectedUsersPositions().keyAt(i);

                    user = adapter.getListData().get(pos).asBUser();
                    currentUser.addContact(user);
                    entitiesIDs[i] = user.getEntityID();
                }

                showToast(adapter.getSelectedCount() + " Users were added as contacts.");

                Intent intent = new Intent(MainActivity.Action_Contacts_Added);
                intent.putExtra(USER_IDS_LIST, entitiesIDs);
                sendBroadcast(intent);

                finish();
            }
        });
    }
}
