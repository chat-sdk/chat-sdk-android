/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.activities.abstracted;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.braunster.chatsdk.Utils.asynctask.MakeThreadImage;
import com.braunster.chatsdk.Utils.helper.ChatSDKChatHelper;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.activities.ChatSDKBaseActivity;
import com.braunster.chatsdk.activities.ChatSDKBaseThreadActivity;
import com.braunster.chatsdk.activities.ChatSDKPickFriendsActivity;
import com.braunster.chatsdk.adapter.ChatSDKMessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.BThreadEntity;
import com.braunster.chatsdk.fragments.ChatSDKContactsFragment;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.network.events.MessageEventListener;
import com.braunster.chatsdk.object.Batcher;
import com.braunster.chatsdk.thread.ChatSDKImageMessagesThreadPool;
import com.braunster.chatsdk.view.ChatMessageBoxView;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


/**
 * Created by itzik on 6/8/2014.
 */
public abstract class ChatSDKAbstractChatActivity extends ChatSDKBaseActivity implements  AbsListView.OnScrollListener{

    private static final String TAG = ChatSDKAbstractChatActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatActivity;

    public static final int ADD_USERS = 103;
    public static final int SHOW_DETAILS = 200;

    public static final String ACTION_CHAT_CLOSED = "braunster.chat.action.chat_closed";

    /** The message event listener tag, This is used so we could find and remove the listener from the EventManager.
     * It will be removed when activity is paused. or when opened again for new thread.*/
    public static final String MessageListenerTAG = TAG + "MessageTAG";
    public static final String ThreadListenerTAG = TAG + "threadTAG";

    /** The key to get the thread long id.*/
    public static final String THREAD_ID = ChatSDKBaseThreadActivity.THREAD_ID;

    public static final String THREAD_ENTITY_ID = "Thread_Entity_ID";

    public static final String LIST_POS = "list_pos";
    public static final String FROM_PUSH = "from_push";
    public static final String MSG_TIMESTAMP = "timestamp";

    /** Pass true if you want slide down animation for this activity exit. */
    public static final String ANIMATE_EXIT = "animate_exit";

    /** The key to get the path of the last captured image path in case the activity is destroyed while capturing.*/
    public static final String CAPTURED_PHOTO_PATH = "captured_photo_path";

    protected  View actionBarView;

    protected ChatSDKChatHelper chatSDKChatHelper;

    protected  ChatMessageBoxView messageBoxView;
    protected  ListView listMessages;
    protected ChatSDKMessagesListAdapter messagesListAdapter;
    protected  BThread thread;

    protected  ProgressBar progressBar;
    protected  int listPos = -1;

    protected  Bundle data;

    private boolean queueStopped = false;

    /** If set to false in onCreate the menu items wont be inflated in the menu.
     * This can be useful if you want to customize the action bar.*/
    protected boolean inflateMenuItems = true;

    /** Save the scroll state of the messages list.*/
    protected  boolean scrolling = false;

    private boolean created = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setEnableCardToast(true);
        super.onCreate(savedInstanceState);

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
    protected Bitmap getTaskDescriptionBitmap() {
        return super.getTaskDescriptionBitmap();
    }

    protected ActionBar readyActionBarToCustomView(){
        ActionBar ab = getActionBar();
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
        if (DEBUG) Timber.d("initActionBar");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ActionBar ab = readyActionBarToCustomView();

            /*http://stackoverflow.com/questions/16026818/actionbar-custom-view-with-centered-imageview-action-items-on-sides*/

            actionBarView = inflateActionBarView(R.layout.chat_sdk_actionbar_chat_activity);

            boolean changed;

            TextView txtName = (TextView) actionBarView.findViewById(R.id.chat_sdk_name);
            changed = setThreadName(txtName);


            final CircleImageView circleImageView = (CircleImageView) actionBarView.findViewById(R.id.chat_sdk_circle_image);
            final ImageView imageView = (ImageView) actionBarView.findViewById(R.id.chat_sdk_round_corner_image);

            final View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            };

            changed =  setThreadImage(circleImageView, imageView, onClickListener) || changed;

            if (changed)
                ab.setCustomView(actionBarView);
        }
    }

    /**
     *  Setting the thread name in the action bar.
     * */
    protected boolean setThreadName(TextView txtName){
        String displayName = thread.displayName();

        if (StringUtils.isBlank(displayName) )
            return false;

        if (txtName.getText() == null || !displayName.equals(txtName.getText().toString()))
        {
            // Set the title of the screen, This is used for the label in the screen overview on lollipop devices.
            setTitle(displayName);
            
            txtName.setText(displayName);
            txtName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToast(((TextView) v).getText().toString());
                }
            });

            return true;
        }

        return false;
    }

    /**
     * Setting the thread image in the action bar.
     * */
    protected boolean setThreadImage(final CircleImageView circleImageView, final ImageView imageView, final View.OnClickListener onClickListener){
        final String imageUrl = thread.threadImageUrl();

        if (circleImageView.getTag() == null || StringUtils.isEmpty(imageUrl) || !imageUrl.equals(circleImageView.getTag()))
        {
            if (StringUtils.isEmpty(imageUrl))
                setRoundCornerDefault(circleImageView, imageView, onClickListener);
            else
            {
                // Check if there is a image saved in the cahce for this thread.
//                if (thread.getType()==BThread.Type.Private)
                    if (imageUrl.split(",").length > 1)
                    {
                        int size = getResources().getDimensionPixelSize(R.dimen.chat_sdk_chat_action_barcircle_image_view_size);
                        new MakeThreadImage(imageUrl.split(","), size, size, thread.getEntityID(), circleImageView);
                        circleImageView.setOnClickListener(onClickListener);
                        circleImageView.setVisibility(View.VISIBLE);
                        circleImageView.bringToFront();
                    }
                    else
                        VolleyUtils.getImageLoader().get(imageUrl, new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                if (response.getBitmap() != null) {
                                    circleImageView.setTag(imageUrl);
                                    circleImageView.setVisibility(View.INVISIBLE);
                                    imageView.setVisibility(View.INVISIBLE);
                                    circleImageView.setImageBitmap(response.getBitmap());
                                    circleImageView.setVisibility(View.VISIBLE);

                                    // setting the task description again so the thread image will be seeing.
                                    setTaskDescription(response.getBitmap(), getTaskDescriptionLabel(), getTaskDescriptionColor());
                                    
                                    circleImageView.setOnClickListener(onClickListener);

                                    circleImageView.bringToFront();
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                setRoundCornerDefault(circleImageView, imageView, onClickListener);
                            }


                        });
            }

            return true;
        }

        return false;
    }

    /**
     * Set the default image for this thread in the action bar.
     * */
    protected void setRoundCornerDefault(CircleImageView circleImageView, ImageView roundedCornerImageView, View.OnClickListener onClickListener){
        circleImageView.setVisibility(View.INVISIBLE);
        roundedCornerImageView.setVisibility(View.INVISIBLE);

        if (thread.getTypeSafely() == BThread.Type.Public)
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

        final SwipeRefreshLayout mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.ptr_layout);
        
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DEBUG) Timber.d("onRefreshStarted");

                BNetworkManager.sharedManager().getNetworkAdapter().loadMoreMessagesForThread(thread)
                        .done(new DoneCallback<List<BMessage>>() {
                            @Override
                            public void onDone(List<BMessage> bMessages) {
                                if (DEBUG)
                                    Timber.d("New messages are loaded, Amount: %s", (bMessages == null ? "No messages" : bMessages.size()));

                                if (bMessages.size() < 2)
                                    showToast(getString(R.string.chat_activity_no_more_messages_to_load_toast));
                                else {
                                    // Saving the position in the list so we could back to it after the update.
                                    chatSDKChatHelper.loadMessages(true, false, -1, messagesListAdapter.getCount() + bMessages.size());
                                }

                                mSwipeRefresh.setRefreshing(false);
                            }
                        })
                        .fail(new FailCallback<Void>() {
                            @Override
                            public void onFail(Void aVoid) {
                                mSwipeRefresh.setRefreshing(false);
                            }
                        });
            }
        });

        listMessages = (ListView) findViewById(R.id.list_chat);

        chatSDKChatHelper.setListMessages(listMessages);

        if (messagesListAdapter == null)
            messagesListAdapter = new ChatSDKMessagesListAdapter(ChatSDKAbstractChatActivity.this, BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getId());

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
    protected void onStart() {
        super.onStart();

        if (thread != null && thread.getType() == BThread.Type.Public)
        {
            getNetworkAdapter().addUsersToThread(thread, getNetworkAdapter().currentUserModel());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Timber.v("onResume");

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
                            hideSoftKeyboard(ChatSDKAbstractChatActivity.this);
                    }
                }, 300);

                return false;
            }
        }, R.id.chat_sdk_btn_chat_send_message, R.id.chat_sdk_btn_options);

        final MessageEventListener messageEventListener = new MessageEventListener(MessageListenerTAG + thread.getId(), thread.getEntityID()) {
            @Override
            public boolean onMessageReceived(BMessage message) {
                if (DEBUG) Timber.v("onMessageReceived, EntityID: %s", message.getEntityID());

                // Check that the message is relevant to the current thread.
                if (!message.getThread().getEntityID().equals(thread.getEntityID()) || message.getThreadDaoId() != thread.getId().intValue()) {
                    return false;
                }

                //Set as read.
                chatSDKChatHelper.markAsRead(message);

                boolean isAdded = messagesListAdapter.addRow(message);

                // Check if the message from the current user, If so return so we wont vibrate for the user messages.
                if (message.getBUserSender().getEntityID().equals(
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getEntityID()) )
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
                if (DEBUG) Timber.v("triggered, Users: %s", list.size());
                updateChat();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {

                getNetworkAdapter().getEventManager().messagesOn(thread.getEntityID(), null);
                getNetworkAdapter().getEventManager().threadUsersAddedOn(thread.getEntityID());

                // Making sure that this thread is handled by the EventManager so the user will get all the chat updates as he enters.
                // If we are not listening then we add it the the manager.
                if (!getNetworkAdapter().getEventManager().isListeningToThread(thread.getEntityID()))
                {
                    getNetworkAdapter().getEventManager().threadOn(thread.getEntityID(), null);
                }

                NotificationUtils.cancelNotification(ChatSDKAbstractChatActivity.this, BDefines.MESSAGE_NOTIFICATION_ID);

                chatSDKChatHelper.integrateUI(messageBoxView, messagesListAdapter, listMessages, progressBar);

                listMessages.setOnScrollListener(ChatSDKAbstractChatActivity.this);

                // Removing the last listener just to be sure we wont receive duplicates notifications.
                getNetworkAdapter().getEventManager().removeEventByTag(MessageListenerTAG + thread.getId());
                getNetworkAdapter().getEventManager().addEvent(messageEventListener);

                // Removing the last listener just to be sure we wont receive duplicates notifications.
                getNetworkAdapter().getEventManager().removeEventByTag(ThreadListenerTAG + thread.getId());
                getNetworkAdapter().getEventManager().addEvent(threadBatchedEvent);
            }
        }).start();


        // If the activity is just been created we load regularly, else we load and retain position
        if (!created)
            chatSDKChatHelper.loadMessagesAndRetainCurrentPos();
        else
        {
            chatSDKChatHelper.loadMessages(listPos);
        }

        created = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        getNetworkAdapter().getEventManager().removeEventByTag(MessageListenerTAG + thread.getId());
        getNetworkAdapter().getEventManager().removeEventByTag(ThreadListenerTAG + thread.getId());
    }

    /**
     * Sending a broadcast that the chat was closed, Only if there were new messages on this chat.
     * This is used for example to update the thread list that messages has been read.
     * */
    @Override
    protected void onStop() {
        super.onStop();

        if (chatSDKChatHelper.getReadCount() > 0)
            sendBroadcast(new Intent(ACTION_CHAT_CLOSED));

        if (thread != null && thread.getType() == BThread.Type.Public)
        {
            getNetworkAdapter().removeUsersFromThread(thread, getNetworkAdapter().currentUserModel());
        }
    }

    /**
     * Not used, There is a piece of code here that could be used to clean all images that was loaded for this chat from cache.
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Clear all the images that was loaded for this chat from the cache. Currently not used but may be useful some day or to someone.
        for (String key : messagesListAdapter.getCacheKeys())
            VolleyUtils.getBitmapCache().remove(key);*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) Timber.v("onNewIntent");

        if ( !getThread(intent.getExtras()) )
            return;

        created = true;

        chatSDKChatHelper.setThread(thread);

        if (messagesListAdapter != null)
            messagesListAdapter.clear();

        initActionBar();

        chatSDKChatHelper.checkIfWantToShare(intent);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) Timber.v("onActivityResult");

        chatSDKChatHelper.handleResult(requestCode, resultCode, data);


        if (requestCode == ADD_USERS)
        {
            if (DEBUG) Timber.d("ADD_USER_RETURN");
            if (resultCode == RESULT_OK)
            {
                updateChat();
            }
        }
        else if (requestCode == SHOW_DETAILS)
        {
            if (DEBUG) Timber.d("SHOW_DETAILS");
            if (resultCode == RESULT_OK)
            {
                // Updating the selected chat id.
                if (data != null && data.getExtras()!= null && data.getExtras().containsKey(THREAD_ID))
                {
                    if ( !getThread(data.getExtras()) )
                        return;

                    created = true;

                    chatSDKChatHelper.setThread(thread);

                    if (messagesListAdapter != null)
                        messagesListAdapter.clear();

                    initActionBar();
                }
                else
                    updateChat();


            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!inflateMenuItems)
            return super.onCreateOptionsMenu(menu);

        // Adding the add user option only if group chat is enabled.
        if (BDefines.Options.GroupEnabled)
        {
            MenuItem item =
                    menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, getString(R.string.chat_activity_show_users_item_text));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            item.setIcon(R.drawable.ic_plus);
        }

        if (BDefines.Options.ThreadDetailsEnabled)
        {

            MenuItem itemThreadUsers =
                menu.add(Menu.NONE, R.id.action_chat_sdk_thread_details, 10, getString(R.string.chat_activity_show_thread_details));
            
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

        if (id == R.id.action_chat_sdk_add)
        {
            startAddUsersActivity();
        }
        else if (id == R.id.action_chat_sdk_show)
        {
            showUsersDialog();
        }
        else if (id == R.id.action_chat_sdk_thread_details)
        {
            openThreadDetailsActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open the add users activity, Here you can see your contact list and add users to this chat.
     * The default add users activity will remove contacts that is already in this chat.
     * */
    protected void startAddUsersActivity(){
        // Showing the pick friends activity.
        Intent intent = new Intent(this, chatSDKUiHelper.pickFriendsActivity);
        intent.putExtra(ChatSDKPickFriendsActivity.MODE, ChatSDKPickFriendsActivity.MODE_ADD_TO_CONVERSATION);
        intent.putExtra(ChatSDKBaseThreadActivity.THREAD_ID, thread.getId());
        intent.putExtra(ANIMATE_EXIT, true);

        startActivityForResult(intent, ADD_USERS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Show a dialog containing all the users in this chat. */
    protected void showUsersDialog(){
        ChatSDKContactsFragment contactsFragment = ChatSDKContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), "Thread Users:", true);
        contactsFragment.show(getFragmentManager(), "Contacts");
    }

    /**
     * Open the thread details activity, Admin user can change thread name an image there.
     * */
    protected void openThreadDetailsActivity(){
        // Showing the pick friends activity.
        Intent intent = new Intent(this, chatSDKUiHelper.threadDetailsActivity);
        intent.putExtra(THREAD_ID, thread.getId());
        intent.putExtra(ANIMATE_EXIT, true);

        startActivityForResult(intent, SHOW_DETAILS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Not used, show a dialog containing all the user in this chat with custom title and option to show or hide headers.
     *
     * @param withHeaders if true the list will contain its headers for users.
     * @param title the title of the dialog.
     *
     * */
    protected void showUsersDialog(String title, boolean withHeaders){
        ChatSDKContactsFragment contactsFragment = ChatSDKContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), title, true);
        contactsFragment.setWithHeaders(withHeaders);
        contactsFragment.setTitle(title);
        contactsFragment.show(getFragmentManager(), "Contacts");
    }

    @Override
    public void onAuthenticated() {
        super.onAuthenticated();
        if (DEBUG) Timber.v("onAuthenticated");
        chatSDKChatHelper.loadMessagesAndRetainCurrentPos();
    }


    /**
     * Get the current thread from the bundle data, Thread could be in the getIntent or in onNewIntent.
     * */
    private boolean getThread(Bundle bundle){

        if (bundle != null && (bundle.containsKey(THREAD_ID) || bundle.containsKey(THREAD_ENTITY_ID)) )
        {
            this.data = bundle;
        }
        else
        {
            if ( getIntent() == null || getIntent().getExtras() == null)
            {
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
            if (DEBUG) Timber.e("Thread id is empty");
            finish();
            return false;
        }

        if (thread == null)
        {
            if (DEBUG) Timber.e("No Thread found for given ID.");
            finish();
            return false;
        }

        return true;
    }

    /**
     * Update chat current thread using the {@link ChatSDKAbstractChatActivity#data} bundle saved.
     * Also calling the option menu to update it self. Used for showing the thread users icon if thread users amount is bigger then 2.
     * Finally update the action bar for thread image and name, The update will occur only if needed so free to call.
     *  */
    private void updateChat(){
        getThread(this.data);
        invalidateOptionsMenu();
        initActionBar();
    }

    /**
     * show the option popup when the menu key is pressed.
     * */
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

    /**
     * If the chat was open from a push notification we won't pass the backPress to the system instead we will navigate him to the main activity.
     * */
    @Override
    public void onBackPressed() {
        // If the message was opend from a notification back button should lead us to the main activity.
        if (data.containsKey(FROM_PUSH))
        {
            if (DEBUG) Timber.d("onBackPressed, From Push");
            data.remove(FROM_PUSH);

            chatSDKUiHelper.startMainActivity();
            return;
        }
        super.onBackPressed();
    }

    /** Used for pausing the volley image loader while the user is scrolling so the scroll will be smooth.*/
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        scrolling = scrollState != SCROLL_STATE_IDLE;

        // Pause disk cache access to ensure smoother scrolling
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            VolleyUtils.getRequestQueue().stop();
            ChatSDKImageMessagesThreadPool.getInstance().getThreadPool().pause();
            queueStopped = true;
        }

        // Pause disk cache access to ensure smoother scrolling
        if (queueStopped && !scrolling)
        {
            ChatSDKImageMessagesThreadPool.getInstance().getThreadPool().resume();
            VolleyUtils.getRequestQueue().start();
            queueStopped = false;
        }

        messagesListAdapter.setScrolling(scrolling);
    }

    /**
     *  Not used.
     * */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * Get message list adapter that is used for the messages list.
     * */
    protected ChatSDKMessagesListAdapter getMessagesListAdapter() {
        return messagesListAdapter;
    }

    private Handler handler = new Handler();
}
