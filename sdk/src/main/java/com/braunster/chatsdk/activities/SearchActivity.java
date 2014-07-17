package com.braunster.chatsdk.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
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
    private static final boolean DEBUG = true;


    public static final String MODE = "mode";

    /** Add each user found as a contact automatically.*/
    public static final String ACTION_ADD_WHEN_FOUND = "com.braunster.chatsdk.ACTION_SEARCH_AND_ADD_USERS";

    /** Request code for on activity result. For the add when found mode.
     * In the result intent there will be list of all the users entity id that were found and added.*/
    public static final int GET_CONTACTS_ADDED_REQUEST = 10;

    public static final String USER_IDS_LIST = "User_Ids_List";

    private Button btnSearch;
    private EditText etInput;
    private ListView listResults;
    private UsersWithStatusListAdapter adapter;

    private String action = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_search);

        initViews();
        if (getIntent().getAction() != null)
            action = getIntent().getAction();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getAction() != null)
            action = getIntent().getAction();
    }

    private void initViews(){
        btnSearch = (Button) findViewById(R.id.chat_sdk_btn_search);
        etInput = (EditText) findViewById(R.id.chat_sdk_et_search_input);
        listResults = (ListView) findViewById(R.id.chat_sdk_list_search_results);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter = new UsersWithStatusListAdapter(this);
        listResults.setAdapter(adapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etInput.getText().toString().isEmpty())
                {
                    Toast.makeText(SearchActivity.this, "Please enter some text for the search.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(SearchActivity.this, "No match found.", Toast.LENGTH_SHORT).show();

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

                    }

                    @Override
                    public void onItemError(Object object) {

                    }
                });
            }
        });

        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Adding the picked user as a contact to the current user.
                BNetworkManager.sharedManager().getNetworkAdapter().currentUser().addContact(adapter.getItem(position).asBUser());
                createAndOpenThreadWithUsers(adapter.getItem(position).getText(),
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser(), adapter.getItem(position).asBUser());
            }
        });
    }
}
