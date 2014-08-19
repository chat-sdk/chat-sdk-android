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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.NotificationUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.sorter.MessageSorter;
import com.braunster.chatsdk.Utils.volley.RoundedCornerNetworkImageView;
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
import com.braunster.chatsdk.network.events.ThreadEventListener;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
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
public class ChatActivity extends BaseActivity implements View.OnKeyListener, View.OnClickListener, TextView.OnEditorActionListener, AbsListView.OnScrollListener{

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
    public static final String ThreadListenerTAG = TAG + "threadTAG";

    /** The key to get the thread long id.*/
    public static final String THREAD_ID = "Thread_ID";
    public static final String THREAD_ENTITY_ID = "Thread_Entity_ID";

    public static final String LIST_POS = "list_pos";
    public static final String FROM_PUSH = "from_push";

    /** The key to get the path of the last captured image path in case the activity is destroyed while capturing.*/
    public static final String CAPTURED_PHOTO_PATH = "captured_photo_path";

    /** The key to get the shared file uri. This is used when the activity is opened to share and image or a file with the chat users.
     *  The Activity will be open from the ContactsFragment that will be placed inside the ShareWithContactActivity. */
    public static final String SHARED_FILE_URI = "share_file_uri";

    /** The key to get shared text, this is used when the activity is open to share text with the chat user.
     *  The Activity will be open from the ContactsFragment that will be placed inside the ShareWithContactActivity. */
    public static final String SHARED_TEXT = "shared_text";

    private TextView btnSend;
    private ImageButton btnOptions;
    private EditText etMessage;
    private ListView listMessages;
    private MessagesListAdapter messagesListAdapter;
    private BThread thread;
    private PopupWindow optionPopup;
    private PullToRefreshLayout mPullToRefreshLayout;
    private ProgressBar progressBar;
    private int listPos = -1;

    private String capturePhotoPath = "";

    private Bundle data;

    /** Save the scroll state of the messages list.*/
    private boolean scrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setEnableCardToast(true);
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.v(TAG, "onCreate");

        setContentView(R.layout.chat_sdk_activity_chat);

        enableCheckOnlineOnResumed(true);

        if ( !getThread(savedInstanceState) )
            return;

        if (savedInstanceState != null)
        {
            if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH))
            {
                capturePhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH);
                savedInstanceState.remove(CAPTURED_PHOTO_PATH);
            }

            listPos = savedInstanceState.getInt(LIST_POS, -1);
            savedInstanceState.remove(LIST_POS);
        }

        initViews();

        initToast();

        initActionBar();
    }

    private void initActionBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getSupportActionBar();
            ab.setDisplayShowHomeEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayShowCustomEnabled(true);

            /*http://stackoverflow.com/questions/16026818/actionbar-custom-view-with-centered-imageview-action-items-on-sides*/

            // Inflate the custom view
            LayoutInflater inflater = LayoutInflater.from(this);
            View header = inflater.inflate( R.layout.chat_sdk_actionbar_chat_activity, null );

            TextView txtName = (TextView) header.findViewById(R.id.chat_sdk_name);

            boolean changed = false;

            String displayName = thread.displayName();
            if (txtName.getText() == null || !displayName.equals(txtName.getText().toString()))
            {
                txtName.setText(thread.displayName());
                txtName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showToast(((TextView) v).getText().toString());
                    }
                });

                changed = true;
            }

            final String imageUrl = thread.threadImageUrl();

            final CircleImageView circleImageView = (CircleImageView) header.findViewById(R.id.chat_sdk_circle_image);
            final RoundedCornerNetworkImageView roundedCornerImageView = (RoundedCornerNetworkImageView) header.findViewById(R.id.chat_sdk_round_corner_image);

            if (circleImageView.getTag() == null || !imageUrl.equals(circleImageView.getTag()))
            {
                final View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                };

                VolleyUtills.getImageLoader().get(thread.threadImageUrl(), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (response.getBitmap() != null)
                        {
                            circleImageView.setTag(imageUrl);
                            circleImageView.setVisibility(View.INVISIBLE);
                            roundedCornerImageView.setVisibility(View.INVISIBLE);
                            circleImageView.setImageBitmap(response.getBitmap());
                            circleImageView.setVisibility(View.VISIBLE);

                            circleImageView.setOnClickListener(onClickListener);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setRoundCornerDefault();
                    }

                    private void setRoundCornerDefault(){
                        circleImageView.setVisibility(View.INVISIBLE);
                        roundedCornerImageView.setVisibility(View.INVISIBLE);

                        if (thread.getType() == BThread.Type.Public)
                            roundedCornerImageView.setImageResource(R.drawable.ic_users);
                        else if (thread.getUsers().size() < 3)
                            roundedCornerImageView.setImageResource(R.drawable.ic_profile);
                        else
                            roundedCornerImageView.setImageResource(R.drawable.ic_users);

                        roundedCornerImageView.setVisibility(View.VISIBLE);

                        roundedCornerImageView.setOnClickListener(onClickListener);
                    }
                });

                changed = true;
            }

            if (changed)
                ab.setCustomView(header);
        }
    }

    private void initViews(){
        btnSend = (TextView) findViewById(R.id.chat_sdk_btn_chat_send_message);
        btnOptions = (ImageButton) findViewById(R.id.chat_sdk_btn_options);
        etMessage = (EditText) findViewById(R.id.chat_sdk_et_message_to_send);
        progressBar = (ProgressBar) findViewById(R.id.chat_sdk_progressbar);

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
                                            loadMessages(true, true, -2);
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

        listMessages = (ListView) findViewById(R.id.list_chat);

        messagesListAdapter = new MessagesListAdapter(ChatActivity.this, BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getId());
        listMessages.setAdapter(messagesListAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (thread != null)
            outState.putLong(THREAD_ID, thread.getId());

        if (StringUtils.isNotEmpty(capturePhotoPath))
            outState.putString(CAPTURED_PHOTO_PATH, capturePhotoPath);

        outState.putInt(LIST_POS, listMessages.getFirstVisiblePosition());
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventManager.getInstance().removeEventByTag(MessageListenerTAG + thread.getId());
        EventManager.getInstance().removeEventByTag(ThreadListenerTAG + thread.getId());

        for (String key : messagesListAdapter.getCacheKeys())
            VolleyUtills.getBitmapCache().remove(key);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) Log.v(TAG, "onNewIntent");

        if ( !getThread(intent.getExtras()) )
            return;

        created = true;

        if (messagesListAdapter != null)
            messagesListAdapter.clear();

        initActionBar();

        checkIfWantToShare(intent);
    }

    private boolean created = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume");

        if ( !getThread(data) )
            return;

        // Set up the UI to dismiss keyboard on touch event, Option and Send buttons are not included.
        // If list is scrolling we ignoring the touch event.
        setupTouchUIToDismissKeyboard(findViewById(R.id.chat_sdk_root_view), new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Using small delay for better accuracy in catching the scrolls.
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!scrolling)
                            hideSoftKeyboard(ChatActivity.this);
                    }
                }, 300);

                return false;
            }
        }, R.id.chat_sdk_btn_chat_send_message, R.id.chat_sdk_btn_options);

        final MessageEventListener messageEventListener = new MessageEventListener(MessageListenerTAG + thread.getId(), thread.getEntityID()) {
            @Override
            public boolean onMessageReceived(BMessage message) {
                if (DEBUG) Log.v(TAG, "onMessageReceived, EntityID: " + message.getEntityID());

                // Check that the message is relevant to the current thread.
                if (!message.getBThreadOwner().getEntityID().equals(thread.getEntityID()) || message.getOwnerThread() != thread.getId().intValue()) {
                    return false;
                }

                boolean isAdded = messagesListAdapter.addRow(message);

                // Check if the message from the current user, If so return so we wont vibrate for the user messages.
                if (message.getBUserSender().getEntityID().equals(
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID()) )
                {
                    if (message.getType() != BMessage.Type.TEXT && isAdded)
                    {
                        scrollListTo(-1);
                    }
                    return false;
                }

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

        final ThreadEventListener threadEventListener = new ThreadEventListener(ThreadListenerTAG + thread.getId(), thread.getEntityID()) {
            @Override
            public boolean onThreadDetailsChanged(String threadId) {
                return false;
            }

            @Override
            public boolean onUserAddedToThread(String threadId, String userId) {
                if (threadId.equals(thread.getEntityID()))
                    updateChat();
                return false;
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationUtils.cancelNotification(ChatActivity.this, PushUtils.MESSAGE_NOTIFICATION_ID);

                btnSend.setOnClickListener(ChatActivity.this);

                btnOptions.setOnClickListener(ChatActivity.this);

                etMessage.setOnEditorActionListener(ChatActivity.this);
                etMessage.setOnKeyListener(ChatActivity.this);

                listMessages.setOnScrollListener(ChatActivity.this);

                // Removing the last listener just to be sure we wont receive duplicates notifications.
                EventManager.getInstance().removeEventByTag(MessageListenerTAG + thread.getId());
                EventManager.getInstance().addMessageEvent(messageEventListener);

                // Removing the last listener just to be sure we wont receive duplicates notifications.
                EventManager.getInstance().removeEventByTag(ThreadListenerTAG + thread.getId());
                EventManager.getInstance().addThreadEvent(threadEventListener);
            }
        }).start();


        if (!created)
            loadMessagesAndRetainCurrentPos();
        else
        {
            loadMessages(listPos);
        }

        created = false;
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
            showProgressCard("Sending...");
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
                        dismissProgressCardWithSmallDelay();
                        return;
                    }

                    if (image != null) {
                        if (DEBUG) Log.i(TAG, "Image is not null");
                        sendImageMessage(image.getPath());
                    }
                    else {
                        if (DEBUG) Log.e(TAG, "Image is null");
                        dismissProgressCardWithSmallDelay();
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
                                dismissProgressCardWithSmallDelay();
//                                messagesListAdapter.addRow(bMessage);
                            }

                            @Override
                            public void onDoneWithError(BError error) {
                                dismissProgressCardWithSmallDelay();
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
                updateChat();
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

        /*Dont show users icon for a private thread with two users or less.*/
        if (thread.getType() == BThread.Type.Public || thread.getUsers().size() > 2)
        {
            MenuItem itemThreadUsers =
                    menu.add(Menu.NONE, R.id.action_chat_sdk_show, 10, "Show thread users.");
            itemThreadUsers.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            itemThreadUsers.setIcon(android.R.drawable.ic_menu_info_details);
        }

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
        loadMessagesAndRetainCurrentPos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy, CacheSize: " + VolleyUtills.getBitmapCache().size());
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
            showProgressCard("Sending...");

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

    /** Update chat current thread using the {@link com.braunster.chatsdk.activities.ChatActivity#data} bundle saved.
     *  Also calling the option menu to update it self. Used for showing the thread users icon if thread users amount is bigger then 2.
     *  Finally update the action bar for thread image and name, The update will occur only if needed so free to call.*/
    private void updateChat(){
        getThread(this.data);
        invalidateOptionsMenu();
        initActionBar();
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

    /** Send a text message when the done button is pressed on the keyboard.*/
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND)
            sendTextMessageWithStatus();


        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // if enter is pressed start calculating
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            int editTextLineCount = ((EditText) v).getLineCount();
            if (editTextLineCount >= getResources().getInteger(R.integer.chat_sdk_max_message_lines))
                return true;
        }
        return false;
    }

    /** show the option popup when the menu key is pressed.*/
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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE)
            scrolling = false;
        else scrolling = true;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    /*Message Loading and listView animation and scrolling*/
    /** Load messages from the database and saving the current position of the list.*/
    private void loadMessagesAndRetainCurrentPos(){
        loadMessages(true, false, 0);
    }

    private void loadMessages(final boolean retain, boolean hideListView, final int offsetOrPos){
        if (thread == null)
        {
            Log.e(TAG, "Thread is null");
            return;
        }

        if (hideListView)
        {
            listMessages.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else progressBar.setVisibility(View.INVISIBLE);

        ChatSDKThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {

                final int oldDataSize = messagesListAdapter.getCount();

                // Loading messages
                List<BMessage> messages = BNetworkManager.sharedManager().getNetworkAdapter().getMessagesForThreadForEntityID(thread.getId());

                // Sorting the message by date to make sure the list looks ok.
                Collections.sort(messages, new MessageSorter(MessageSorter.ORDER_TYPE_DESC));

                // Setting the new message to the adapter.
                final List<MessagesListAdapter.MessageListItem> list = messagesListAdapter.makeList(messages);

                if (list.size() == 0)
                {
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            listMessages.setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesListAdapter.setListData(list);

                        // restoring the old position after the load is done.
                        if (retain)
                        {
                            int newDataSize = messagesListAdapter.getCount();
                            final int index = listMessages.getFirstVisiblePosition() + newDataSize - oldDataSize + offsetOrPos;
                            View v = listMessages.getChildAt(0);
                            final int top = (v == null) ? -1 : v.getTop();

                            listMessages.post(new Runnable() {
                                @Override
                                public void run() {
                                    listMessages.setSelectionFromTop(index, top);

                                    if (listMessages.getVisibility() == View.INVISIBLE)
                                        animateListView();
                                }
                            });
                        }
                        else scrollListTo(offsetOrPos);
                    }
                });
            }
        });


    }

    private void loadMessagesAndScrollBottom(){
        loadMessages(false, true, - 1);
    }

    private void loadMessages(int scrollingPos){
        loadMessages(false, true, scrollingPos);
    }

    private void scrollListTo(final int pos) {
        listMessages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                if (pos== -1)
                    listMessages.setSelection(messagesListAdapter.getCount()-1);
                else
                    listMessages.setSelection(pos);

                if (listMessages.getVisibility() == View.INVISIBLE)
                    animateListView();
            }
        });
    }

    private void animateListView(){
        if (DEBUG) Log.v(TAG, "animateListView");

        listMessages.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_expand));
        listMessages.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listMessages.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        listMessages.getAnimation().start();
    }

    /*Message Sending*/
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
                        dismissProgressCardWithSmallDelay();

                        if(messagesListAdapter.addRow(message))
                            scrollListTo(-1);
                    }

                    @Override
                    public void onDoneWithError(BError error) {
                        dismissProgressCardWithSmallDelay();
                        showAlertToast("Image could not been sent. " + error.message);
                    }
                });
    }


}
