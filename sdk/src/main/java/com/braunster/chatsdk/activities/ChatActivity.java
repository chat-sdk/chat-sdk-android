package com.braunster.chatsdk.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.adapter.MessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.ContactsFragment;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.MessageEventListener;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.object.BError;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener{

    // TODO add button to add users to action bar.
    // TODO implement bubbles UI

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final int PHOTO_PICKER_ID = 100;
    private static final int CAPTURE_IMAGE = 101;
    public static final int PICK_LOCATION = 102;

    public static final String THREAD_ID = "Thread_ID";
    public static final String MessageListenerTAG = TAG + "MessageTAG";

    private Button btnSend;
    private ImageButton btnOptions;
    private EditText etMessage;
    private ListView listMessages;
    private MessagesListAdapter messagesListAdapter;
    private BThread thread;
    private PopupWindow optionPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate");
        setContentView(R.layout.chat_sdk_activity_chat);

        if ( !getThread(savedInstanceState) )
            return;

        initViews();

        initListView();

        initActionBar(thread.displayName() == null || thread.displayName().equals("") ? "Chat" : thread.displayName());
    }

    private void initActionBar(String username){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getSupportActionBar();
            ab.setTitle(username);
//            ab.setIcon(new BitmapDrawable(thread.getUsers().get(0).getMetaPicture()));
        }
    }

    private void initViews(){
        btnSend = (Button) findViewById(R.id.chat_sdk_btn_chat_send_message);
        btnOptions = (ImageButton) findViewById(R.id.chat_sdk_btn_options);
        etMessage = (EditText) findViewById(R.id.chat_sdk_et_message_to_send);
    }

    private void initListView(){
        listMessages = (ListView) findViewById(R.id.list_chat);
        listMessages.setItemsCanFocus(true);

        messagesListAdapter = new MessagesListAdapter(ChatActivity.this, BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getId());
        listMessages.setAdapter(messagesListAdapter);

        loadMessages();
    }

    private void loadMessages(){
        if (thread == null)
        {
            Log.e(TAG, "Thread is null");
            return;
        }

        messagesListAdapter.setListData(
                messagesListAdapter.makeList(BNetworkManager.sharedManager().getNetworkAdapter().getMessagesForThreadForEntityID(thread.getId())));
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (thread != null)
            outState.putLong(THREAD_ID, thread.getId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventManager.getInstance().removeEventByTag(MessageListenerTAG + thread.getId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) Log.v(TAG, "onNewIntent");

        if ( !getThread(intent.getExtras()) )
            return;

        initListView();

        initActionBar(thread.displayName() == null || thread.displayName().equals("") ? "Chat" : thread.displayName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume");
        listMessages.post(new Runnable() {
            @Override
            public void run() {
                listMessages.smoothScrollToPosition(messagesListAdapter.getCount()-1);
            }
        });

        loadMessages();

        // Removing the last listener just to be sure we wont receive duplicates notifications.
        EventManager.getInstance().removeEventByTag(MessageListenerTAG + thread.getId());
        EventManager.getInstance().addMessageEvent(new MessageEventListener(MessageListenerTAG + thread.getId(), thread.getEntityID()){
            @Override
            public boolean onMessageReceived(BMessage message) {


                // Check that the message is relevant to the current thread.
                if (!message.getBThreadOwner().getEntityID().equals(thread.getEntityID()) || message.getOwnerThread() != thread.getId())
                    return false;
                // Make sure the message that incoming is not the user message.
                if (message.getBUserSender().getEntityID().equals(
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID()) )
                    return false;

                messagesListAdapter.addRow(message);
                // We check to see that this message is really a new one and not loaded from the server.
                if (System.currentTimeMillis() - message.getDate().getTime() < 1000*60)
                {
                    Vibrator v = (Vibrator) ChatActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(BDefines.VIBRATION_DURATION);
                }

                return false;
            }
        });

        btnSend.setOnClickListener(this);

        btnOptions.setOnClickListener(this);

        etMessage.setOnEditorActionListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
        {
            if (DEBUG) Log.e(TAG, "onActivityResult, Intent is null");
            return;
        }

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

                        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithImage(
                                image.getPath(), thread.getId(), new CompletionListenerWithData<BMessage>() {
                            @Override
                            public void onDone(BMessage bMessage) {
                                if (DEBUG) Log.v(TAG, "Image is sent");
                                messagesListAdapter.addRow(bMessage);
                            }

                            @Override
                            public void onDoneWithError(BError error) {
                                Toast.makeText(ChatActivity.this, "Image could not been sent. " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                if (data.getExtras() == null)
                    return;

                if (data.getExtras().containsKey(LocationActivity.ERROR))
                    Toast.makeText(this, data.getExtras().getString(LocationActivity.ERROR), Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == Activity.RESULT_OK)
            {
                if (DEBUG) Log.d(TAG, "Result OK");
                // Send the message, Params Latitude, Longitude, Base64 Representation of the image of the location, threadId.
                BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithLocation(data.getExtras().getString(LocationActivity.SNAP_SHOT_PATH, null),
                                        new LatLng(data.getDoubleExtra(LocationActivity.LANITUDE, 0), data.getDoubleExtra(LocationActivity.LONGITUDE, 0)),
                                        thread.getId(), new CompletionListenerWithData<BMessage>() {
                            @Override
                            public void onDone(BMessage bMessage) {
                                if (DEBUG) Log.v(TAG, "Image is sent");
                                messagesListAdapter.addRow(bMessage);
                            }

                            @Override
                            public void onDoneWithError(BError error) {
                                Toast.makeText(ChatActivity.this, "Location could not been sent.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        else if (requestCode == CAPTURE_IMAGE)
        {
            if (DEBUG) Log.d(TAG, "Capture image return");
            if (resultCode == Activity.RESULT_OK) {
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

                BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithImage(
                        image.getPath(), thread.getId(), new CompletionListenerWithData<BMessage>() {
                            @Override
                            public void onDone(BMessage bMessage) {
                                if (DEBUG) Log.v(TAG, "Image is sent");
                                messagesListAdapter.addRow(bMessage);
                            }

                            @Override
                            public void onDoneWithError(BError error) {
                                Toast.makeText(ChatActivity.this, "Image could not been sent. " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, "Add contact to chat.");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);

        MenuItem itemThreadUsers =
                menu.add(Menu.NONE, R.id.action_chat_sdk_show, 10, "Show thread users.");
        itemThreadUsers.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        itemThreadUsers.setIcon(android.R.drawable.ic_menu_info_details);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        // ASK what the add button do in this class
        if (id == R.id.action_chat_sdk_add)
        {
            ContactsFragment contactsFragment = ContactsFragment.newDialogInstance(
                                                                                    ContactsFragment.MODE_LOAD_CONTACTS,
                                                                                    ContactsFragment.CLICK_MODE_ADD_USER_TO_THREAD,
                                                                                    "Contacts:",
                                                                                    thread.getEntityID());
            contactsFragment.show(getSupportFragmentManager(), "Contacts");
        }
        else if (id == R.id.action_chat_sdk_show)
        {
            ContactsFragment contactsFragment = ContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), "Thread Users:");
            contactsFragment.show(getSupportFragmentManager(), "Contacts");
        }

        return super.onOptionsItemSelected(item);
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

            if (getIntent().getExtras().getLong(THREAD_ID, 0) == 0)
            {
                if (DEBUG) Log.e(TAG, "Thread id is empty");
                finish();
                return false;
            }

            b = getIntent().getExtras();
        }

        thread = DaoCore.fetchEntityWithProperty(BThread.class,
                BThreadDao.Properties.Id,
                b.getLong(THREAD_ID));

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

        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithText(etMessage.getText().toString(), thread.getId(), new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage message) {
                if (DEBUG) Log.v(TAG, "Adding message");
                messagesListAdapter.addRow(message);
            }

            @Override
            public void onDoneWithError(BError error) {
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
            dismissOption();

            // TODO allow multiple pick of photos.
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), PHOTO_PICKER_ID);


        }
        else  if (id == R.id.chat_sdk_btn_take_picture) {
            if (!Utils.SystemChecks.checkCameraHardware(this))
            {
                Toast.makeText(this, "This device does not have a camera.", Toast.LENGTH_SHORT).show();
                return;
            }

            dismissOption();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.FileSaver.getAlbumStorageDir(Utils.FileSaver.IMAGE_DIR_NAME).getAbsolutePath());

            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE);
        }
        else  if (id == R.id.chat_sdk_btn_location) {
            dismissOption();
            Intent intent = new Intent(ChatActivity.this, LocationActivity.class);
            startActivityForResult(intent, PICK_LOCATION);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND)
            sendLogic();

        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
    }
}
