package com.braunster.chatsdk.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.adapter.MessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BNetworkManager;

import java.io.File;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatActivity extends BaseActivity{

    // TODO listen to new  incoming  messages

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final int PHOTO_PICKER_ID = 100;

    public static final String THREAD_ID = "Thread_ID";

    private Button btnSend;
    private ImageButton btnOptions;
    private EditText etMessage;
    private ListView listMessages;
    private MessagesListAdapter messagesListAdapter;
    private ActivityListener activityListener;
    private BThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (BNetworkManager.getInstance().getNetworkAdapter() == null)
            setNetworkAdapterAndSync();

        if ( !getThread(savedInstanceState) )
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
        btnOptions = (ImageButton) findViewById(R.id.btn_options);
        etMessage = (EditText) findViewById(R.id.et_message_to_send);
        initListView();
    }

    private void initListView(){
        listMessages = (ListView) findViewById(R.id.list_chat);

        // If the user is null, Recreat the adapter and sync.
        if (BNetworkManager.getInstance().currentUser() != null)
            setNetworkAdapterAndSync(new CompletionListener() {
                @Override
                public void onDone() {
                    messagesListAdapter = new MessagesListAdapter(ChatActivity.this, BNetworkManager.getInstance().currentUser().getEntityID());
                    listMessages.setAdapter(messagesListAdapter);

                    if (thread == null)
                        Log.e(TAG, "Thread is null");
                    messagesListAdapter.setListData(BNetworkManager.getInstance().getMessagesForThreadForEntityID(thread.getEntityID()));
                }

                @Override
                public void onDoneWithError() {
                    Toast.makeText(ChatActivity.this, "Failed to set adapter and sync.", Toast.LENGTH_SHORT).show();
                }
            });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (thread != null)
            outState.putString(THREAD_ID, thread.getEntityID());
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

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent,
                        "Complete action using"), PHOTO_PICKER_ID);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (DEBUG) Log.v(TAG, "onActivityResult");

        if (requestCode == PHOTO_PICKER_ID)
        {
            switch (resultCode)
            {
                case Activity.RESULT_OK:
                    if (DEBUG) Log.d(TAG, "Result OK");
                    Uri uri = (Uri) data.getData();
                    File image = null;
                    try
                    {
                        image = Utils.getFile(this, uri);
                    }
                    catch (NullPointerException e){
                        if (DEBUG) Log.e(TAG, "Null pointer when getting file.");
                        Toast.makeText(ChatActivity.this, "Unable to fetch image", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (image != null) {
                        if (DEBUG) Log.i(TAG, "Image is not null");
                        BNetworkManager.getInstance().sendMessageWithImage(image, thread.getEntityID(), new CompletionListenerWithData<BMessage>() {
                            @Override
                            public void onDone(BMessage bMessage) {
                                if (DEBUG) Log.v(TAG, "Image is sent");
                                messagesListAdapter.addRow(bMessage);
                            }

                            @Override
                            public void onDoneWithError() {
                                Toast.makeText(ChatActivity.this, "Image could not been sent.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if (DEBUG) Log.e(TAG, "Image is null");

                    break;

                case Activity.RESULT_CANCELED:
                    if (DEBUG) Log.d(TAG, "Result Canceled");
                    break;

                default:
                    if (DEBUG) Log.d(TAG, "Default");
                    break;
            }
        }

    }

    private boolean getThread(Bundle savedInstanceBundle){
        Bundle b;

        if (savedInstanceBundle != null && savedInstanceBundle.containsKey(THREAD_ID))
        {
            if (DEBUG) Log.d(TAG, "Saved instance bundle is not null");
            b = savedInstanceBundle;
        }
        else
        {
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

            b = getIntent().getExtras();
        }

        thread = DaoCore.fetchEntityWithProperty(BThread.class,
                BThreadDao.Properties.EntityID,
                b.getString(THREAD_ID));

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
