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
import android.view.LayoutInflater;
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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.MessageSorter;
import com.braunster.chatsdk.Utils.NotificationUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.adapter.MessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.ContactsFragment;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.MessageEventListener;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.parse.PushUtils;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener{

    // TODO add button to add users to action bar.
    // TODO implement bubbles UI

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatActivity;

    private static final int PHOTO_PICKER_ID = 100;
    private static final int CAPTURE_IMAGE = 101;
    public static final int PICK_LOCATION = 102;
    public static final int ADD_USERS = 103;

    /** The message event listener tag, This is used so we could find and remove the listener from the EventManager.
     * It will be removed when activity is paused. or when opend again for new thread.*/
    public static final String MessageListenerTAG = TAG + "MessageTAG";

    /** The key to get the thread long id.*/
    public static final String THREAD_ID = "Thread_ID";
    public static final String THREAD_ENTITY_ID = "Thread_Entity_ID";

    public static final String FROM_PUSH = "from_push";

    /** The key to get the path of the last captured image path in case the activity is destroyed while capturing.*/
    public static final String CAPTURED_PHOTO_PATH = "captured_photo_path";

    /** The key to get the shared file uri. This is used when the activity is opened to share and image or a file with the chat users.
     *  The Activity will be open from the ContactsFragment that will be placed inside the ShareWithContactActivity. */
    public static final String SHARED_FILE_URI = "share_file_uri";

    /** The key to get shared text, this is used when the activity is open to share text with the chat user.
     *  The Activity will be open from the ContactsFragment that will be placed inside the ShareWithContactActivity. */
    public static final String SHARED_TEXT = "shared_text";

    private Button btnSend;
    private ImageButton btnOptions;
    private EditText etMessage;
    private ListView listMessages;
    private MessagesListAdapter messagesListAdapter;
    private BThread thread;
    private PopupWindow optionPopup;
    private PullToRefreshLayout mPullToRefreshLayout;

    private String capturePhotoPath = "";

    private Bundle data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableCheckOnlineOnResumed(true);
        setEnableCardToast(true);

        super.onCreate(savedInstanceState);

        if (DEBUG) Log.v(TAG, "onCreate");

        setContentView(R.layout.chat_sdk_activity_chat_pull_to_refresh);

        setupTouchUIToDismissKeyboard(findViewById(R.id.chat_sdk_root_view), R.id.chat_sdk_btn_chat_send_message, R.id.chat_sdk_btn_options);

        if ( !getThread(savedInstanceState) )
            return;

        if (savedInstanceState != null)
        {
            if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH))
            {
                capturePhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH);
                savedInstanceState.remove(CAPTURED_PHOTO_PATH);
            }
        }

        initViews();

        initToast();

        initListView();

        initActionBar();

//        checkIfWantToShare(getIntent());
    }

    private void initActionBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getSupportActionBar();
            ab.setDisplayShowHomeEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayShowCustomEnabled(true);
//            ab.setTitle(username);

            /*http://stackoverflow.com/questions/16026818/actionbar-custom-view-with-centered-imageview-action-items-on-sides*/

            // Inflate the custom view
            LayoutInflater inflater = LayoutInflater.from(this);
            View header = inflater.inflate( R.layout.chat_sdk_actionbar_circle_imageview, null );

            TextView txtName = (TextView) header.findViewById(R.id.chat_sdk_name);

            txtName.setText(thread.displayName());

            txtName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToast(((TextView) v).getText().toString());
                }
            });

            final CircleImageView circleImageView = (CircleImageView) header.findViewById(R.id.image);

            VolleyUtills.getImageLoader().get(thread.threadImageUrl(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null)
                        circleImageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            ab.setCustomView(header);
        }
    }

    private void initViews(){
        btnSend = (Button) findViewById(R.id.chat_sdk_btn_chat_send_message);
        btnOptions = (ImageButton) findViewById(R.id.chat_sdk_btn_options);
        etMessage = (EditText) findViewById(R.id.chat_sdk_et_message_to_send);

        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set a OnRefreshListener
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        if (DEBUG) Log.d(TAG, "onRefreshStarted");

                        BNetworkManager.sharedManager().getNetworkAdapter().loadMoreMessagesForThread(thread, new CompletionListenerWithData<BMessage[]>() {
                                    @Override
                                    public void onDone(BMessage[] messages) {
                                        if (DEBUG)
                                            Log.d(TAG, "New messages are loaded, Amount: " + (messages == null ? "No messages" : messages.length));

                                        if (messages.length < 2)
                                            showToast("There is no new messages to load...");
                                        else {
                                            // Saving the position in the list so we could back to it after the update.
                                            loadMessagesAndRetainPos(messages.length - 2);
                                        }

                                        mPullToRefreshLayout.setRefreshComplete();
                                    }

                                    @Override
                                    public void onDoneWithError(BError error) {

                                    }
                                }
                        );
                    }
                })
        // Finally commit the setup to our PullToRefreshLayout
        .setup(mPullToRefreshLayout);


    }

    private void initListView(){
        listMessages = (ListView) findViewById(R.id.list_chat);
        listMessages.setItemsCanFocus(true);

        messagesListAdapter = new MessagesListAdapter(ChatActivity.this, BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getId());
        listMessages.setAdapter(messagesListAdapter);

        loadMessages();
    }

    private void loadMessagesAndRetainPos(int newDataSize){
        int index = listMessages.getFirstVisiblePosition() + newDataSize;
        View v = listMessages.getChildAt(0);
        int top = (v == null) ? -1 : v.getTop();
        loadMessages(index, top);
    }

    private void loadMessages(final int index, final int top){
        if (thread == null)
        {
            Log.e(TAG, "Thread is null");
            return;
        }
        if (top != -1 && index != -1)
            listMessages.setVisibility(View.INVISIBLE);

        List<BMessage> messages = BNetworkManager.sharedManager().getNetworkAdapter().getMessagesForThreadForEntityID(thread.getId());

        Collections.sort(messages, new MessageSorter(MessageSorter.ORDER_TYPE_DESC));

        messagesListAdapter.setListData(
                messagesListAdapter.makeList(messages));

        // restore
        if (top != -1 && index != -1)
            listMessages.post(new Runnable() {
                @Override
                public void run() {
                    listMessages.setSelectionFromTop(index, top);
                    listMessages.setVisibility(View.VISIBLE);
                }
            });
    }

    private void loadMessages(){
        loadMessages(-1, -1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (thread != null)
            outState.putLong(THREAD_ID, thread.getId());

        if (StringUtils.isNotEmpty(capturePhotoPath))
            outState.putString(CAPTURED_PHOTO_PATH, capturePhotoPath);
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

        initActionBar();

        checkIfWantToShare(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume");
//        listMessages.post(new Runnable() {
//            @Override
//            public void run() {
//                listMessages.smoothScrollToPosition(messagesListAdapter.getCount()-1);
//            }
//        });
        if ( !getThread(data) )
            return;

        NotificationUtils.cancelNotification(this, PushUtils.MESSAGE_NOTIFICATION_ID);

        loadMessages();

        MessageEventListener messageEventListener = new MessageEventListener(MessageListenerTAG + thread.getId(), thread.getEntityID()) {
            @Override
            public boolean onMessageReceived(BMessage message) {
                if (DEBUG) Log.v(TAG, "onMessageReceived, EntityID: " + message.getEntityID());

                // Check that the message is relevant to the current thread.
                if (!message.getBThreadOwner().getEntityID().equals(thread.getEntityID()) || message.getOwnerThread() != thread.getId().intValue())
                    return false;

                if (!messagesListAdapter.addRow(message))
                    return false;

                // Check if the message from the current user, If so return so we wont vibrate for the user messages.
                if (message.getBUserSender().getEntityID().equals(
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID()) )
                    return false;

                // We check to see that this message is really a new one and not loaded from the server.
                if (System.currentTimeMillis() - message.getDate().getTime() < 1000*60)
                {
                    Vibrator v = (Vibrator) ChatActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(BDefines.VIBRATION_DURATION);
                }

                return false;
            }
        };

        // Removing the last listener just to be sure we wont receive duplicates notifications.
        EventManager.getInstance().removeEventByTag(MessageListenerTAG + thread.getId());
        EventManager.getInstance().addMessageEvent(messageEventListener);

        btnSend.setOnClickListener(this);

        btnOptions.setOnClickListener(this);

        etMessage.setOnEditorActionListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) Log.v(TAG, "onActivityResult");

        if (requestCode != CAPTURE_IMAGE && requestCode != ADD_USERS && data == null)
        {
            if (DEBUG) Log.e(TAG, "onActivityResult, Intent is null");
            return;
        }

        if (requestCode != ADD_USERS && resultCode == Activity.RESULT_OK) {
            showCard("Saving...", 50);
        }

        /* Pick photo logic*/
        if (requestCode == PHOTO_PICKER_ID)
        {
            switch (resultCode)
            {
                case Activity.RESULT_OK:
                    if (DEBUG) Log.d(TAG, "Result OK");
                    Uri uri = data.getData();
                    File image = null;
                    try
                    {
                        image = Utils.getFile(this, uri);
                    }
                    catch (NullPointerException e){
                        if (DEBUG) Log.e(TAG, "Null pointer when getting file.");
                        showAlertToast("Unable to fetch image");
                        dismissCardWithSmallDelay();
                        return;
                    }

                    if (image != null) {
                        if (DEBUG) Log.i(TAG, "Image is not null");
                        sendImageMessage(image.getPath());
                    }
                    else {
                        if (DEBUG) Log.e(TAG, "Image is null");
                        dismissCardWithSmallDelay();
                        showAlertToast("Error when loading the image.");
                    }

                    break;

                case Activity.RESULT_CANCELED:
                    if (DEBUG) Log.d(TAG, "Result Canceled");
                    break;
            }
        }
        /* Pick location logic*/
        else if (requestCode == PICK_LOCATION)
        {

            if (resultCode == Activity.RESULT_CANCELED) {
                if (DEBUG) Log.d(TAG, "Result Cancelled");
                if (data.getExtras() == null)
                    return;

                if (data.getExtras().containsKey(LocationActivity.ERROR))
                    showAlertToast(data.getExtras().getString(LocationActivity.ERROR));
            }
            else if (resultCode == Activity.RESULT_OK)
            {
                if (DEBUG) Log.d(TAG, "Result OK");
                if (DEBUG) Log.d(TAG, "Zoom level: " + data.getFloatExtra(LocationActivity.ZOOM, 0.0f));
                // Send the message, Params Latitude, Longitude, Base64 Representation of the image of the location, threadId.
                BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithLocation(data.getExtras().getString(LocationActivity.SNAP_SHOT_PATH, null),
                                        new LatLng(data.getDoubleExtra(LocationActivity.LANITUDE, 0), data.getDoubleExtra(LocationActivity.LONGITUDE, 0)),
                                        thread.getId(), new CompletionListenerWithData<BMessage>() {
                            @Override
                            public void onDone(BMessage bMessage) {
                                if (DEBUG) Log.v(TAG, "Image is sent");
                                updateCard("Sent...", 100);
                                dismissCardWithSmallDelay();
//                                messagesListAdapter.addRow(bMessage);
                            }

                            @Override
                            public void onDoneWithError(BError error) {
                                dismissCardWithSmallDelay();
                                showAlertToast("Location could not been sent.");
                            }
                        });
            }
        }
        /* Capture image logic*/
        else if (requestCode == CAPTURE_IMAGE)
        {
            if (DEBUG) Log.d(TAG, "Capture image return");
            if (resultCode == Activity.RESULT_OK) {
                if (DEBUG) Log.d(TAG, "Result OK");

                sendImageMessage(capturePhotoPath);
            }
        }

        else if (requestCode == ADD_USERS)
        {
            if (DEBUG) Log.d(TAG, "ADD_USER_RETURN");
            if (resultCode == RESULT_OK)
            {
                getThread(this.data);
                initActionBar();
            }
   /*         else showAlertToast("Failed to add users.");*/

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
/*
            ContactsFragment contactsFragment = ContactsFragment.newDialogInstance(
                                                                                    ContactsFragment.MODE_LOAD_CONTACTS,
                                                                                    ContactsFragment.CLICK_MODE_ADD_USER_TO_THREAD,
                                                                                    "Contacts:",
                                                                                    thread.getEntityID());
            contactsFragment.show(getSupportFragmentManager(), "Contacts");
*/

            // Showign the pick firends activity.
            Intent intent = new Intent(this, PickFriendsActivity.class);
            intent.putExtra(PickFriendsActivity.MODE, PickFriendsActivity.MODE_ADD_TO_CONVERSATION);
            intent.putExtra(PickFriendsActivity.THREAD_ID, thread.getId());
            intent.putExtra(PickFriendsActivity.ANIMATE_EXIT, true);

            startActivityForResult(intent, ADD_USERS);

            overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
        }
        else if (id == R.id.action_chat_sdk_show)
        {
            ContactsFragment contactsFragment = ContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), "Thread Users:");
            contactsFragment.show(getSupportFragmentManager(), "Contacts");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAuthenticated() {
        super.onAuthenticated();
        if (DEBUG) Log.v(TAG, "onAuthenticated");
        loadMessages();
    }


    /** Get the current thread from the bundle data, Thread could be in the getIntent or in onNewIntent.*/
    private boolean getThread(Bundle bundle){

        if (bundle != null && (bundle.containsKey(THREAD_ID) || bundle.containsKey(THREAD_ENTITY_ID)) )
        {
            if (DEBUG) Log.d(TAG, "Saved instance bundle is not null");
            this.data = bundle;
        }
        else
        {
            if ( getIntent() == null || getIntent().getExtras() == null)
            {
                if (DEBUG) Log.e(TAG, "No Extras");
                finish();
                return false;
            }

            this.data = getIntent().getExtras();
        }

        if (this.data.containsKey(THREAD_ID))
        {
            thread = DaoCore.<BThread>fetchEntityWithProperty(BThread.class,
                    BThreadDao.Properties.Id,
                    this.data.getLong(THREAD_ID));
        }
        else  if (this.data.containsKey(THREAD_ENTITY_ID))
        {
            thread = DaoCore.<BThread>fetchEntityWithProperty(BThread.class,
                    BThreadDao.Properties.EntityID,
                    this.data.getString(THREAD_ENTITY_ID));
        }else{
            if (DEBUG) Log.e(TAG, "Thread id is empty");
            finish();
            return false;
        }

        if (thread == null)
        {
            if (DEBUG) Log.e(TAG, "No Thread found for given ID.");
            finish();
            return false;
        }

        return true;
    }

    /** Send text message logic.*/
    private void sendTextMessage(){
        sendTextMessage(etMessage.getText().toString(), true);
    }
    /** Send text message
     * @param text the text to send.
     * @param clearEditText if true clear the message edit text.*/
    private void sendTextMessage(String text, boolean clearEditText){
        if (DEBUG) Log.v(TAG, "sendTextMessage, Text: " + text + ", Clear: " + String.valueOf(clearEditText));

        if (StringUtils.isEmpty(text) || StringUtils.isBlank(text))
        {
           showAlertToast("Cant send empty message!");
            return;
        }

        // Clear all white space from message
        text = text.trim();

//        showCard("Sending...", 50);

        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithText(text, thread.getId(), new CompletionListenerWithData<BMessage>() {
            @Override
            public void onDone(BMessage message) {

//                updateCard("Status...", 100);
//                dismissCardWithSmallDelay();

                // If the event manager is not listening to the current thread we will add the message to the list from here.
                // This could happen when the app is authenticating after it was killed by the system.
                if (!EventManager.getInstance().isListeningToIcomingMessages(thread.getEntityID()))
                    messagesListAdapter.addRow(message);
            }

            @Override
            public void onDoneWithError(BError error) {
              showAlertToast("Error while sending message.");
            }
        });

        if (clearEditText)
            etMessage.getText().clear();
    }

    /** Send text message logic.*/
    private void sendTextMessageWithStatus(){
        sendTextMessageWithStatus(etMessage.getText().toString(), true);
    }

    /** Send text message
     * @param text the text to send.
     * @param clearEditText if true clear the message edit text.*/
    private void sendTextMessageWithStatus(String text, boolean clearEditText){
        if (DEBUG) Log.v(TAG, "sendTextMessage, Text: " + text + ", Clear: " + String.valueOf(clearEditText));

        if (StringUtils.isEmpty(text) || StringUtils.isBlank(text))
        {
            showAlertToast("Cant send empty message!");
            return;
        }

        // Clear all white space from message
        text = text.trim();

        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithText(text, thread.getId(),new RepetitiveCompletionListenerWithMainTaskAndError<BMessage, BMessage, BError>() {
            @Override
            public boolean onMainFinised(BMessage message, BError error) {
                if (DEBUG) Log.v(TAG, "onMainFinished, Status: " + message.getStatusOrNull());
                messagesListAdapter.addRow(message);
                return false;
            }

            @Override
            public boolean onItem(BMessage message) {
                if (DEBUG) Log.v(TAG, "onItem, Status: " + message.getStatusOrNull());
                    messagesListAdapter.addRow(message);
                return false;
            }

            @Override
            public void onDone() {
            }


            @Override
            public void onItemError(BMessage message, BError error) {
                showAlertToast("Error while sending message.");
                messagesListAdapter.addRow(message);
                /*FIXME todo handle error by showing indicator on the message in the list.*/
            }
        });

        if (clearEditText)
            etMessage.getText().clear();
    }

    /** Send an image message.
     * @param filePath the path to the image file that need to be sent.*/
    private void sendImageMessage(String filePath){
        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithImage(
                filePath, thread.getId(), new CompletionListenerWithData<BMessage>() {
                    @Override
                    public void onDone(BMessage message) {
                        if (DEBUG) Log.v(TAG, "Image is sent");
                        updateCard("Status...", 100);
                        dismissCardWithSmallDelay();

                        // If the event manager is not listening to the current thread we will add the message to the list from here.
                        // This could happen when the app is authenticating after it was killed by the system.
                        if (!EventManager.getInstance().isListeningToIcomingMessages(thread.getEntityID()))
                            messagesListAdapter.addRow(message);
                    }

                    @Override
                    public void onDoneWithError(BError error) {
                        dismissCardWithSmallDelay();
                        showAlertToast("Image could not been sent. " + error.message);
                    }
                });
    }


    /** Show the message option popup, From here the user can send images and location messages.*/
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

    /** Check the intent if carries some data that received from another app to share on this chat.*/
    private void checkIfWantToShare(Intent intent){
        if (DEBUG) Log.v(TAG, "checkIfWantToShare");

        if (intent.getExtras().containsKey(SHARED_FILE_URI))
        {
            showCard("Uploading image...", 50);

            String path = Utils.getRealPathFromURI(this, (Uri) intent.getExtras().get(SHARED_FILE_URI));
            if (DEBUG) Log.d(TAG, "Path from uri: " + path);

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_FILE_URI);

            sendImageMessage(path);
        }
        else if (intent.getExtras().containsKey(SHARED_TEXT))
        {
            String text =intent.getExtras().getString(SHARED_TEXT);

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_TEXT);

            sendTextMessageWithStatus(text, false);
        }
    }




    /* Implement listeners.*/
    @Override
    public void onClick(View v) {
        int id= v.getId();

        if (id == R.id.chat_sdk_btn_chat_send_message) {
            sendTextMessageWithStatus();
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

            File file, dir =Utils.FileSaver.getAlbumStorageDir(Utils.FileSaver.IMAGE_DIR_NAME);
            if(dir.exists())
            {

                file = new File(dir, DaoCore.generateEntity() + ".jpg");
                capturePhotoPath = file.getPath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            }

            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE);
        }
        else  if (id == R.id.chat_sdk_btn_location) {
            dismissOption();
            Intent intent = new Intent(ChatActivity.this, LocationActivity.class);
            startActivityForResult(intent, PICK_LOCATION);
        }
    }

    /* Send a text message when the done button is pressed on the keyboard.*/
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND)
            sendTextMessageWithStatus();

        return false;
    }

    /* show the option popup when the menu key is pressed.*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_MENU:
                showOptionPopup();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (data.containsKey(FROM_PUSH))
        {
            if (DEBUG) Log.d(TAG, "onBackPressed, From Push");
            data.remove(FROM_PUSH);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }
        super.onBackPressed();
    }
}
