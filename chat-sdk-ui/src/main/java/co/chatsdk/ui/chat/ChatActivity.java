/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.ChatOptionType;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.utils.ActivityResult;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.CrashReportingObserver;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.contacts.SelectContactActivity;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.threads.ThreadImageBuilder;
import co.chatsdk.ui.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ChatActivity extends BaseActivity implements TextInputDelegate, ChatOptionsDelegate {

    public static final int ADD_USERS = 103;
    public static final int SHOW_DETAILS = 200;

    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker message screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = true;
    protected boolean addUserToChatOnEnter = true;

    protected enum ListPosition {
        Top, Current, Bottom
    }

    protected static boolean enableTrace = false;

    /**
     * The key to get the thread long id.
     */
    public static final String LIST_POS = "list_pos";

    /**
     * Pass true if you want slide down animation for this context exit.
     */
    public static final String ANIMATE_EXIT = "animate_exit";

    protected View actionBarView;

    protected TextInputView textInputView;
    protected RecyclerView recyclerView;
    protected MessagesListAdapter messageListAdapter;
    protected Thread thread;
    protected TextView subtitleTextView;

    protected DisposableList disposableList = new DisposableList();
    protected Disposable typingTimerDisposable;

    protected ProgressBar progressBar;
    protected int listPos = -1;

    protected Bundle bundle;

    /**
     * If set to false in onCreate the menu threads wont be inflated in the menu.
     * This can be useful if you want to customize the action bar.
     */
    protected boolean inflateMenuItems = true;

    /**
     * Save the scroll state of the messages list.
     */
    protected boolean scrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();

        if (!updateThreadFromBundle(savedInstanceState)) {
            return;
        }

        if (savedInstanceState != null) {
            listPos = savedInstanceState.getInt(LIST_POS, -1);
            savedInstanceState.remove(LIST_POS);
        }

        if (thread.typeIs(ThreadType.Private1to1) && thread.otherUser() != null && ChatSDK.lastOnline() != null) {
            ChatSDK.lastOnline().getLastOnline(thread.otherUser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((date, throwable) -> {
                if (throwable == null && date != null) {
                    Locale current = getResources().getConfiguration().locale;
                    PrettyTime pt = new PrettyTime(current);
                    setSubtitleText(String.format(getString(R.string.last_seen__), pt.format(date)));
                }
            });
        }

        initActionBar();

        // If the context is just been created we load regularly, else we load and retain position
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

            actionBarView.setOnClickListener(v -> {
                if (ChatSDK.config().threadDetailsEnabled) {
                    openThreadDetailsActivity();
                }
            });

            TextView textView = actionBarView.findViewById(R.id.tvName);

            String displayName = Strings.nameForThread(thread);
            setTitle(displayName);
            textView.setText(displayName);

            subtitleTextView = actionBarView.findViewById(R.id.tvSubtitle);

            final SimpleDraweeView circleImageView = actionBarView.findViewById(R.id.ivAvatar);
            ThreadImageBuilder.load(circleImageView, thread);

            ab.setCustomView(actionBarView);
        }
    }

    protected void initViews () {

        setContentView(R.layout.chat_sdk_activity_chat);

        // Set up the message box - this is the box that sits above the keyboard
        textInputView = findViewById(R.id.chat_sdk_message_box);
        textInputView.setDelegate(this);
        textInputView.setAudioModeEnabled(ChatSDK.audioMessage() != null);

        progressBar = findViewById(R.id.chat_sdk_progressbar);

        final SwipeRefreshLayout mSwipeRefresh = findViewById(R.id.ptr_layout);

        mSwipeRefresh.setOnRefreshListener(() -> {

            List<MessageListItem> items = messageListAdapter.getMessageItems();
            Message firstMessage = null;
            if(items.size() > 0) {
                firstMessage = items.get(0).message;
            }

            disposableList.add(ChatSDK.thread().loadMoreMessagesForThread(firstMessage, thread)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((messages, throwable) -> {
                        if (throwable == null) {
                            if (messages.size() < 2) {
                                showToast(getString(R.string.chat_activity_no_more_messages_to_load_toast));
                            }
                            else {
                                for(Message m : messages) {
                                    messageListAdapter.addRow(m, false, false);
                                }
                                messageListAdapter.sortAndNotify();
                                recyclerView.getLayoutManager().scrollToPosition(messages.size());
                            }
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }));
        });

        recyclerView = findViewById(R.id.list_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scrollListTo(ListPosition.Bottom, false);
            }
        });

        if (messageListAdapter == null) {
            messageListAdapter = new MessagesListAdapter(ChatActivity.this);
        }

        recyclerView.setAdapter(messageListAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        PermissionRequestHandler.shared().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Send text message
     *
     * @param text to send.
     * @param clearEditText if true clear the message edit text.
     */
    public void sendMessage(String text, boolean clearEditText) {

        if (StringUtils.isEmpty(text) || StringUtils.isBlank(text)) {
            return;
        }

        handleMessageSend(ChatSDK.thread().sendMessageWithText(text.trim(), thread));

        if (clearEditText && textInputView != null) {
            textInputView.clearText();
        }

        stopTyping(false);
        scrollListTo(ListPosition.Bottom, false);
    }

    protected void handleMessageSend (Observable<MessageSendProgress> observable) {
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MessageSendProgress>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        //disposableList.add(d);
                    }

                    @Override
                    public void onNext(@NonNull MessageSendProgress messageSendProgress) {
                        Timber.v("Message Status: " + messageSendProgress.getStatus());
                        if(messageListAdapter.addRow(messageSendProgress.message, true, true)) {
                            messageListAdapter.notifyDataSetChanged();
                            scrollListTo(ListPosition.Bottom, false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        ChatSDK.logError(e);
                        ToastHelper.show(getApplicationContext(), e.getLocalizedMessage());
                    }

                    @Override
                    public void onComplete() {
                        messageListAdapter.notifyDataSetChanged();
                        scrollListTo(ListPosition.Bottom, false);
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(InterfaceManager.THREAD_ENTITY_ID, thread.getEntityID());
        }

        // Save the list position
        outState.putInt(LIST_POS, layoutManager().findFirstVisibleItemPosition());
    }

    protected LinearLayoutManager layoutManager () {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        return layoutManager;
    }

    @Override
    protected void onStart() {
        super.onStart();



        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded, EventType.ThreadReadReceiptUpdated, EventType.MessageRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    Message message = networkEvent.message;

                    // Check that the message is relevant to the current thread.
                    if (message.getThreadId() != thread.getId().intValue()) {
                        return;
                    }

                    message.setRead(true);
                    message.update();

                    boolean isAdded = messageListAdapter.addRow(message);
                    if(!isAdded) {
                        messageListAdapter.notifyDataSetChanged();
                    }

                    // Check if the message from the current user, If so return so we wont vibrate for the user messages.
                    if (message.getSender().isMe() && isAdded) {
                        scrollListTo(ListPosition.Bottom, layoutManager().findLastVisibleItemPosition() > messageListAdapter.size() - 2);
                    }
                    else {
                        // If the user is near the bottom, then we scroll down when a message comes in
                        if(layoutManager().findLastVisibleItemPosition() > messageListAdapter.size() - 5) {
                            scrollListTo(ListPosition.Bottom, true);
                        }
                    }

                    if(ChatSDK.readReceipts() != null) {
                        ChatSDK.readReceipts().markRead(thread);
                    }
                }));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    messageListAdapter.removeRow(networkEvent.message, true);
                }));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(
                        EventType.ThreadDetailsUpdated,
                        EventType.ThreadUsersChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> messageListAdapter.notifyDataSetChanged()));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated)).subscribe(networkEvent -> messageListAdapter.notifyDataSetChanged()));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged)).subscribe(networkEvent -> {
                    if(networkEvent.thread.equals(thread)) {
                        if(networkEvent.text != null) {
                            networkEvent.text += getString(R.string.typing);
                        }
                        Timber.v(networkEvent.text);
                        setSubtitleText(networkEvent.text);
                    }
                }));
    }

    protected void setSubtitleText(String text) {
        if(StringChecker.isNullOrEmpty(text)) {
            if(thread.typeIs(ThreadType.Private1to1)) {
                text = getString(R.string.tap_here_for_contact_info);
            }
            else {
                text = "";
                for(User u : thread.getUsers()) {
                    if(!u.isMe()) {
                        String name = u.getName();
                        if (name != null && name.length() > 0) {
                            text += name + ", ";
                        }
                    }
                }
                if(text.length() > 0) {
                    text = text.substring(0, text.length() - 2);
                }
            }
        }
        final String finalText = text;
        new Handler(getMainLooper()).post(() -> subtitleTextView.setText(finalText));
    }

    @Override
    protected void onResume() {
        super.onResume();

        removeUserFromChatOnExit = true;

        if (!updateThreadFromBundle(bundle)) {
            return;
        }

        if (thread != null && thread.typeIs(ThreadType.Public) && addUserToChatOnEnter) {
            User currentUser = ChatSDK.currentUser();
            ChatSDK.thread().addUsersToThread(thread, currentUser)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CrashReportingCompletableObserver(disposableList));
        }

        // Set up the UI to dismiss keyboard on touch event, Option and Send buttons are not included.
        // If list is scrolling we ignoring the touch event.
        setupTouchUIToDismissKeyboard(findViewById(R.id.chat_sdk_root_view), (v, event) -> {

            // Using small delay for better accuracy in catching the scrolls.
            v.postDelayed(() -> {
                if (!scrolling) {
                    hideSoftKeyboard(ChatActivity.this);
                    stopTyping(false);
                }
            }, 300);

            return false;
        }, R.id.chat_sdk_btn_chat_send_message, R.id.chat_sdk_btn_options);

        markRead();

        // We have to do this because otherwise if we background the app
        // we will miss any messages that came through while we were in
        // the background
        loadMessages(true, -1, ListPosition.Bottom);

        // Show a local notification if the message is from a different thread
        ChatSDK.ui().setLocalNotificationHandler(thread -> !thread.equals(ChatActivity.this.thread));

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
        markRead();

        if (thread != null && thread.typeIs(ThreadType.Public) && removeUserFromChatOnExit) {
            ChatSDK.thread().removeUsersFromThread(thread, ChatSDK.currentUser()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CrashReportingCompletableObserver());
        }
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


        if (!updateThreadFromBundle(intent.getExtras()))
            return;

        if (messageListAdapter != null)
            messageListAdapter.clear();

        initActionBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ActivityResultPushSubjectHolder.shared().onNext(new ActivityResult(requestCode, resultCode, data));

        if (requestCode == ADD_USERS) {
            if (resultCode == RESULT_OK) {
                updateChat();
            }
        }
        else if (requestCode == SHOW_DETAILS) {

            if (resultCode == RESULT_OK) {
                // Updating the selected chat id.
                if (data != null && data.getExtras() != null && data.getExtras().containsKey(InterfaceManager.THREAD_ENTITY_ID)) {
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
        if (ChatSDK.config().groupsEnabled && thread.typeIs(ThreadType.Group)) {
            MenuItem item = menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, getString(R.string.chat_activity_show_users_item_text));
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

    protected void markRead () {
        if(ChatSDK.readReceipts() != null) {
            ChatSDK.readReceipts().markRead(thread);
        }
        else {
            thread.markRead();
        }
    }

    /**
     * Open the add users context, Here you can see your contact list and add users to this chat.
     * The default add users context will remove contacts that is already in this chat.
     */
    protected void startAddUsersActivity() {
        Intent intent = new Intent(this, ChatSDK.ui().getSelectContactActivity());
        intent.putExtra(SelectContactActivity.MODE, SelectContactActivity.MODE_ADD_TO_CONVERSATION);
        intent.putExtra(InterfaceManager.THREAD_ENTITY_ID, thread.getEntityID());
        intent.putExtra(LIST_POS, listPos);
        intent.putExtra(ANIMATE_EXIT, true);

        startActivityForResult(intent, ADD_USERS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Show a dialog containing all the users in this chat.
     */
    protected void showUsersDialog() {
        ContactsFragment contactsFragment = ContactsFragment.newThreadUsersDialogInstance(thread.getEntityID(), getString(R.string.thread_users));
        contactsFragment.show(getSupportFragmentManager(), getString(R.string.contacts));
    }

    /**
     * Open the thread details context, Admin user can change thread name an messageImageView there.
     */
    protected void openThreadDetailsActivity() {
        // Showing the pick friends context.
        Intent intent = new Intent(this, ChatSDK.ui().getThreadDetailsActivity());
        intent.putExtra(InterfaceManager.THREAD_ENTITY_ID, thread.getEntityID());
        intent.putExtra(LIST_POS, listPos);
        intent.putExtra(ANIMATE_EXIT, true);

        startActivityForResult(intent, SHOW_DETAILS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Get the current thread from the bundle bundle, CoreThread could be in the getIntent or in onNewIntent.
     */
    protected boolean updateThreadFromBundle(Bundle bundle) {

        if (bundle != null && (bundle.containsKey(InterfaceManager.THREAD_ENTITY_ID))) {
            this.bundle = bundle;
        }
        else {
            if (getIntent() == null || getIntent().getExtras() == null) {
                finish();
                return false;
            }
            this.bundle = getIntent().getExtras();
        }

        if (this.bundle.containsKey(InterfaceManager.THREAD_ENTITY_ID)) {
            String threadEntityID = this.bundle.getString(InterfaceManager.THREAD_ENTITY_ID);
            if(threadEntityID != null) {
                thread = StorageManager.shared().fetchThreadWithEntityID(threadEntityID);
            }
        }
        if (this.bundle.containsKey(LIST_POS)) {
            listPos = (Integer) this.bundle.get(LIST_POS);
            scrollListTo(ListPosition.Current, false);
        }

        if (thread == null) {
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
    protected void updateChat() {
        updateThreadFromBundle(this.bundle);
        supportInvalidateOptionsMenu();
        initActionBar();
    }

    public void startTyping () {
        setChatState(TypingIndicatorHandler.State.composing);
        typingTimerDisposable = Observable.just(true).delay(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(aBoolean -> setChatState(TypingIndicatorHandler.State.active));
    }

    @Override
    public void sendAudio(Recording recording) {
        if(ChatSDK.audioMessage() != null) {
            handleMessageSend(ChatSDK.audioMessage().sendMessage(recording, thread));
        }
    }

    @Override
    public void stopTyping() {
        stopTyping(false);
    }

    @Override
    public void onKeyboardShow() {
        scrollListTo(ListPosition.Bottom, false);
    }

    @Override
    public void onKeyboardHide() {
        scrollListTo(ListPosition.Bottom, false);
    }

    protected void stopTyping (boolean inactive) {
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

    protected void setChatState (TypingIndicatorHandler.State state) {
        if(ChatSDK.typingIndicator() != null) {
            ChatSDK.typingIndicator().setChatState(state, thread)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CrashReportingCompletableObserver(disposableList));
        }
    }

    /**
     * show the option popup when the menu key is pressed.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                showOptions();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * If the chat was open from a push notification we won't pass the backPress to the system instead we will navigate him to the main context.
     */
//    @Override
//    public void onBackPressed() {
//        // If the message was opened from a notification back button should lead us to the main context.
//        if (bundle.containsKey(Defines.FROM_PUSH)) {
//            bundle.remove(Defines.FROM_PUSH);
//
//            ChatSDK.ui().startMainActivity(this);
//            return;
//        }
//        super.onBackPressed();
//    }

    public void loadMessages(final boolean showLoadingIndicator, final int amountToLoad, final ListPosition toPosition) {

        if (showLoadingIndicator) {
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
        }

        ChatSDK.thread().loadMoreMessagesForThread(null, thread)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((messages, throwable) -> {
                    progressBar.setVisibility(View.INVISIBLE);

                    messageListAdapter.setMessages(messages);

                    if (showLoadingIndicator) {
                        //animateListView();
                    }
                    recyclerView.setVisibility(View.VISIBLE);

                    scrollListTo(toPosition, !showLoadingIndicator);
                });
    }

    public void markAsDelivered(List<Message> messages){
        for (Message m : messages) {
            markAsDelivered(m);
        }
    }

    public void markAsDelivered(Message message){
        message.setMessageStatus(MessageSendStatus.Delivered);
        message.update();
    }

    public void scrollListTo(final int position, final boolean animated) {
        listPos = position;

        if (animated) {
            recyclerView.smoothScrollToPosition(listPos);
        }
        else {
            recyclerView.getLayoutManager().scrollToPosition(listPos);
        }
    }

    public void scrollListTo(final ListPosition position, final boolean animated) {

        int pos = 0;

        switch (position) {
            case Top:
                pos = 0;
                break;
            case Current:
                pos = listPos == -1 ? messageListAdapter.size() - 1 : listPos;
                break;
            case Bottom:
                pos = messageListAdapter.size() - 1;
                break;
        }

        scrollListTo(pos, animated);
    }

    @Override
    public void showOptions() {
       removeUserFromChatOnExit = false;
       optionsHandler = ChatSDK.ui().getChatOptionsHandler(this);
       optionsHandler.show(this);
    }

    @Override
    public void hideOptions() {
        removeUserFromChatOnExit = true;
        if(optionsHandler != null) {
            optionsHandler.hide();
        }
    }

    @Override
    public void onSendPressed(String text) {
        sendMessage(text, true);
    }

    @Override
    public void executeChatOption(ChatOption option) {
        if(option.getType() == ChatOptionType.SendMessage) {
            handleMessageSend((Observable<MessageSendProgress>) option.execute(this, thread));
        }
        else {
            option.execute(this, thread).subscribe(new CrashReportingObserver<>());
        }
    }



}
