/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
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


import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BThreadDao;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import wanderingdevelopment.tk.sdkbaseui.Activities.BaseActivity;
import wanderingdevelopment.tk.sdkbaseui.Activities.BaseThreadActivity;
import wanderingdevelopment.tk.sdkbaseui.Activities.PickFriendsActivity;
import wanderingdevelopment.tk.sdkbaseui.R;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import co.chatsdk.core.defines.Debug;

import wanderingdevelopment.tk.sdkbaseui.Fragments.ContactsFragment;

import co.chatsdk.core.events.PredicateFactory;
import co.chatsdk.core.utils.volley.VolleyUtils;

import wanderingdevelopment.tk.sdkbaseui.UiHelpers.MakeThreadImage;

import co.chatsdk.core.dao.DaoCore;
import com.braunster.chatsdk.thread.ChatSDKImageMessagesThreadPool;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import wanderingdevelopment.tk.sdkbaseui.utils.Strings;
import wanderingdevelopment.tk.sdkbaseui.view.CircleImageView;
import timber.log.Timber;

public class ChatActivity extends BaseActivity implements AbsListView.OnScrollListener {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatActivity;

    public static final int ADD_USERS = 103;
    public static final int SHOW_DETAILS = 200;

    public static final String ACTION_CHAT_CLOSED = "braunster.chat.action.chat_closed";

    /**
     * The key to get the thread long id.
     */
    public static final String THREAD_ID = "thread_id";
    public static final String LIST_POS = "list_pos";

    /**
     * Pass true if you want slide down animation for this activity exit.
     */
    public static final String ANIMATE_EXIT = "animate_exit";

    protected View actionBarView;

    protected ChatHelper chatHelper;

    protected TextInputView textInputView;
    protected ListView listMessages;
    protected MessagesListAdapter messagesListAdapter;
    protected BThread thread;

    protected ProgressBar progressBar;
    protected int listPos = -1;

    protected Bundle bundle;

    private boolean queueStopped = false;

    /**
     * If set to false in onCreate the menu items wont be inflated in the menu.
     * This can be useful if you want to customize the action bar.
     */
    protected boolean inflateMenuItems = true;

    /**
     * Save the scroll state of the messages list.
     */
    protected boolean scrolling = false;

    private boolean created = true;

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

        setCardToastEnabled(true);

        chatHelper = new ChatHelper(this, thread, chatSDKUiHelper);
        chatHelper.restoreSavedInstance(savedInstanceState);

        initViews();

        enableCheckOnlineOnResumed(true);

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
    }

    @Override
    protected Bitmap getTaskDescriptionBitmap() {
        return super.getTaskDescriptionBitmap();
    }

    protected ActionBar readyActionBarToCustomView() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);

        return ab;
    }

    protected View inflateActionBarView(int resId) {
        // Inflate the custom view
        if (actionBarView == null || actionBarView.getId() != resId) {
            LayoutInflater inflater = LayoutInflater.from(this);
            actionBarView = inflater.inflate(resId, null);
        }

        return actionBarView;
    }

    protected void initActionBar() {
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

            changed = setThreadImage(circleImageView, imageView, onClickListener) || changed;

            if (changed)
                ab.setCustomView(actionBarView);
        }
    }

    /**
     * Setting the thread name in the action bar.
     */
    protected boolean setThreadName(TextView txtName) {

        String displayName = Strings.nameForThread(thread);

        if (StringUtils.isBlank(displayName))
            return false;

        if (txtName.getText() == null || !displayName.equals(txtName.getText().toString())) {
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
     */
    protected boolean setThreadImage(final CircleImageView circleImageView, final ImageView imageView, final View.OnClickListener onClickListener) {
        final String imageUrl = thread.threadImageUrl();

        if (circleImageView.getTag() == null || StringUtils.isEmpty(imageUrl) || !imageUrl.equals(circleImageView.getTag())) {
            if (StringUtils.isEmpty(imageUrl))
                setRoundCornerDefault(circleImageView, imageView, onClickListener);
            else {
                if (imageUrl.split(",").length > 1) {
                    int size = getResources().getDimensionPixelSize(R.dimen.chat_sdk_chat_action_barcircle_image_view_size);
                    new MakeThreadImage(imageUrl.split(","), size, size, thread.getEntityID(), circleImageView);
                    circleImageView.setOnClickListener(onClickListener);
                    circleImageView.setVisibility(View.VISIBLE);
                    circleImageView.bringToFront();
                } else
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
     */
    protected void setRoundCornerDefault(CircleImageView circleImageView, ImageView roundedCornerImageView, View.OnClickListener onClickListener) {
        circleImageView.setVisibility(View.INVISIBLE);
        roundedCornerImageView.setVisibility(View.INVISIBLE);

        if (thread.typeIs(ThreadType.Public)) {
            roundedCornerImageView.setImageResource(R.drawable.ic_users);
        } else if (thread.getUsers().size() < 3 || thread.typeIs(ThreadType.Private1to1)) {
            roundedCornerImageView.setImageResource(R.drawable.ic_profile);
        } else {
            roundedCornerImageView.setImageResource(R.drawable.ic_users);
        }

        roundedCornerImageView.setVisibility(View.VISIBLE);

        roundedCornerImageView.bringToFront();

        roundedCornerImageView.setOnClickListener(onClickListener);
    }

    protected void initViews() {
        final Activity activity = this;

        setContentView(R.layout.chat_sdk_activity_chat);

        // Set up the message box - this is the box that sits above the keyboard
        textInputView = (TextInputView) findViewById(R.id.chat_sdk_message_box);
        textInputView.setAlertToast(chatSDKUiHelper.getAlertToast());
        textInputView.setMessageSendListener(new TextInputView.MessageSendListener() {
            @Override
            public void onSendPressed(String text) {
                sendMessage(text, true);
            }
        });

        textInputView.setMessageBoxOptionsListener(new TextInputView.MessageBoxOptionsListener() {
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

                photoSelector = new PhotoSelector();

                try {
                    photoSelector.startTakePhotoActivity(activity, new PhotoSelector.Result() {
                        public void result(String result) {
                            sendImageMessage(result);
                        }
                    });
                } catch (Exception e) {
                    chatSDKUiHelper.dismissProgressCard();
                    chatSDKUiHelper.showToast(e.getMessage());
                }
            }

            @Override
            public void onPickImagePressed() {
                photoSelector.startPickImageActivity(activity, new PhotoSelector.Result() {
                    @Override
                    public void result(String result) {
                        sendImageMessage(result);
                    }
                });
            }

            @Override
            public boolean onOptionButtonPressed() {
                return false;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.chat_sdk_progressbar);

        chatHelper.setProgressBar(progressBar);

        final SwipeRefreshLayout mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DEBUG) Timber.d("onRefreshStarted");

                NM.thread().loadMoreMessagesForThread(thread).subscribe(new BiConsumer<List<BMessage>, Throwable>() {
                    @Override
                    public void accept(List<BMessage> bMessages, Throwable throwable) throws Exception {
                        if (throwable == null) {
                            if (bMessages.size() < 2)
                                showToast(getString(R.string.chat_activity_no_more_messages_to_load_toast));
                            else {
                                // Saving the position in the list so we could back to it after the update.
                                chatHelper.loadMessages(true, false, -1, messagesListAdapter.getCount() + bMessages.size());
                            }

                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

        listMessages = (ListView) findViewById(R.id.list_chat);

        chatHelper.setListMessages(listMessages);

        if (messagesListAdapter == null) {
            messagesListAdapter = new MessagesListAdapter(ChatActivity.this, NM.currentUser().getId());
        }

        listMessages.setAdapter(messagesListAdapter);
        chatHelper.setMessagesListAdapter(messagesListAdapter);
    }

    /**
     * Send text message
     *
     * @param text          the text to send.
     * @param clearEditText if true clear the message edit text.
     */
    public void sendMessage(String text, boolean clearEditText) {
        if (DEBUG)
            Timber.v("sendTextMessage, Text: %s, Clear: %s", text, String.valueOf(clearEditText));

        if (StringUtils.isEmpty(text) || StringUtils.isBlank(text)) {
            return;
        }

        NM.thread().sendMessageWithText(text.trim(), thread)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        chatSDKUiHelper.showToast(R.string.unable_to_send_message);
                    }
                }).subscribe();

        if (clearEditText && textInputView != null)
            textInputView.clearText();
    }

    public void sendLocationMessage(String snapshotPath, LatLng latLng) {

        showSendingMessageToast();

        NM.thread().sendMessageWithLocation(snapshotPath, latLng, thread)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        chatSDKUiHelper.showToast(R.string.unable_to_send_location_message);
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        chatSDKUiHelper.dismissProgressCardWithSmallDelay();
                    }
                }).subscribe();


        // TODO: BEN1 Add this in
        // Adding the message after it was prepared bt the NetworkAdapter.
//        String path = bundle.getExtras().getString(ChatSDKLocationActivity.SNAP_SHOT_PATH, "");
//        if (StringUtils.isNotBlank(path))
//        {
//            if (messagesListAdapter != null)
//            {
//                messagesListAdapter.addRow(message);
//                scrollListTo(messagesListAdapter.getCount(), true);
//            }
//        }


    }

    private void showSendingMessageToast() {
        chatSDKUiHelper.initCardToast();
        chatSDKUiHelper.showProgressCard("Sending...");
    }

    /**
     * Send an image message.
     *
     * @param filePath the path to the image file that need to be sent.
     */
    public void sendImageMessage(final String filePath) {
        if (DEBUG) Timber.v("sendImfageMessage, Path: %s", filePath);

        showSendingMessageToast();

        NM.thread().sendMessageWithImage(filePath, thread)
                .doOnNext(new Consumer<ImageUploadResult>() {
                    @Override
                    public void accept(ImageUploadResult value) throws Exception {
                        chatSDKUiHelper.setProgress(value.progress.asFraction());
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        chatSDKUiHelper.dismissProgressCardWithSmallDelay();
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        chatSDKUiHelper.showToast(R.string.unable_to_send_image_message);
                    }
                }).subscribe();


        // TODO: BEN1 Add this!

//        // Adding the message after it was prepared bt the NetworkAdapter.
//        if (messagesListAdapter != null)
//        {
//            messagesListAdapter.addRow(message);
//            scrollListTo(messagesListAdapter.getCount(), true);
//        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null)
            outState.putLong(THREAD_ID, thread.getId());

        // Save the list position
        outState.putInt(LIST_POS, listMessages.getFirstVisiblePosition());
        chatHelper.onSavedInstanceBundle(outState);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Timber.v("onResume");

        if (!updateThreadFromBundle(bundle)) {
            return;
        }

        if (thread != null && thread.typeIs(ThreadType.Public)) {
            BUser currentUser = NM.currentUser();
            NM.thread().addUsersToThread(thread, currentUser).subscribe();
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
                            hideSoftKeyboard(ChatActivity.this);
                    }
                }, 300);

                return false;
            }
        }, R.id.chat_sdk_btn_chat_send_message, R.id.chat_sdk_btn_options);

        NM.events().sourceOnMain()
                .filter(PredicateFactory.type(EventType.MessageAdded))
                .filter(PredicateFactory.threadEntityID(thread.getEntityID()))
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {
                        BMessage message = networkEvent.message;

                        // Check that the message is relevant to the current thread.
                        if (message.getThreadId() != thread.getId().intValue()) {
                            return;
                        }

                        //Set as read.
                        chatHelper.markAsRead(message);

                        boolean isAdded = messagesListAdapter.addRow(message);

                        BUser currentUser = NM.currentUser();

                        // Check if the message from the current user, If so return so we wont vibrate for the user messages.
                        if (message.getSender().getEntityID().equals(currentUser.getEntityID())) {
                            if (isAdded) {
                                chatHelper.scrollListTo(-1, true);
                            }
                        } else {
                            Vibrator v = (Vibrator) ChatActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(Defines.VIBRATION_DURATION);
                        }

                        message.setDelivered(BMessage.Delivered.Yes);
                        DaoCore.updateEntity(message);

                    }
                });

        NM.events().sourceOnMain()
                .filter(PredicateFactory.type(
                        EventType.PrivateThreadAdded,
                        EventType.PrivateThreadRemoved,
                        EventType.PublicThreadAdded,
                        EventType.PublicThreadRemoved,
                        EventType.ThreadDetailsUpdated,
                        EventType.ThreadUsersChanged))
                .filter(PredicateFactory.threadEntityID(thread.getEntityID()))
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {
                        updateChat();
                    }
                });

        // If the activity is just been created we load regularly, else we load and retain position
        if (!created)
            chatHelper.loadMessagesAndRetainCurrentPos();
        else {
            chatHelper.loadMessages(listPos);
        }

        created = false;
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());

        if (chatHelper.getReadCount() > 0) {
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
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) Timber.v("onNewIntent");

        if (!updateThreadFromBundle(intent.getExtras()))
            return;

        created = true;

        if (messagesListAdapter != null)
            messagesListAdapter.clear();

        initActionBar();

        //chatHelper.checkIfWantToShare(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) Timber.v("onActivityResult");


        try {
            photoSelector.handleResult(this, requestCode, resultCode, data);
            locationSelector.handleResult(this, requestCode, resultCode, data);
        } catch (Exception e) {
            chatSDKUiHelper.dismissProgressCard();
            if (e.getMessage() != null && e.getMessage().length() > 0) {
                chatSDKUiHelper.showToast(e.getMessage());
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
                if (data != null && data.getExtras() != null && data.getExtras().containsKey(THREAD_ID)) {
                    if (!updateThreadFromBundle(data.getExtras()))
                        return;

                    created = true;

                    if (messagesListAdapter != null)
                        messagesListAdapter.clear();

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
        if (Defines.Options.GroupEnabled) {
            MenuItem item =
                    menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, getString(R.string.chat_activity_show_users_item_text));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            item.setIcon(R.drawable.ic_plus);
        }

        if (Defines.Options.ThreadDetailsEnabled) {

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

        if (id == R.id.action_chat_sdk_add) {
            startAddUsersActivity();
        } else if (id == R.id.action_chat_sdk_show) {
            showUsersDialog();
        } else if (id == R.id.action_chat_sdk_thread_details) {
            openThreadDetailsActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open the add users activity, Here you can see your contact list and add users to this chat.
     * The default add users activity will remove contacts that is already in this chat.
     */
    protected void startAddUsersActivity() {
        // Showing the pick friends activity.
        Intent intent = new Intent(this, chatSDKUiHelper.getPickFriendsActivity());
        intent.putExtra(PickFriendsActivity.MODE, PickFriendsActivity.MODE_ADD_TO_CONVERSATION);
        intent.putExtra(BaseThreadActivity.THREAD_ID, thread.getId());
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
     * Open the thread details activity, Admin user can change thread name an image there.
     */
    protected void openThreadDetailsActivity() {
        // Showing the pick friends activity.
        Intent intent = new Intent(this, chatSDKUiHelper.getThreadDetailsActivity());
        intent.putExtra(THREAD_ID, thread.getId());
        intent.putExtra(ANIMATE_EXIT, true);

        startActivityForResult(intent, SHOW_DETAILS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Not used, show a dialog containing all the user in this chat with custom title and option to show or hide headers.
     *
     * @param withHeaders if true the list will contain its headers for users.
     * @param title       the title of the dialog.
     */
    protected void showUsersDialog(String title, boolean withHeaders) {
        ContactsFragment contactsFragment = ContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), title, true);
        contactsFragment.setWithHeaders(withHeaders);
        contactsFragment.setTitle(title);
        contactsFragment.show(getSupportFragmentManager(), "Contacts");
    }

    @Override
    public void onAuthenticated() {
        super.onAuthenticated();
        if (DEBUG) Timber.v("onAuthenticated");
        chatHelper.loadMessagesAndRetainCurrentPos();
    }


    /**
     * Get the current thread from the bundle bundle, CoreThread could be in the getIntent or in onNewIntent.
     */
    private boolean updateThreadFromBundle(Bundle bundle) {

        if (bundle != null && (bundle.containsKey(THREAD_ID))) {
            this.bundle = bundle;
        }
        else {
            if (getIntent() == null || getIntent().getExtras() == null) {
                finish();
                return false;
            }
            this.bundle = getIntent().getExtras();
        }

        if (this.bundle.containsKey(THREAD_ID)) {
            thread = DaoCore.fetchEntityWithProperty(BThread.class,
                    BThreadDao.Properties.Id,
                    this.bundle.getLong(THREAD_ID));
        }

        if (thread == null) {
            if (DEBUG) Timber.e("No Thread found for given ID.");
            finish();
            return false;
        }
        else {
            this.thread = thread;
            chatHelper.setThread(thread);
        }

        return true;
    }

    /**
     * Update chat current thread using the {@link ChatActivity#bundle} bundle saved.
     * Also calling the option menu to update it self. Used for showing the thread users icon if thread users amount is bigger then 2.
     * Finally update the action bar for thread image and name, The update will occur only if needed so free to call.
     */
    private void updateChat() {



        updateThreadFromBundle(this.bundle);
        supportInvalidateOptionsMenu();
        initActionBar();
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

            chatSDKUiHelper.startMainActivity();
            return;
        }
        super.onBackPressed();
    }

    /**
     * Used for pausing the volley image loader while the user is scrolling so the scroll will be smooth.
     */
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
        if (queueStopped && !scrolling) {
            ChatSDKImageMessagesThreadPool.getInstance().getThreadPool().resume();
            VolleyUtils.getRequestQueue().start();
            queueStopped = false;
        }

        messagesListAdapter.setScrolling(scrolling);
    }

    /**
     * Not used.
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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

//    public void checkIfWantToShare(Intent intent){
//        if (DEBUG) Timber.v("checkIfWantToShare");
//
//        if (intent.getExtras() == null || intent.getExtras().isEmpty()) {
//            return;
//        }
//
//        if (intent.getExtras().containsKey(SHARED_FILE_URI))
//        {
//            if (DEBUG) Timber.i("Want to share URI");
//
//            try{
//                String path = Utils.getRealPathFromURI(activity.get(), (Uri) intent.getExtras().get(SHARED_FILE_URI));
//
//                if (DEBUG) Timber.d("Path from uri: " + path);
//
//                uiHelper.showProgressCard(R.string.sending);
//
//                sendImageMessage(path);
//            }
//            catch (NullPointerException e){
//                uiHelper.showToast(R.string.unable_to_fetch_image);
//            }
//
//            // removing the key so we wont send again,
//            intent.getExtras().remove(SHARED_FILE_URI);
//
//            intent.removeExtra(SHARED_FILE_URI);
//
//            shared = true;
//        }
//        else if (intent.getExtras().containsKey(SHARED_TEXT))
//        {
//            if (DEBUG) Timber.i("Want to share Text");
//
//            String text =intent.getExtras().getString(SHARED_TEXT);
//
//            // removing the key so we wont send again,
//            intent.getExtras().remove(SHARED_TEXT);
//
//            sendMessageWithText(text, false);
//
//            intent.removeExtra(SHARED_TEXT);
//
//            shared = true;
//        }
//        else if (intent.getExtras().containsKey(SHARED_FILE_PATH))
//        {
//            if (DEBUG) Timber.i("Want to share File from path");
//            uiHelper.showProgressCard("Sending...");
//
//            String path =intent.getStringExtra(SHARED_FILE_PATH);
//
//            // removing the key so we wont send again,
//            intent.getExtras().remove(SHARED_FILE_PATH);
//
//            sendImageMessage(path);
//
//            intent.removeExtra(SHARED_FILE_PATH);
//
//            shared = true;
//        }
//        else if (intent.getExtras().containsKey(SHARE_LOCATION)){
//            if (DEBUG) Timber.i("Want to share Location");
//            // FIXME pull text from string resource for language control
//            uiHelper.showProgressCard(R.string.sending);
//
//            //TODO: BEN1
//            this.sendLocationMessage(intent);


//            BNetworkManager.getThreadsInterface().sendMessageWithLocation(intent.getExtras().getString(SHARE_LOCATION, null),
//                    new LatLng(intent.getDoubleExtra(LAT, 0), intent.getDoubleExtra(LNG, 0)),
//                    thread.getId())
//                    .done(new DoneCallback<BMessage>() {
//                        @Override
//                        public void onDone(BMessage message) {
//                            if (DEBUG) Timber.v("Image is sent");
//
//                            uiHelper.dismissProgressCardWithSmallDelay();
//                        }
//                    })
//                    .fail(new FailCallback<ChatError>() {
//                        @Override
//                        public void onFail(ChatError chatError) {
//                            uiHelper.dismissProgressCardWithSmallDelay();
//                            uiHelper.showToast(R.string.unable_to_send_location_message);
//                        }
//                    });
//
//            intent.removeExtra(SHARE_LOCATION);
//
//            shared = true;
//        }
//    }


}
