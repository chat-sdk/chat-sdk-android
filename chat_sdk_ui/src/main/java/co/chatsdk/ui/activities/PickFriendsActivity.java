/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import co.chatsdk.core.NM;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.Defines;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.threads.ThreadDetailsActivity;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.ui.contacts.UsersListAdapter;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import co.chatsdk.ui.chat.ChatActivity;

/**
 * Created by itzik on 6/17/2014.
 */
public class PickFriendsActivity extends BaseActivity {

    public static final int MODE_NEW_CONVERSATION = 1991;
    public static final int MODE_ADD_TO_CONVERSATION = 1992;

    public static final String MODE = "mode";

    private static final String TAG = PickFriendsActivity.class.getSimpleName();
    private static boolean DEBUG = Debug.PickFriendsActivity;

    private ListView listContacts;
    private UsersListAdapter listAdapter;
    private Button btnStartChat;
    private TextView txtSearch;
    private ImageView imgSearch;
    private CheckBox chSelectAll;

    /** Default value - MODE_NEW_CONVERSATION*/
    private int mode = MODE_NEW_CONVERSATION;

    /** For add to conversation mode.*/
    private long threadID = -1;

    /** For add to conversation mode.*/
    private Thread thread;

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

        // Refresh the list when the contacts change
        NM.events().sourceOnMain()
                .filter(NetworkEvent.filterContactsChanged())
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                        loadData();
                    }
                });


        initViews();
    }

    private void getDataFromBundle(Bundle bundle){
        mode = bundle.getInt(MODE, mode);
        threadID = bundle.getLong(ThreadDetailsActivity.THREAD_ID, threadID);
        animateExit = bundle.getBoolean(ChatActivity.ANIMATE_EXIT, animateExit);
    }

    protected void initActionBar(){
        ActionBar ab = getSupportActionBar();
        if (ab!=null)
        {
            ab.setTitle(getString(R.string.pick_friends));
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ThreadDetailsActivity.THREAD_ID, threadID);
        outState.putInt(MODE, mode);
        outState.putBoolean(ChatActivity.ANIMATE_EXIT, animateExit);
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

        if (mode == MODE_ADD_TO_CONVERSATION)
            btnStartChat.setText(getResources().getString(R.string.add_users));
        
        if (!Defines.Options.GroupEnabled) {
            btnStartChat.setVisibility(View.GONE);
        }
    }

    private void initList(){
        listAdapter = new UsersListAdapter(PickFriendsActivity.this, Defines.Options.GroupEnabled);
        listContacts.setAdapter(listAdapter);

        loadData();

        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If groups enabled toggeling selection
                if (Defines.Options.GroupEnabled)
                    listAdapter.toggleSelection(position);
                else
                {
                    createAndOpenThreadWithUsers("", NM.currentUser(), listAdapter.getItem(position).getUser());
                }
            }
        });
    }

    private void loadData () {
        final List<User> list = NM.contact().contacts();

        // Removing the users that is already inside the thread.
        if (mode == MODE_ADD_TO_CONVERSATION && threadID != -1){
            thread = StorageManager.shared().fetchThreadWithID(threadID);
            List<User> threadUser = thread.getUsers();
            list.removeAll(threadUser);
        }

        listAdapter.setUsers(list, true);

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

                if (listAdapter.getSelectedCount() == 0) {
                    showToast(getString(R.string.pick_friends_activity_no_users_selected_toast));
                    return;
                }

                if (mode == MODE_ADD_TO_CONVERSATION) {
                    showProgDialog( getString(R.string.pick_friends_activity_prog_dialog_add_to_convo_message));
                }
                else if (mode == MODE_NEW_CONVERSATION) {
                    showProgDialog(getString(R.string.pick_friends_activity_prog_dialog_open_new_convo_message));
                }

                final ArrayList<User> users = new ArrayList<>();

                for (int i = 0 ; i < listAdapter.getSelectedCount() ; i++) {
                    int pos = listAdapter.getSelectedUsersPositions().keyAt(i);
                    users.add(listAdapter.getUserItems().get(pos).getUser());
                }

                if (mode == MODE_NEW_CONVERSATION) {
                    users.add(NM.currentUser());
                    // If there are more than 2 users then show a dialog to enter the name
                    if(users.size() > 2) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PickFriendsActivity.this);
                        builder.setTitle(getString(R.string.pick_friends_activity_prog_group_name_dialog));

                        // Set up the input
                        final EditText input = new EditText(PickFriendsActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton(getString(R.string.create), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createAndOpenThreadWithUsers(input.getText().toString(), users);
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();

                    }
                    else {
                        createAndOpenThreadWithUsers("", users);
                    }
                }
                else if (mode == MODE_ADD_TO_CONVERSATION){

                    NM.thread().addUsersToThread(thread, users).doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            PickFriendsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setResult(AppCompatActivity.RESULT_OK);
                                    dismissProgDialog();
                                    finish();
                                    if (animateExit)
                                        overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
                                }
                            });
                        }
                    }).doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            PickFriendsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgDialog();
                                    setResult(AppCompatActivity.RESULT_CANCELED);
                                    finish();
                                }
                            });
                        }
                    }).subscribe();

                }
            }
        });

        View.OnClickListener searchClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.getInstance().startSearchActivity();
            }
        };

        txtSearch.setOnClickListener(searchClickListener);
        imgSearch.setOnClickListener(searchClickListener);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (animateExit)
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
    }


}
