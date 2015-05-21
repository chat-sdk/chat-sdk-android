/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractChatActivity;
import com.braunster.chatsdk.adapter.ChatSDKUsersListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.List;

import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKPickFriendsActivity extends ChatSDKBaseActivity {

    public static final int MODE_NEW_CONVERSATION = 1991;
    public static final int MODE_ADD_TO_CONVERSATION = 1992;

    public static final String MODE = "mode";

    private static final String TAG = ChatSDKPickFriendsActivity.class.getSimpleName();
    private static boolean DEBUG = Debug.PickFriendsActivity;

    private ListView listContacts;
    private ChatSDKUsersListAdapter listAdapter;
    private Button btnStartChat;
    private TextView txtSearch;
    private ImageView imgSearch;
    private CheckBox chSelectAll;

    /** Default value - MODE_NEW_CONVERSATION*/
    private int mode = MODE_NEW_CONVERSATION;

    /** For add to conversation mode.*/
    private long threadID = -1;

    /** For add to conversation mode.*/
    private BThread thread;

    /** Set true if you want slide down animation for this activity exit. */
    private boolean animateExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_pick_friends);
        initActionBar();

        if (savedInstanceState != null)
        {
            getDataFromBundle(savedInstanceState);
        }
        else
        {
            if (getIntent().getExtras() != null)
            {
                getDataFromBundle(getIntent().getExtras());
            }
        }

        initViews();
    }

    private void getDataFromBundle(Bundle bundle){
        mode = bundle.getInt(MODE, mode);
        threadID = bundle.getLong(ChatSDKBaseThreadActivity.THREAD_ID, threadID);
        animateExit = bundle.getBoolean(ChatSDKAbstractChatActivity.ANIMATE_EXIT, animateExit);
    }

    protected void initActionBar(){
        ActionBar ab = getActionBar();
        if (ab!=null)
        {
            ab.setTitle(getString(R.string.pick_friends));
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ChatSDKBaseThreadActivity.THREAD_ID, threadID);
        outState.putInt(MODE, mode);
        outState.putBoolean(ChatSDKAbstractChatActivity.ANIMATE_EXIT, animateExit);
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
        txtSearch = (TextView) findViewById(R.id.chat_sdk_et_search);
        imgSearch = (ImageView) findViewById(R.id.chat_sdk_search_image);
        btnStartChat = (Button) findViewById(R.id.chat_sdk_btn_add_contacts);
        chSelectAll = (CheckBox) findViewById(R.id.chat_sdk_chk_select_all);

        if (mode == MODE_ADD_TO_CONVERSATION)
            btnStartChat.setText(getResources().getString(R.string.add_users));
        
        if (!BDefines.Options.GroupEnabled)
        {
            btnStartChat.setVisibility(View.GONE);
            chSelectAll.setVisibility(View.GONE);
        }
    }

    private void initList(){
        final List<BUser> list = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getContacts();

        // Removing the users that is already inside the thread.
        if (mode == MODE_ADD_TO_CONVERSATION && threadID != -1){
            thread = DaoCore.fetchEntityWithProperty(BThread.class, BThreadDao.Properties.Id, threadID);
            List<BUser> threadUser = thread.getUsers();
            list.removeAll(threadUser);
        }

        if (list.size() > 0)
            chSelectAll.setEnabled(true);

        listAdapter = new ChatSDKUsersListAdapter(ChatSDKPickFriendsActivity.this, BDefines.Options.GroupEnabled);
        listAdapter.setUserItems(listAdapter.makeList(list, false, true));
        listContacts.setAdapter(listAdapter);

        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If groups enabled toggeling selection
                if (BDefines.Options.GroupEnabled)
                    listAdapter.toggleSelection(position);
                else
                {
                    createAndOpenThreadWithUsers("", getNetworkAdapter().currentUserModel(), listAdapter.getItem(position).asBUser());
                }
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

        initList();

        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listAdapter.getSelectedCount() == 0)
                {
                    showAlertToast(getString(R.string.pick_friends_activity_no_users_selected_toast));
                    return;
                }

                int addCurrent = 1;
                if (mode == MODE_ADD_TO_CONVERSATION)
                {
                    showProgDialog( getString(R.string.pick_friends_activity_prog_dialog_add_to_convo_message));
                    addCurrent = 0;
                }
                else if (mode == MODE_NEW_CONVERSATION)
                {
                    showProgDialog(getString(R.string.pick_friends_activity_prog_dialog_open_new_convo_message));
                    addCurrent = 1;
                }

                final BUser[] users = new BUser[listAdapter.getSelectedCount() + addCurrent];

                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        for (int i = 0 ; i < listAdapter.getSelectedCount() ; i++)
                        {
                            int pos = -1;
                            if (listAdapter.getSelectedUsersPositions().valueAt(i))
                                pos = listAdapter.getSelectedUsersPositions().keyAt(i);

                            users[i] = listAdapter.getUserItems().get(pos).asBUser();

                            if (DEBUG) Timber.d("Selected User[%s]: ", users[i].getMetaName());

                        }

                        if (mode == MODE_NEW_CONVERSATION)
                        {
                            users[users.length - 1] = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel();
                            createAndOpenThreadWithUsers("", users);
                            
                            chSelectAll.setSelected(false);
                        }
                        else if (mode == MODE_ADD_TO_CONVERSATION){

                            getNetworkAdapter().addUsersToThread(thread, users)
                                    .done(new DoneCallback<BThread>() {
                                        @Override
                                        public void onDone(BThread thread) {
                                            ChatSDKPickFriendsActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    setResult(Activity.RESULT_OK);

                                                    dismissProgDialog();

                                                    finish();

                                                    if (animateExit)
                                                        overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
                                                }
                                            });
                                        }
                                    })
                                    .fail(new FailCallback<BError>() {
                                        @Override
                                        public void onFail(BError error) {
                                            ChatSDKPickFriendsActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    dismissProgDialog();
                                                    setResult(Activity.RESULT_CANCELED);
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                }).start();
            }
        });

        View.OnClickListener searchClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatSDKUiHelper.startSearchActivity();
            }
        };

        txtSearch.setOnClickListener(searchClickListener);
        imgSearch.setOnClickListener(searchClickListener);


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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (animateExit)
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
    }


}
