package com.braunster.chatsdk.activities.abstracted;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.NotificationUtils;
import com.braunster.chatsdk.Utils.helper.ChatSDKChatHelper;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.activities.ChatSDKBaseActivity;
import com.braunster.chatsdk.activities.ChatSDKPickFriendsActivity;
import com.braunster.chatsdk.adapter.ChatSDKMessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.ChatSDKContactsFragment;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.network.events.MessageEventListener;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.Batcher;
import com.braunster.chatsdk.parse.PushUtils;
import com.braunster.chatsdk.view.ChatMessageBoxView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


/**
 * Created by itzik on 6/8/2014.
 */
public abstract class ChatSDKAbstractChatActivity extends ChatSDKBaseActivity implements  AbsListView.OnScrollListener{

    private static final String TAG = ChatSDKAbstractChatActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatActivity;

    public static final int ADD_USERS = 103;

    public static final String ACTION_CHAT_CLOSED = "braunster.chat.action.chat_closed";

    /** The message event listener tag, This is used so we could find and remove the listener from the EventManager.
     * It will be removed when activity is paused. or when opened again for new thread.*/
    public static final String MessageListenerTAG = TAG + "MessageTAG";
    public static final String ThreadListenerTAG = TAG + "threadTAG";

    /** The key to get the thread long id.*/
    public static final String THREAD_ID = "Thread_ID";
    public static final String THREAD_ENTITY_ID = "Thread_Entity_ID";

    public static final String LIST_POS = "list_pos";
    public static final String FROM_PUSH = "from_push";

    /** The key to get the path of the last captured image path in case the activity is destroyed while capturing.*/
    public static final String CAPTURED_PHOTO_PATH = "captured_photo_path";

    protected  View actionBarView;

    protected ChatSDKChatHelper chatSDKChatHelper;

    protected  ChatMessageBoxView messageBoxView;
    protected  ListView listMessages;
    protected ChatSDKMessagesListAdapter messagesListAdapter;
    protected  BThread thread;

    protected  PullToRefreshLayout mPullToRefreshLayout;
    protected  ProgressBar progressBar;
    protected  int listPos = -1;

    protected  Bundle data;

    /** If set to false in onCreate the menu items wont be inflated in the menu.
     * This can be useful if you want to customize the action bar.*/
    protected boolean inflateMenuItems = true;

    /** Save the scroll state of the messages list.*/
    protected  boolean scrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setEnableCardToast(true);
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.v(TAG, "onCreate");

        enableCheckOnlineOnResumed(true);

        if ( !getThread(savedInstanceState) )
            return;

        chatSDKChatHelper = new ChatSDKChatHelper(this, thread, chatSDKUiHelper);
        chatSDKChatHelper.restoreSavedInstance(savedInstanceState);

        if (savedInstanceState != null)
        {
            listPos = savedInstanceState.getInt(LIST_POS, -1);
            savedInstanceState.remove(LIST_POS);
        }

        initActionBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    protected ActionBar readyActionBarToCustomView(){
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);

        return ab;
    }

    protected View inflateActionBarView(int resId){
        // Inflate the custom view
        if (actionBarView == null || actionBarView.getId() != resId) {
            LayoutInflater inflater = LayoutInflater.from(this);
            actionBarView = inflater.inflate(resId, null);
        }

        return actionBarView;
    }

    protected void initActionBar(){
        if (DEBUG) Log.d(TAG, "initActionBar");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ActionBar ab = readyActionBarToCustomView();

            /*http://stackoverflow.com/questions/16026818/actionbar-custom-view-with-centered-imageview-action-items-on-sides*/

            actionBarView = inflateActionBarView(R.layout.chat_sdk_actionbar_chat_activity);


            boolean changed = false;

            TextView txtName = (TextView) actionBarView.findViewById(R.id.chat_sdk_name);
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
            final CircleImageView circleImageView = (CircleImageView) actionBarView.findViewById(R.id.chat_sdk_circle_image);
            final ImageView roundedCornerImageView = (ImageView) actionBarView.findViewById(R.id.chat_sdk_round_corner_image);

            if (circleImageView.getTag() == null || StringUtils.isEmpty(imageUrl) || !imageUrl.equals(circleImageView.getTag()))
            {
                final View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startMainActivity();
                    }
                };

                if (StringUtils.isEmpty(imageUrl))
                    setRoundCornerDefault(circleImageView, roundedCornerImageView, onClickListener);
                else
                    VolleyUtils.getImageLoader().get(imageUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                circleImageView.setTag(imageUrl);
                                circleImageView.setVisibility(View.INVISIBLE);
                                roundedCornerImageView.setVisibility(View.INVISIBLE);
                                circleImageView.setImageBitmap(response.getBitmap());
                                circleImageView.setVisibility(View.VISIBLE);

                                circleImageView.setOnClickListener(onClickListener);

                                circleImageView.bringToFront();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            setRoundCornerDefault(circleImageView, roundedCornerImageView, onClickListener);
                        }


                    });

                changed = true;
            }

            if (changed)
                ab.setCustomView(actionBarView);
        }
    }

    protected void setRoundCornerDefault(CircleImageView circleImageView, ImageView roundedCornerImageView, View.OnClickListener onClickListener){
        circleImageView.setVisibility(View.INVISIBLE);
        roundedCornerImageView.setVisibility(View.INVISIBLE);

        if (thread.getType() == BThread.Type.Public)
            roundedCornerImageView.setImageResource(R.drawable.ic_users);
        else if (thread.getUsers().size() < 3)
            roundedCornerImageView.setImageResource(R.drawable.ic_profile);
        else
            roundedCornerImageView.setImageResource(R.drawable.ic_users);

        roundedCornerImageView.setVisibility(View.VISIBLE);

        roundedCornerImageView.bringToFront();

        roundedCornerImageView.setOnClickListener(onClickListener);
    }

    protected void initViews(){

        messageBoxView = (ChatMessageBoxView) findViewById(R.id.chat_sdk_message_box);
        messageBoxView.setAlertToast(chatSDKUiHelper.getAlertToast());

        chatSDKChatHelper.setMessageBoxView(messageBoxView);

        progressBar = (ProgressBar) findViewById(R.id.chat_sdk_progressbar);

        chatSDKChatHelper.setProgressBar(progressBar);

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
                                            chatSDKChatHelper.loadMessages(true, true, -1);
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

        chatSDKChatHelper.setListMessages(listMessages);

        if (messagesListAdapter == null)
            messagesListAdapter = new ChatSDKMessagesListAdapter(ChatSDKAbstractChatActivity.this, BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getId());

        listMessages.setAdapter(messagesListAdapter);
        chatSDKChatHelper.setMessagesListAdapter(messagesListAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (thread != null)
            outState.putLong(THREAD_ID, thread.getId());

        chatSDKChatHelper.onSavedInstanceBundle(outState);

        outState.putInt(LIST_POS, listMessages.getFirstVisiblePosition());
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventManager.getInstance().removeEventByTag(MessageListenerTAG + thread.getId());
        EventManager.getInstance().removeEventByTag(ThreadListenerTAG + thread.getId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) Log.v(TAG, "onNewIntent");

        if ( !getThread(intent.getExtras()) )
            return;

        created = true;

        chatSDKChatHelper.setThread(thread);

        if (messagesListAdapter != null)
            messagesListAdapter.clear();

        initActionBar();

        chatSDKChatHelper.checkIfWantToShare(intent);
    }

    private boolean created = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume");

        if ( !getThread(data) )
            return;

        EventManager.getInstance().handleMessages(thread.getEntityID());

        // Making sure that this thread is handled by the EventManager so the user will get all the chat updates as he enters.
        // If we are not listening then we add it the the manager.
        if (!EventManager.getInstance().isListeningToThread(thread.getEntityID()))
        {
            EventManager.getInstance().handleThread(thread.getEntityID());

        }

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
                            hideSoftKeyboard(ChatSDKAbstractChatActivity.this);
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

                //Set as read.
                chatSDKChatHelper.markAsRead(message);

                boolean isAdded = messagesListAdapter.addRow(message);

                // Check if the message from the current user, If so return so we wont vibrate for the user messages.
                if (message.getBUserSender().getEntityID().equals(
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID()) )
                {
                    if (isAdded)
                    {
                        chatSDKChatHelper.scrollListTo(-1, true);
                    }
                    return false;
                }

                // We check to see that this message is really a new one and not loaded from the server.
                if (System.currentTimeMillis() - message.getDate().getTime() < 1000*60)
                {
                    Vibrator v = (Vibrator) ChatSDKAbstractChatActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(BDefines.VIBRATION_DURATION);
                }

                return false;
            }
        };

        final BatchedEvent threadBatchedEvent = new BatchedEvent(ThreadListenerTAG + thread.getId(), thread.getEntityID(), Event.Type.ThreadEvent, handler);
        threadBatchedEvent.setBatchedAction(Event.Type.ThreadEvent, new Batcher.BatchedAction<String>() {
            @Override
            public void triggered(List<String> list) {
                if (DEBUG) Log.v(TAG, "triggered, Users: " + list.size());
                updateChat();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationUtils.cancelNotification(ChatSDKAbstractChatActivity.this, PushUtils.MESSAGE_NOTIFICATION_ID);

                chatSDKChatHelper.integrateUI(messageBoxView, messagesListAdapter, listMessages, progressBar);

                listMessages.setOnScrollListener(ChatSDKAbstractChatActivity.this);

                // Removing the last listener just to be sure we wont receive duplicates notifications.
                EventManager.getInstance().removeEventByTag(MessageListenerTAG + thread.getId());
                EventManager.getInstance().addMessageEvent(messageEventListener);

                // Removing the last listener just to be sure we wont receive duplicates notifications.
                EventManager.getInstance().removeEventByTag(ThreadListenerTAG + thread.getId());
                EventManager.getInstance().addAppEvent(threadBatchedEvent);
            }
        }).start();


        if (!created)
            chatSDKChatHelper.loadMessagesAndRetainCurrentPos();
        else
        {
            chatSDKChatHelper.loadMessages(listPos);
        }

        created = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) Log.v(TAG, "onActivityResult");

        int result = chatSDKChatHelper.handleResult(requestCode, resultCode, data);

        if (result == ChatSDKChatHelper.NOT_HANDLED)
            if (requestCode == ADD_USERS)
            {
                if (DEBUG) Log.d(TAG, "ADD_USER_RETURN");
                if (resultCode == RESULT_OK)
                {
                    updateChat();
                }
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!inflateMenuItems)
            return super.onCreateOptionsMenu(menu);

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

        if (!inflateMenuItems)
            return super.onOptionsItemSelected(item);

        // ASK what the add button do in this class
        if (id == R.id.action_chat_sdk_add)
        {
            startAddUsersActivity();
        }
        else if (id == R.id.action_chat_sdk_show)
        {
            showUsersDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void startAddUsersActivity(){
        // Showing the pick friends activity.
        Intent intent = new Intent(this, chatSDKUiHelper.pickFriendsActivity);
        intent.putExtra(ChatSDKPickFriendsActivity.MODE, ChatSDKPickFriendsActivity.MODE_ADD_TO_CONVERSATION);
        intent.putExtra(ChatSDKPickFriendsActivity.THREAD_ID, thread.getId());
        intent.putExtra(ChatSDKPickFriendsActivity.ANIMATE_EXIT, true);

        startActivityForResult(intent, ADD_USERS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    protected void showUsersDialog(){
        ChatSDKContactsFragment contactsFragment = ChatSDKContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), "Thread Users:", true);
        contactsFragment.show(getSupportFragmentManager(), "Contacts");
    }

    protected void showUsersDialog(String title, boolean withHeaders){
        ChatSDKContactsFragment contactsFragment = ChatSDKContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), "Thread Users:", true);
        contactsFragment.setWithHeaders(withHeaders);
        contactsFragment.setTitle(title);
        contactsFragment.show(getSupportFragmentManager(), "Contacts");
    }

    @Override
    public void onAuthenticated() {
        super.onAuthenticated();
        if (DEBUG) Log.v(TAG, "onAuthenticated");
        chatSDKChatHelper.loadMessagesAndRetainCurrentPos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatSDKChatHelper.getReadCount() > 0)
            sendBroadcast(new Intent(ACTION_CHAT_CLOSED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy, CacheSize: " + VolleyUtils.getBitmapCache().size());

        /* Clear all the images that was loaded for this chat from the cache.
        for (String key : messagesListAdapter.getCacheKeys())
            VolleyUtils.getBitmapCache().remove(key);*/
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

    /** Update chat current thread using the {@link ChatSDKAbstractChatActivity#data} bundle saved.
     *  Also calling the option menu to update it self. Used for showing the thread users icon if thread users amount is bigger then 2.
     *  Finally update the action bar for thread image and name, The update will occur only if needed so free to call.*/
    private void updateChat(){
        getThread(this.data);
        invalidateOptionsMenu();
        initActionBar();
    }

    /** show the option popup when the menu key is pressed.*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_MENU:
                messageBoxView.showOptionPopup();
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

            chatSDKUiHelper.startMainActivity();
            return;
        }
        super.onBackPressed();
    }

    private boolean queueStopped = false;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        scrolling = scrollState != SCROLL_STATE_IDLE;

        // Pause disk cache access to ensure smoother scrolling
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            VolleyUtils.getRequestQueue().stop();
            queueStopped = true;
        }

        // Pause disk cache access to ensure smoother scrolling
        if (queueStopped && !scrolling)
        {
            VolleyUtils.getRequestQueue().start();
            queueStopped = false;
        }

        messagesListAdapter.setScrolling(scrolling);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /*Message Loading and listView animation and scrolling*/


    public ChatSDKMessagesListAdapter getMessagesListAdapter() {
        return messagesListAdapter;
    }

    private Handler handler = new Handler();
}
