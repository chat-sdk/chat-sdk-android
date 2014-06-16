package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.adapter.MessagesListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatActivity extends ActionBarActivity{

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String THREAD_ID = "Thread_ID";

    private Button btnSend;
    private EditText etMessage;
    private ListView listMessages;
    private MessagesListAdapter messagesListAdapter;

    private BThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();


        if ( getIntent() == null || getIntent().getExtras() == null)
        {
            if (DEBUG) Log.e(TAG, "No Extras");
            finish();
            return;
        }

        if (getIntent().getExtras().getString(THREAD_ID, "").equals(""))
        {
            if (DEBUG) Log.e(TAG, "Thread id is empty");
            finish();
            return;
        }

        thread = DaoCore.fetchEntityWithProperty(BThread.class,
                                                    BThreadDao.Properties.EntityID,
                                                        getIntent().getExtras().getString(THREAD_ID));

        if (thread == null)
        {
            if (DEBUG) Log.e(TAG, "No Thread found for given ID.");
            finish();
            return;
        }

        initActionBar(thread.getName());
    }

    private void initActionBar(String username){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getSupportActionBar();
            ab.setTitle(username);
        }
    }

    private void initViews(){
        btnSend = (Button) findViewById(R.id.btn_send);
        etMessage = (EditText) findViewById(R.id.et_message_to_send);
        initListView();
    }

    private void initListView(){
        listMessages = (ListView) findViewById(R.id.list_chat);
        messagesListAdapter = new MessagesListAdapter(this);
        listMessages.setAdapter(messagesListAdapter);
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
