package com.braunster.chatsdk.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.adapter.PagerAdapterTabs;
import com.braunster.chatsdk.adapter.UsersWithStatusListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class PickFriendsActivity extends BaseActivity {

    public static final int MODE_NEW_CONVERSATION = 1991;
    public static final int MODE_ADD_TO_CONVERSATION = 1992;

    public static final String MODE= "mode";
    public static final String THREAD_ID = "thread_id";
    public static final String ANIMATE_EXIT = "animate_exit";

    private static final String TAG = PickFriendsActivity.class.getSimpleName();
    private static boolean DEBUG = Debug.PickFriendsActivity;

    private ListView listContacts;
    private UsersWithStatusListAdapter listAdapter;
    private Button btnGetFBFriends, btnStartChat;
    private EditText etSearch;
    private CheckBox chSelectAll;

    /** Default value - MODE_NEW_CONVERSATION*/
    private int mode = MODE_NEW_CONVERSATION;

    /** For add to conversation mode.*/
    private long threadID = -1;
    /** For add to conversation mode.*/
    private BThread thread;

    private boolean animateExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_pick_friends);
        initActionBar();


        if (savedInstanceState != null)
        {
            getDateFromBundle(savedInstanceState);
        }
        else
        {
            if (getIntent().getExtras() != null)
            {
                getDateFromBundle(getIntent().getExtras());
            }
        }

        initToast();
        initViews();
    }

    private void getDateFromBundle(Bundle bundle){
        mode = bundle.getInt(MODE, mode);
        threadID = bundle.getLong(THREAD_ID, threadID);
        animateExit = bundle.getBoolean(ANIMATE_EXIT, animateExit);
    }

    private void initActionBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getSupportActionBar();
            ab.setTitle("Pick Friends");
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(THREAD_ID, threadID);
        outState.putInt(MODE, mode);
        outState.putBoolean(ANIMATE_EXIT, animateExit);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

    private void initViews() {
        listContacts = (ListView) findViewById(R.id.chat_sdk_list_contacts);
        btnGetFBFriends = (Button) findViewById(R.id.chat_sdk_btn_invite_from_fb);
        etSearch = (EditText) findViewById(R.id.chat_sdk_et_search);
        btnStartChat = (Button) findViewById(R.id.chat_sdk_btn_add_contacts);
        chSelectAll = (CheckBox) findViewById(R.id.chat_sdk_chk_select_all);

        if (mode == MODE_ADD_TO_CONVERSATION)
            btnStartChat.setText(getResources().getString(R.string.add_users));
    }

    private void initList(){
        final List<BUser> list = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getContacts();

        // Removing the users that is already inside the thread.
        if (mode == MODE_ADD_TO_CONVERSATION && threadID != -1){
            thread = DaoCore.fetchEntityWithProperty(BThread.class, BThreadDao.Properties.Id, threadID);
            List<BUser> threadUser = thread.getUsers();

            for (BUser u : threadUser)
                list.remove(u);
        }

        if (list.size() > 0)
            chSelectAll.setEnabled(true);

        listAdapter = new UsersWithStatusListAdapter(PickFriendsActivity.this, UsersWithStatusListAdapter.makeList(list, false, true), true);
        listContacts.setAdapter(listAdapter);

        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (DEBUG) Log.d(TAG, "selected count: " + listAdapter.getSelectedCount());

                int addCurrent = 1;
                if (mode == MODE_ADD_TO_CONVERSATION)
                {
                    showProgDialog("Adding users to thread...");
                    addCurrent = 0;
                }
                else if (mode == MODE_NEW_CONVERSATION)
                {
                    showProgDialog("Creating thread...");
                    addCurrent = 1;
                }

                BUser[] users = new BUser[listAdapter.getSelectedCount() + addCurrent];

                for (int i = 0 ; i < listAdapter.getSelectedCount() ; i++)
                {
                    int pos = -1;
                    if (listAdapter.getSelectedUsersPositions().valueAt(i))
                        pos = listAdapter.getSelectedUsersPositions().keyAt(i);

                    users[i] = listAdapter.getListData().get(pos).asBUser();

                    Log.d(TAG, "Selected User[" + i + "]: " + users[i].getMetaName());
                }

                if (mode == MODE_NEW_CONVERSATION)
                {
                    users[users.length - 1] = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
                    createAndOpenThreadWithUsers("", users);
                }
                else if (mode == MODE_ADD_TO_CONVERSATION){


                    BNetworkManager.sharedManager().getNetworkAdapter().addUsersToThread(thread, new RepetitiveCompletionListenerWithError<BUser, Object>() {
                        @Override
                        public boolean onItem(BUser user) {
                            return false;
                        }

                        @Override
                        public void onDone() {

                            // Updating the ui.
                            Intent intent = new Intent(MainActivity.Action_Refresh_Fragment);
                            if (thread.getType() == BThread.Type.Public)
                                intent.putExtra(MainActivity.PAGE_ADAPTER_POS, PagerAdapterTabs.ChatRooms);
                            else intent.putExtra(MainActivity.PAGE_ADAPTER_POS, PagerAdapterTabs.Conversations);
                            sendBroadcast(intent);

                            dismissProgDialog();

                            setResult(Activity.RESULT_OK);
                            finish();

                            if (animateExit)
                                overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
                        }

                        @Override
                        public void onItemError(BUser user, Object o) {
                            dismissProgDialog();
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                    }, users );
                }
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

        initList();

        etSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PickFriendsActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        chSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    listAdapter.selectAll();
                }
                else listAdapter.clearSelection();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (animateExit)
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
    }
}
