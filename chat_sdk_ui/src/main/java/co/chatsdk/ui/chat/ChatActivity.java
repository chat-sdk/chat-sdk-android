/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NM;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.sorter.MessageSorter;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.MessageUploadResult;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.ui.R;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.threads.ThreadImageBuilder;
import co.chatsdk.ui.utils.ToastHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.activities.SelectContactActivity;

import co.chatsdk.core.defines.Debug;

import co.chatsdk.ui.contacts.ContactsFragment;

import co.chatsdk.core.dao.DaoCore;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.chatsdk.ui.utils.Strings;

import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ChatActivity extends BaseActivity implements AbsListView.OnScrollListener {

    private static final boolean DEBUG = Debug.ChatActivity;

    public static final int ADD_USERS = 103;
    public static final int SHOW_DETAILS = 200;

    private enum ListPosition {
        Top, Current, Bottom
    }

    private static boolean enableTrace = false;

    public static final String ACTION_CHAT_CLOSED = "co.chatsdk.chat_closed";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * The key to get the thread long id.
     */
    public static final String LIST_POS = "list_pos";

    /**
     * Pass true if you want slide down animation for this activity exit.
     */
    public static final String ANIMATE_EXIT = "animate_exit";

    protected View actionBarView;

    protected TextInputView textInputView;
    protected ListView messageListView;
    protected MessagesListAdapter messageListAdapter;
    protected Thread thread;
    protected TextView typingTextView;

    private DisposableList disposableList = new DisposableList();
    private Disposable typingTimerDisposable;

    protected ProgressBar progressBar;
    protected int listPos = -1;

    protected Bundle bundle;

    /** Keeping track of the amount of messages that was read in this thread.*/
    private int readCount = 0;

    /**
     * If set to false in onCreate the menu items wont be inflated in the menu.
     * This can be useful if you want to customize the action bar.
     */
    protected boolean inflateMenuItems = true;

    /**
     * Save the scroll state of the messages list.
     */
    protected boolean scrolling = false;

    private PhotoSelector photoSelector = new PhotoSelector();
    private LocationSelector locationSelector = new LocationSelector();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();

        if (!updateThreadFromBundle(savedInstanceState))
            return;


        if (savedInstanceState != null) {
            listPos = savedInstanceState.getInt(LIST_POS, -1);
            savedInstanceState.remove(LIST_POS);
        }

        initActionBar();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        // If the activity is just been created we load regularly, else we load and retain position
        loadMessages(true, -1, ListPosition.Bottom);

        setChatState(TypingIndicatorHandler.State.active);

        if(enableTrace) {
            android.os.Debug.startMethodTracing("chat");
        }

    }

    @Override
    protected Bitmap getTaskDescriptionBitmap() {
        return super.getTaskDescriptionBitmap();
    }

    protected ActionBar readyActionBarToCustomView () {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);

        return ab;
    }


    protected void initActionBar () {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            final ActionBar ab = readyActionBarToCustomView();
            /*
             * http://stackoverflow.com/questions/16026818/actionbar-custom-view-with-centered-imageview-action-items-on-sides
             */

            actionBarView = getLayoutInflater().inflate(R.layout.chat_sdk_actionbar_chat_activity, null);

            actionBarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Defines.Options.ThreadDetailsEnabled) {
                        openThreadDetailsActivity();
                    }
                }
            });


            TextView textView = (TextView) actionBarView.findViewById(R.id.tvName);

            String displayName = Strings.nameForThread(thread);
            setTitle(displayName);
            textView.setText(displayName);

            typingTextView = (TextView) actionBarView.findViewById(R.id.tvTyping);

            final CircleImageView circleImageView = (CircleImageView) actionBarView.findViewById(R.id.ivAvatar);

            disposableList.add(ThreadImageBuilder.getBitmapForThread(this, thread).subscribe(new BiConsumer<Bitmap, Throwable>() {
                @Override
                public void accept(Bitmap bitmap, Throwable throwable) throws Exception {
                    circleImageView.setImageBitmap(bitmap);
                    circleImageView.setVisibility(View.VISIBLE);
                    ab.setCustomView(actionBarView);
                }
            }));

            ab.setCustomView(actionBarView);

        }
    }

    protected void initViews() {

        final Activity activity = this;

        setContentView(R.layout.chat_sdk_activity_chat);

        // Set up the message box - this is the box that sits above the keyboard
        textInputView = (TextInputView) findViewById(R.id.chat_sdk_message_box);

        textInputView.setListener(new TextInputView.Listener() {
            @Override
            public void onLocationPressed() {
                try {
                    locationSelector.startChooseLocationActivity(activity, new LocationSelector.Result() {
                        @Override
                        public void result (String filePath, LatLng latLng) {
                            sendLocationMessage(filePath, latLng);
                        }
                    });
                }
                catch (Exception e) {

                }
            }

            @Override
            public void onTakePhotoPressed() {

                verifyStoragePermissions(activity);

                photoSelector = new PhotoSelector();

                try {
                    photoSelector.startTakePhotoActivity(activity, new PhotoSelector.Result() {
                        public void result(String result) {
                            sendImageMessage(result);
                        }
                    });
                } catch (Exception e) {
                    ToastHelper.show(e.getLocalizedMessage());
                }
            }

            @Override
            public void onPickImagePressed() {

                verifyStoragePermissions(activity);

                photoSelector.startPickImageActivity(activity, new PhotoSelector.Result() {
                    @Override
                    public void result(String result) {
                        sendImageMessage(result);
                    }
                });
            }

            @Override
            public void onSendPressed(String text) {
                sendMessage(text, true);
            }

            @Override
            public void startTyping() {
                ChatActivity.this.startTyping();
            }

            @Override
            public void stopTyping() {
                ChatActivity.this.stopTyping(false);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.chat_sdk_progressbar);

        final SwipeRefreshLayout mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DEBUG) Timber.d("onRefreshStarted");

                List<MessageListItem> items = messageListAdapter.getMessageItems();
                Message firstMessage = null;
                if(items.size() > 0) {
                    firstMessage = items.get(0).message;
                }

                final View topView = messageListView.getChildAt(0);

                disposableList.add(NM.thread().loadMoreMessagesForThread(firstMessage, thread).subscribe(new BiConsumer<List<Message>, Throwable>() {
                    @Override
                    public void accept(final List<Message> messages, Throwable throwable) throws Exception {
                        if (throwable == null) {
                            if (messages.size() < 2) {
                                showToast(getString(R.string.chat_activity_no_more_messages_to_load_toast));
                            }
                            else {
                                for(Message m : messages) {
                                    messageListAdapter.addRow(m, false, false);
                                }
                                messageListAdapter.sortItemsAndNotify();
                                messageListView.setSelection(messages.size());
                            }
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                }));
            }
        });

        messageListView = (ListView) findViewById(R.id.list_chat);

        if (messageListAdapter == null) {
            messageListAdapter = new MessagesListAdapter(ChatActivity.this);
        }

        messageListView.setAdapter(messageListAdapter);
        messageListView.setOnScrollListener(this);
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Send text message
     *
     * @param text to send.
     * @param clearEditText if true clear the message edit text.
     */
    public void sendMessage(String text, boolean clearEditText) {
        if (DEBUG)
            Timber.v("sendTextMessage, Text: %s, Clear: %s", text, String.valueOf(clearEditText));

        if (StringUtils.isEmpty(text) || StringUtils.isBlank(text)) {
            return;
        }

        disposableList.add(NM.thread().sendMessageWithText(text.trim(), thread)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        ToastHelper.show(R.string.unable_to_send_message);
                    }
                }).subscribe());

        if (clearEditText && textInputView != null) {
            textInputView.clearText();
        }

        stopTyping(false);
    }

    public void sendLocationMessage(String snapshotPath, LatLng latLng) {

        disposableList.add(NM.thread().sendMessageWithLocation(snapshotPath, latLng, thread)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        ToastHelper.show(R.string.unable_to_send_location_message);
                    }
                }).subscribe());
    }

    /**
     * Send an messageImageView message.
     *
     * @param filePath the path to the messageImageView file that need to be sent.
     */
    public void sendImageMessage(final String filePath) {
        if (DEBUG) Timber.v("sendImageMessage, Path: %s", filePath);

        NM.thread().sendMessageWithImage(filePath, thread).subscribe(new Observer<MessageUploadResult>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposableList.add(d);
            }

            @Override
            public void onNext(@NonNull MessageUploadResult messageUploadResult) {
                messageListAdapter.addRow(messageUploadResult.message);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                ToastHelper.show(R.string.unable_to_send_image_message);
            }

            @Override
            public void onComplete() {
                messageListAdapter.notifyDataSetChanged();
            }
        });

        messageListAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(BaseInterfaceAdapter.THREAD_ENTITY_ID, thread.getEntityID());
        }

        // Save the list position
        outState.putInt(LIST_POS, messageListView.getFirstVisiblePosition());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());


        disposableList.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {

                        Message message = networkEvent.message;

                        // Check that the message is relevant to the current thread.
                        if (message.getThreadId() != thread.getId().intValue()) {
                            return;
                        }

                        //Set as read.
                        markAsRead(message);

                        boolean isAdded = messageListAdapter.addRow(message);

                        // Check if the message from the current user, If so return so we wont vibrate for the user messages.
                        if (message.getSender().isMe()) {
                            if (isAdded) {
                                scrollListTo(ListPosition.Bottom, messageListView.getLastVisiblePosition() > messageListAdapter.size() - 2);
                            }
                        }
                        else {
                            // If the user is near the bottom, then we scroll down when a message comes in
                            if(messageListView.getLastVisiblePosition() > messageListAdapter.size() - 5) {
                                scrollListTo(ListPosition.Bottom, true);
                            }
                            //Vibrator v = (Vibrator) ChatActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                            //v.vibrate(Defines.VIBRATION_DURATION);
                        }

                        message.setDelivered(true);
                        message.update();

                    }
                }));

        disposableList.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(
                        EventType.PrivateThreadAdded,
                        EventType.PrivateThreadRemoved,
                        EventType.PublicThreadAdded,
                        EventType.PublicThreadRemoved,
                        EventType.ThreadDetailsUpdated,
                        EventType.ThreadUsersChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {
                        messageListAdapter.notifyDataSetChanged();
                    }
                }));

        disposableList.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated)).subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {
                        messageListAdapter.notifyDataSetChanged();
                    }
                }));

        disposableList.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged)).subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                        if(networkEvent.thread.equals(thread)) {
                            if(networkEvent.text != null && networkEvent.text.length() == 0) {
                                networkEvent.text = getString(R.string.typing);
                            }
                            Timber.v(networkEvent.text);
                            typingTextView.setText(networkEvent.text);
                        }
                    }
                }));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Timber.v("onResume");

        if (!updateThreadFromBundle(bundle)) {
            return;
        }

        if (thread != null && thread.typeIs(ThreadType.Public)) {
            User currentUser = NM.currentUser();
            disposableList.add(NM.thread().addUsersToThread(thread, currentUser).subscribe());
        }

        markAsRead(thread.getMessages());

        // Set up the UI to dismiss keyboard on touch event, Option and Send buttons are not included.
        // If list is scrolling we ignoring the touch event.
        setupTouchUIToDismissKeyboard(findViewById(R.id.chat_sdk_root_view), new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Using small delay for better accuracy in catching the scrolls.
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!scrolling) {
                            hideSoftKeyboard(ChatActivity.this);
                            stopTyping(false);
                        }
                    }
                }, 300);

                return false;
            }
        }, R.id.chat_sdk_btn_chat_send_message, R.id.chat_sdk_btn_options);

        messageListAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Sending a broadcast that the chat was closed, Only if there were new messages on this chat.
     * This is used for example to update the thread list that messages has been read.
     */
    @Override
    protected void onStop() {
        super.onStop();

        disposableList.dispose();


        stopTyping(true);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());

        if (readCount > 0) {
            sendBroadcast(new Intent(ACTION_CHAT_CLOSED));
        }

        if (thread != null && thread.typeIs(ThreadType.Public))
        {
            NM.thread().removeUsersFromThread(thread, NM.currentUser());
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();

    }

    /**
     * Not used, There is a piece of code here that could be used to clean all images that was loaded for this chat from cache.
     */
    @Override
    protected void onDestroy() {
        if(enableTrace) {
            android.os.Debug.stopMethodTracing();
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) Timber.v("onNewIntent");

        if (!updateThreadFromBundle(intent.getExtras()))
            return;

        if (messageListAdapter != null)
            messageListAdapter.clear();

        initActionBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) Timber.v("onActivityResult");

        try {
            photoSelector.handleResult(this, requestCode, resultCode, data);
            locationSelector.handleResult(this, requestCode, resultCode, data);
        }
        catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().length() > 0) {
                ToastHelper.show(e.getLocalizedMessage());
            }
        }

        if (requestCode == ADD_USERS) {
            if (DEBUG) Timber.d("ADD_USER_RETURN");
            if (resultCode == RESULT_OK) {
                updateChat();
            }
        }
        else if (requestCode == SHOW_DETAILS) {

            if (DEBUG) Timber.d("SHOW_DETAILS");

            if (resultCode == RESULT_OK) {
                // Updating the selected chat id.
                if (data != null && data.getExtras() != null && data.getExtras().containsKey(BaseInterfaceAdapter.THREAD_ENTITY_ID)) {
                    if (!updateThreadFromBundle(data.getExtras())) {
                        return;
                    }

                    if (messageListAdapter != null) {
                        messageListAdapter.clear();
                    }

                    initActionBar();
                } else {
                    updateChat();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!inflateMenuItems)
            return super.onCreateOptionsMenu(menu);

        // Adding the add user option only if group chat is enabled.
        if (Defines.Options.GroupEnabled && thread.typeIs(ThreadType.Group)) {
            MenuItem item =
                    menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, getString(R.string.chat_activity_show_users_item_text));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            item.setIcon(R.drawable.ic_plus);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (!inflateMenuItems)
            return super.onOptionsItemSelected(item);

        if (id == R.id.action_chat_sdk_add) {
            startAddUsersActivity();
        }
        else if (id == R.id.action_chat_sdk_show) {
            showUsersDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open the add users activity, Here you can see your contact list and add users to this chat.
     * The default add users activity will remove contacts that is already in this chat.
     */
    protected void startAddUsersActivity() {
        Intent intent = new Intent(this, InterfaceManager.shared().a.getSelectContactActivity());
        intent.putExtra(SelectContactActivity.MODE, SelectContactActivity.MODE_ADD_TO_CONVERSATION);
        intent.putExtra(BaseInterfaceAdapter.THREAD_ENTITY_ID, thread.getEntityID());
        intent.putExtra(LIST_POS, listPos);
        intent.putExtra(ANIMATE_EXIT, true);

        startActivityForResult(intent, ADD_USERS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Show a dialog containing all the users in this chat.
     */
    protected void showUsersDialog() {
        ContactsFragment contactsFragment = ContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), "Thread Users:", true);
        contactsFragment.show(getSupportFragmentManager(), "Contacts");
    }

    /**
     * Open the thread details activity, Admin user can change thread name an messageImageView there.
     */
    protected void openThreadDetailsActivity() {
        // Showing the pick friends activity.
        Intent intent = new Intent(this, InterfaceManager.shared().a.getThreadDetailsActivity());
        intent.putExtra(BaseInterfaceAdapter.THREAD_ENTITY_ID, thread.getEntityID());
        intent.putExtra(LIST_POS, listPos);
        intent.putExtra(ANIMATE_EXIT, true);

        startActivityForResult(intent, SHOW_DETAILS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Get the current thread from the bundle bundle, CoreThread could be in the getIntent or in onNewIntent.
     */
    private boolean updateThreadFromBundle(Bundle bundle) {

        if (bundle != null && (bundle.containsKey(BaseInterfaceAdapter.THREAD_ENTITY_ID))) {
            this.bundle = bundle;
        }
        else {
            if (getIntent() == null || getIntent().getExtras() == null) {
                finish();
                return false;
            }
            this.bundle = getIntent().getExtras();
        }

        if (this.bundle.containsKey(BaseInterfaceAdapter.THREAD_ENTITY_ID)) {
            thread = StorageManager.shared().fetchThreadWithEntityID(this.bundle.getString(BaseInterfaceAdapter.THREAD_ENTITY_ID));
        }
        if (this.bundle.containsKey(LIST_POS)) {
            listPos = (Integer) this.bundle.get(LIST_POS);
            scrollListTo(ListPosition.Current, false);
        }

        if (thread == null) {
            if (DEBUG) Timber.e("No Thread found for given ID.");
            finish();
            return false;
        }

        return true;
    }

    /**
     * Update chat current thread using the {@link ChatActivity#bundle} bundle saved.
     * Also calling the option menu to update it self. Used for showing the thread users icon if thread users amount is bigger then 2.
     * Finally update the action bar for thread messageImageView and name, The update will occur only if needed so free to call.
     */
    private void updateChat() {
        updateThreadFromBundle(this.bundle);
        supportInvalidateOptionsMenu();
        initActionBar();
    }

    private void startTyping () {
        setChatState(TypingIndicatorHandler.State.composing);
        typingTimerDisposable = Observable.just(true).delay(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                setChatState(TypingIndicatorHandler.State.active);
            }
        });
    }

    private void stopTyping (boolean inactive) {
        if (typingTimerDisposable != null) {
            typingTimerDisposable.dispose();
            typingTimerDisposable = null;
        }
        if(inactive) {
            setChatState(TypingIndicatorHandler.State.inactive);
        }
        else {
            setChatState(TypingIndicatorHandler.State.active);
        }
    }

    private void setChatState (TypingIndicatorHandler.State state) {
        if(NM.typingIndicator() != null) {
            disposableList.add(NM.typingIndicator().setChatState(state, thread).subscribe());
        }
    }

    /**
     * show the option popup when the menu key is pressed.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                textInputView.showOptionPopup();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * If the chat was open from a push notification we won't pass the backPress to the system instead we will navigate him to the main activity.
     */
    @Override
    public void onBackPressed() {
        // If the message was opend from a notification back button should lead us to the main activity.
        if (bundle.containsKey(Defines.FROM_PUSH)) {
            if (DEBUG) Timber.d("onBackPressed, From Push");
            bundle.remove(Defines.FROM_PUSH);

            InterfaceManager.shared().a.startMainActivity();
            return;
        }
        super.onBackPressed();
    }

    /**
     * Used for pausing the volley messageImageView loader while the user is scrolling so the scroll will be smooth.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        scrolling = scrollState != SCROLL_STATE_IDLE;

        messageListAdapter.setScrolling(scrolling);
    }

    /**
     * Not used.
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        listPos = firstVisibleItem;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public com.google.android.gms.appindexing.Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ChatSDKAbstractChat Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new com.google.android.gms.appindexing.Action.Builder(com.google.android.gms.appindexing.Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(com.google.android.gms.appindexing.Action.STATUS_TYPE_COMPLETED)
                .build();
    }


    public void loadMessages(final boolean showLoadingIndicator, final int amountToLoad, final ListPosition toPosition) {

        if (showLoadingIndicator) {
            messageListView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
        }

        NM.thread().loadMoreMessagesForThread(null, thread).subscribe(new BiConsumer<List<Message>, Throwable>() {
            @Override
            public void accept(List<Message> messages, Throwable throwable) throws Exception {
                progressBar.setVisibility(View.INVISIBLE);

                messageListAdapter.setMessages(messages);

                if (showLoadingIndicator) {
                    //animateListView();
                }
                messageListView.setVisibility(View.VISIBLE);

                scrollListTo(toPosition, !showLoadingIndicator);
            }
        });

//
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//
//                final int listSize = messageListAdapter.getCount();
//
//                int toLoad = amountToLoad > 0 ? amountToLoad : Defines.MAX_MESSAGES_TO_PULL;
//
//                final List<Message> messages = StorageManager.shared().fetchMessagesForThreadWithID(thread.getId(), listSize + toLoad);
//
//                markAsRead(messages);
//
//                // Sorting the message by date to make sure the list looks ok.
//                Collections.sort(messages, new MessageSorter(MessageSorter.ORDER_TYPE_DESC));
//
//                ChatActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
//            }
//        });
    }

    public void markAsRead(List<Message> messages){
        for (Message m : messages) {
            markAsRead(m);
        }
    }

    public void markAsDelivered(List<Message> messages){
        for (Message m : messages) {
            markAsDelivered(m);
        }
    }

    public void markAsRead(Message message){
        message.setIsRead(true);
        message.update();
        readCount++;
    }

    public void markAsDelivered(Message message){
        message.setDelivered(true);
        message.update();
    }

    public void scrollListTo(final int position, final boolean animated) {
        listPos = position;

        if (animated) {
            messageListView.smoothScrollToPosition(listPos);
        }
        else {
            messageListView.setSelection(listPos);
        }
    }

    public void scrollListTo(final ListPosition position, final boolean animated) {

        int pos = 0;

        switch (position) {
            case Top:
                pos = 0;
                break;
            case Current:
                pos = listPos == -1 ? messageListAdapter.getCount() - 1 : listPos;
                break;
            case Bottom:
                pos = messageListAdapter.getCount() - 1;
                break;
        }

        scrollListTo(pos, animated);
    }
}
