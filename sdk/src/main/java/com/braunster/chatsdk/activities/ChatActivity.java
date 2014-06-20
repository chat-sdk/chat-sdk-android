package com.braunster.chatsdk.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.DialogUtils;
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
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.Locale;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener{

    // TODO listen to new  incoming  messages
    // TODO add popup for message options.
    // TODO add location message.
    // TODO auto scroll to bottom.
    // TODO add button to add users to action bar.

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final int PHOTO_PICKER_ID = 100;
    private static final int CAPTURE_IMAGE = 101;
    public static final int PICK_LOCATION = 102;

    public static final String THREAD_ID = "Thread_ID";

    private Button btnSend;
    private ImageButton btnOptions;
    private EditText etMessage;
    private ListView listMessages;
    private MessagesListAdapter messagesListAdapter;
    private ActivityListener activityListener;
    private BThread thread;
    private PopupWindow optionPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_chat);

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
        btnSend = (Button) findViewById(R.id.chat_sdk_btn_chat_send_message);
        btnOptions = (ImageButton) findViewById(R.id.chat_sdk_btn_options);
        etMessage = (EditText) findViewById(R.id.chat_sdk_et_message_to_send);
        initListView();
    }

    private void initListView(){
        listMessages = (ListView) findViewById(R.id.list_chat);
        listMessages.setItemsCanFocus(true);
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

        btnSend.setOnClickListener(this);

        btnOptions.setOnClickListener(this);
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
        else if (requestCode == PICK_LOCATION)
        {
            if (DEBUG) Log.d(TAG, "Location Pick returned");

            if (resultCode == Activity.RESULT_CANCELED) {
                if (DEBUG) Log.d(TAG, "Result Cancelled");
                if (data.getExtras().containsKey(LocationActivity.ERROR))
                    Toast.makeText(this, data.getExtras().getString(LocationActivity.ERROR), Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == Activity.RESULT_OK)
            {
                if (DEBUG) Log.d(TAG, "Result OK");
                // Send the message, Params Latitude, Longitude, Base64 Representation of the image of the location, threadId.
                BNetworkManager.getInstance().sendMessageWithLocation(data.getExtras().getString(LocationActivity.BASE_64_FILE, null),
                                        new LatLng(data.getDoubleExtra(LocationActivity.LANITUDE, 0), data.getDoubleExtra(LocationActivity.LONGITUDE, 0)),
                                        thread.getEntityID(), new CompletionListenerWithData<BMessage>() {
                            @Override
                            public void onDone(BMessage bMessage) {
                                if (DEBUG) Log.v(TAG, "Image is sent");
                                messagesListAdapter.addRow(bMessage);
                            }

                            @Override
                            public void onDoneWithError() {
                                Toast.makeText(ChatActivity.this, "Location could not been sent.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_add_chat_room, 10, "Add Chat");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(android.R.drawable.ic_menu_add);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
        // ASK what add button do in this class
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

    private void showOptionPopup(){
        if (optionPopup!= null && optionPopup.isShowing())
        {
            if (DEBUG) Log.d(TAG, "Tying to show option popup when already showing");
            return;
        }

        optionPopup = DialogUtils.getMenuOptionPopup(this, this);
        optionPopup.showAsDropDown(btnOptions);
    }

    private void dismissOption(){
        if (optionPopup != null)
            optionPopup.dismiss();
    }

    @Override
    public void onClick(View v) {
        int id= v.getId();

        if (id == R.id.chat_sdk_btn_chat_send_message) {
            sendLogic();
        }
        else if (id == R.id.chat_sdk_btn_options){
            showOptionPopup();
        }
        else  if (id == R.id.chat_sdk_btn_choose_picture) {
            // TODO allow multiple pick of photos.
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), PHOTO_PICKER_ID);

            dismissOption();
        }
        else  if (id == R.id.chat_sdk_btn_take_picture) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE);
        }
        else  if (id == R.id.chat_sdk_btn_location) {
            Intent intent = new Intent(ChatActivity.this, LocationActivity.class);
            startActivityForResult(intent, PICK_LOCATION);
            dismissOption();
        }
    }
}
