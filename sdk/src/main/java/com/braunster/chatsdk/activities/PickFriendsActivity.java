package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.adapter.UsersListAdapter;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class PickFriendsActivity extends ActionBarActivity {

    public static final int PICK_FRIENDS_FOR_CONVERSATION = 3;

    private static final String TAG = PickFriendsActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    private ListView listContacts;
    private UsersListAdapter listAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_pick_friends);
        initActionBar();
        initViews();
    }

    private void initActionBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getSupportActionBar();
            ab.setTitle("Pick Friends");
        }
    }

    private void initViews() {
        listContacts = (ListView) findViewById(R.id.list_contacts);
        progressBar = (ProgressBar) findViewById(R.id.prg_bar);
        initList();
    }

    private void initList(){
        List<BUser> users ;
        BNetworkManager.getInstance().getFriendsListWithListener(new CompletionListenerWithData<List<BUser>>() {
            @Override
            public void onDone(List<BUser> u) {
                if (DEBUG) Log.d(TAG, "Contacts, Amount: " + u.size());

                progressBar.setVisibility(View.GONE);
                listContacts.setVisibility(View.VISIBLE);

                listAdapter = new UsersListAdapter(PickFriendsActivity.this, u);
                listContacts.setAdapter(listAdapter);
            }

            @Override
            public void onDoneWithError() {

            }
        });

        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (DEBUG) Log.i(TAG, "Contact Selected: " + listAdapter.getItem(position).getName()
                        + ", ID: " + listAdapter.getItem(position).getEntityID());

                BNetworkManager.getInstance().createThreadWithUsers(new CompletionListenerWithData<String>() {
                    @Override
                    public void onDone(String id) {
                        Intent intent = new Intent(PickFriendsActivity.this, ChatActivity.class);
                        intent.putExtra(ChatActivity.THREAD_ID, id);
                        startActivity(intent);
                    }

                    @Override
                    public void onDoneWithError() {
                        Toast.makeText(PickFriendsActivity.this, "Failed to start chat.", Toast.LENGTH_SHORT).show();
                    }
                }, listAdapter.getItem(position), BNetworkManager.getInstance().currentUser());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
