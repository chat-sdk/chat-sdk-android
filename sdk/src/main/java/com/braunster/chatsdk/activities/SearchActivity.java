package com.braunster.chatsdk.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.adapter.UsersListAdapter;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListener;
import com.braunster.chatsdk.network.BNetworkManager;

/**
 * Created by braunster on 29/06/14.
 */
public class SearchActivity extends BaseActivity {

    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private Button btnSearch;
    private EditText etInput;
    private ListView listResults;
    private UsersListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_search);

        initViews();
    }

    private void initViews(){
        btnSearch = (Button) findViewById(R.id.chat_sdk_btn_search);
        etInput = (EditText) findViewById(R.id.chat_sdk_et_search_input);
        listResults = (ListView) findViewById(R.id.chat_sdk_list_search_results);
        adapter = new UsersListAdapter(this);
        listResults.setAdapter(adapter);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

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

                    @Override
                    public boolean onItem(BUser item) {
                        if (DEBUG) Log.d(TAG, "User found name: " + item.getMetaName());
                        usersFoundCount++;
                        dialog.setMessage("Fetching...Found: " + usersFoundCount);
                        adapter.addRow(item);
                        return false;
                    }

                    @Override
                    public void onDone() {
                        dialog.dismiss();

                        if (usersFoundCount == 0)
                            Toast.makeText(SearchActivity.this, "No match found.", Toast.LENGTH_SHORT).show();
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
                BNetworkManager.sharedManager().getNetworkAdapter().currentUser().addContact(adapter.getItem(position));
                createAndOpenThreadWithUsers(adapter.getItem(position).getMetaName(),
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser(), adapter.getItem(position));
            }
        });
    }
}
