package com.braunster.chatsdk.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.adapter.MessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.DaoCore;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BNetworkManager;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatActivity extends ActionBarActivity{

    // TODO listen to new  incoming  messages

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String THREAD_ID = "Thread_ID";

    private Button btnSend;
    private EditText etMessage;
    private ListView listMessages;
    private MessagesListAdapter messagesListAdapter;
    private ActivityListener activityListener;
    private BThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if ( !getThread() )
            return;

        initViews();
        initActionBar(thread.getName() == null || thread.getName().equals("") ? "Chat" : thread.getName());
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
        messagesListAdapter = new MessagesListAdapter(this, BNetworkManager.getInstance().currentUser().getEntityID());
        listMessages.setAdapter(messagesListAdapter);

        if (thread == null)
            Log.e(TAG, "Thread is null");
        messagesListAdapter.setListData(BNetworkManager.getInstance().getMessagesForThreadForEntityID(thread.getEntityID()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        BNetworkManager.getInstance().removeActivityListener(activityListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityListener = BNetworkManager.getInstance().addActivityListener(new ActivityListener() {
            @Override
            public void onThreadAdded(BThread thread) {

            }

            @Override
            public void onMessageAdded(BMessage message) {
                if (!message.getSender().equals(BNetworkManager.getInstance().currentUser().getEntityID()) && message.getOwnerThread().equals(thread.getEntityID()))
                    messagesListAdapter.addRow(message);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sendLogic();
            }
        });
    }

    private boolean getThread(){
        if ( getIntent() == null || getIntent().getExtras() == null)
        {
            if (DEBUG) Log.e(TAG, "No Extras");
            finish();
            return false;
        }

        if (getIntent().getExtras().getString(THREAD_ID, "").equals(""))
        {
            if (DEBUG) Log.e(TAG, "Thread id is empty");
            finish();
            return false;
        }

        thread = DaoCore.fetchEntityWithProperty(BThread.class,
                BThreadDao.Properties.EntityID,
                getIntent().getExtras().getString(THREAD_ID));

        if (thread == null)
        {
            if (DEBUG) Log.e(TAG, "No Thread found for given ID.");
            finish();
            return false;
        }

        return true;
    }

    private void sendLogic(){
        if (DEBUG) Log.v(TAG, "Send Logic");

        if (etMessage.getText().toString().isEmpty())
        {
            Toast.makeText(ChatActivity.this, "Cant send empty message!", Toast.LENGTH_SHORT).show();
            return;
        }

        BNetworkManager.getInstance().sendMessageWithText(etMessage.getText().toString(), thread.getEntityID(), new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage message) {
                if (DEBUG) Log.v(TAG, "Adding message");
                messagesListAdapter.addRow(message);
            }

            @Override
            public void onDoneWithError() {
                Toast.makeText(ChatActivity.this, "Message did not sent.", Toast.LENGTH_SHORT).show();
            }
        });

        etMessage.getText().clear();
    }
}
