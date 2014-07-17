package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.adapter.UsersWithStatusListAdapter;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class PickFriendsActivity extends BaseActivity {

    public static final int PICK_FRIENDS_FOR_CONVERSATION = 3;

    private static final String TAG = PickFriendsActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    private ListView listContacts;
    private UsersWithStatusListAdapter listAdapter;
    private Button btnGetFBFriends, btnStartChat;
    private EditText etSearch;

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
        listContacts = (ListView) findViewById(R.id.chat_sdk_list_contacts);
        btnGetFBFriends = (Button) findViewById(R.id.chat_sdk_btn_invite_from_fb);
        etSearch = (EditText) findViewById(R.id.chat_sdk_et_search);
        btnStartChat = (Button) findViewById(R.id.chat_sdk_btn_start_chat);
        initList();
    }

    private void initList(){
        final List<BUser> list = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getContacts();
        listAdapter = new UsersWithStatusListAdapter(PickFriendsActivity.this, UsersWithStatusListAdapter.makeList(list, false, true), true);
        listContacts.setAdapter(listAdapter);

        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BUser[] users = new BUser[listAdapter.getSelectedCount() + 1];

                for (int i = 0 ; i < listAdapter.getSelectedCount() ; i++)
                {
                    int pos = -1;
                    if (listAdapter.getSelectedUsersPositions().valueAt(i))
                        pos = listAdapter.getSelectedUsersPositions().keyAt(i);

                    users[i] = listAdapter.getListData().get(pos).asBUser();
                }

                users[users.length - 1] = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

                createAndOpenThreadWithUsers("", users);
            }
        });
      /*  listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (DEBUG) Log.i(TAG, "Contact Selected: " + listAdapter.getItem(position).getText()
                        + ", ID: " + listAdapter.getItem(position).getEntityID());

                createAndOpenThreadWithUsers(listAdapter.getItem(position).getText(),
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser(), listAdapter.getItem(position).asBUser());
            }
        });*/
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        etSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PickFriendsActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

/*        String idPrefix = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getAuthenticationId().substring(0, 2);
        Log.d(TAG, "Prefix: " + idPrefix);
        if (BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getAuthenticationId().substring(0, 2).equals("fb"))
            btnGetFBFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                    DialogUtils.ChatSDKFacebookFriendsDialog dialog = DialogUtils.ChatSDKFacebookFriendsDialog.getInstance();
                    dialog.setFinishedListener(new DialogUtils.DialogInterface<List<GraphUser>>() {
                        @Override
                        public void onFinished(List<GraphUser> graphUsers) {

                        }
                    });
                    dialog.show(fm, "FB_Friends_List");
                }
            });
        else Toast.makeText(this, "You need to login from facebook to use this feature.", Toast.LENGTH_SHORT).show();*/
    }
}
