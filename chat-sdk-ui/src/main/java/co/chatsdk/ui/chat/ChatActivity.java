/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.leinardi.android.speeddial.SpeedDialView;

import org.apache.commons.lang3.StringUtils;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.dao.Keys;
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
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.message_action.MessageActionHandler;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.threads.ThreadImageBuilder;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ChatActivity extends BaseActivity implements TextInputDelegate, ChatOptionsDelegate {

    public static final int ADD_USERS = 103;
    public static final int SHOW_DETAILS = 200;

    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker message screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = ChatSDK.config().removeUserFromPublicThreadOnExit;

    protected enum ListPosition {
        Top, Current, Bottom
    }

    protected static boolean enableTrace = false;

    protected View actionBarView;

    protected TextInputView textInputView;
    protected RecyclerView recyclerView;
    protected MessageListAdapter messageListAdapter;
    protected Thread thread;
    protected TextView titleTextView;
    protected TextView subtitleTextView;
    protected SimpleDraweeView threadImageView;
    protected Disposable typingTimerDisposable;

    protected ProgressBar progressBar;
    protected int listPos = -1;

    protected Bundle bundle;
    protected boolean loadingMoreMessages;

    protected SpeedDialView messageActionsSpeedDialView;
    protected MessageActionHandler messageActionHandler;

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


        setContentView(activityLayout());

        initViews();

        if (!updateThreadFromBundle(savedInstanceState)) {
            return;
        }

        if (savedInstanceState != null) {
            listPos = savedInstanceState.getInt(Keys.IntentKeyListPosSelectEnabled, -1);
            savedInstanceState.remove(Keys.IntentKeyListPosSelectEnabled);
        }

        initActionBar();

        // If the context is just been created we load regularly, else we load and retain position
//        loadMessages(true, -1, ListPosition.Bottom);

        setChatState(TypingIndicatorHandler.State.active);

        if(enableTrace) {
            android.os.Debug.startMethodTracing("chat");
        }

        // Add the event listeners
        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    Message message = networkEvent.message;

                    message.setRead(true);

                    boolean isAdded = messageListAdapter.addRow(message, false, true);

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
                .filter(NetworkEvent.filterType(EventType.ThreadReadReceiptUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    Message message = networkEvent.message;

                    if (ChatSDK.readReceipts() != null && message.getSender().isMe()) {
                        reloadDataForMessage(message);
                    }
                }));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    messageListAdapter.removeRow(networkEvent.message, true);
                }));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadDetailsUpdated, EventType.ThreadUsersChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> reloadActionBar()));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .filter(networkEvent -> thread.containsUser(networkEvent.user))
                .subscribe(networkEvent -> {
                    reloadData();
                    reloadActionBar();
                }));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    String typingText = networkEvent.text;
                    if(typingText != null) {
                        typingText += getString(R.string.typing);
                    }
                    Timber.v(typingText);
                    setSubtitleText(typingText);
                }));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageSendStatusChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    MessageSendProgress progress = networkEvent.getMessageSendProgress();
                    MessageSendStatus status = progress.getStatus();

                    if (status == MessageSendStatus.Sending || status == MessageSendStatus.Created) {
                        if(messageListAdapter.addRow(progress.message, false, true, progress.uploadProgress, true)) {
                            scrollListTo(ListPosition.Bottom, false);
                        }
                    }
                    if (status == MessageSendStatus.Uploading || status == MessageSendStatus.Sent) {
                        reloadDataForMessage(progress.message);
                    }
        }));

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

        final ActionBar ab = readyActionBarToCustomView();

        // http://stackoverflow.com/questions/16026818/actionbar-custom-view-with-centered-imageview-action-items-on-sides
        actionBarView = getLayoutInflater().inflate(R.layout.action_bar_chat_activity, null);

        actionBarView.setOnClickListener(v -> {
            if (ChatSDK.config().threadDetailsEnabled) {
                openThreadDetailsActivity();
            }
        });

        titleTextView = actionBarView.findViewById(R.id.text_name);
        subtitleTextView = actionBarView.findViewById(R.id.text_subtitle);
        threadImageView = actionBarView.findViewById(R.id.image_avatar);

        ab.setCustomView(actionBarView);

        reloadActionBar();
    }

    protected void reloadActionBar () {
        String displayName = Strings.nameForThread(thread);
        setTitle(displayName);
        titleTextView.setText(displayName);
        ThreadImageBuilder.load(threadImageView, thread);
    }

    protected @LayoutRes int activityLayout() {
        return R.layout.activity_chat;
    }

    protected void initViews () {
        // Set up the message box - this is the box that sits above the keyboard
        textInputView = findViewById(R.id.view_message_text_input);
        textInputView.setDelegate(this);
        textInputView.setAudioModeEnabled(ChatSDK.audioMessage() != null);

        progressBar = findViewById(R.id.progress_bar);

        final SwipeRefreshLayout mSwipeRefresh = findViewById(R.id.layout_swipe_to_refresh);

        mSwipeRefresh.setOnRefreshListener(() -> {
            if (!loadingMoreMessages) {
                loadMoreMessages(true, true, true)
                        .doFinally(() -> mSwipeRefresh.setRefreshing(false))
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnError(toastOnErrorConsumer())
                        .subscribe(new CrashReportingCompletableObserver());
            } else {
                mSwipeRefresh.setRefreshing(false);
            }
        });

        recyclerView = findViewById(R.id.recycler_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Optimization
        recyclerView.setItemViewCacheSize(50);

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scrollListTo(ListPosition.Bottom, false);
            }
        });

        if (messageListAdapter == null) {
            messageListAdapter = new MessageListAdapter(ChatActivity.this);
        }

        recyclerView.setAdapter(messageListAdapter);

        // Disable this for now until it works better
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = layoutManager().findFirstVisibleItemPosition();
                int lastVisible = layoutManager().findLastVisibleItemPosition();
                int items = messageListAdapter.getItemCount();

                // Only load more messages if we are scrolling up
                // Also only do this when we are scroling slowly...
//                if (dy < 0 && Math.abs(dy) < 20 && firstVisible < 5) {
//                    loadMoreMessages(false, true, true).subscribe(new CrashReportingCompletableObserver());
//                }

            }
        });

        setupChatActions();
    }

    protected void setupChatActions() {
        messageActionsSpeedDialView = findViewById(R.id.speed_dial_message_actions);
        messageActionHandler = new MessageActionHandler(messageActionsSpeedDialView);

        disposableList.add(messageListAdapter.getMessageActionObservable()
                .flatMapSingle((Function<List<MessageAction>, SingleSource<String>>) messageActions -> {
                    // Open the message action sheet
                    hideKeyboard();
                    return messageActionHandler.open(messageActions, ChatActivity.this);
        }).subscribe(this::showSnackbar, snackbarOnErrorConsumer()));
    }

    public Completable loadMoreMessages (boolean loadFromServer, boolean saveScrollPosition, boolean notify) {
        return Maybe.create((MaybeOnSubscribe<Date>) emitter -> {
            if (loadingMoreMessages) {
                emitter.onComplete();
            } else {
                loadingMoreMessages = true;

                List<MessageListItem> items = messageListAdapter.getMessageItems();
                if(items.size() > 0) {
                    emitter.onSuccess(items.get(0).message.getDate().toDate());
                } else {
                    emitter.onSuccess(new Date(0));
                }
            }
        }).flatMapCompletable(date -> ChatSDK.thread().loadMoreMessagesForThread(date, thread, loadFromServer)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(messages -> {

                    int addedCount = 0;
                    for(Message m : messages) {
                        if(messageListAdapter.addRow(m, false, false, false)) {
                            addedCount++;
                        }
                    }
                    if (notify) {
                        reloadDataInRange(0, messages.size());
                    }

                    int firstVisible = layoutManager().findFirstVisibleItemPosition();
                    int lastVisible = layoutManager().findLastVisibleItemPosition();

                    if (addedCount > 0 && saveScrollPosition) {
                        scrollListTo(messages.size() + lastVisible - firstVisible, false);
                    }

                    firstVisible = layoutManager().findFirstVisibleItemPosition();
                    System.out.println(firstVisible);

                    loadingMoreMessages = false;
                }).ignoreElement());
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

    protected void handleMessageSend (Completable completable) {
        completable.observeOn(AndroidSchedulers.mainThread()).doOnError(throwable -> {
            ChatSDK.logError(throwable);
            ToastHelper.show(getApplicationContext(), throwable.getLocalizedMessage());
        }).subscribe(new CrashReportingCompletableObserver());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }

        // Save the list position
        outState.putInt(Keys.IntentKeyListPosSelectEnabled, layoutManager().findFirstVisibleItemPosition());
    }

    protected LinearLayoutManager layoutManager () {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        return layoutManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void setSubtitleText(String text) {
        if(StringChecker.isNullOrEmpty(text)) {
            if(thread.typeIs(ThreadType.Private1to1)) {
                text = getString(R.string.tap_here_for_contact_info);
            } else {
                text = thread.getDisplayName();
            }
        }
        final String finalText = text;
        new Handler(getMainLooper()).post(() -> subtitleTextView.setText(finalText));
    }

    protected void reloadData () {
        messageListAdapter.notifyDataSetChanged();
    }

    protected void reloadDataInRange (int startingIndex, int itemCount) {
        messageListAdapter.notifyItemRangeChanged(startingIndex, itemCount);
    }

    protected void sortAndReloadData () {
        messageListAdapter.sortAndNotify();
    }

    protected void reloadDataForMessage (Message message) {
        messageListAdapter.notifyMessageChanged(message);
    }

    @Override
    protected void onResume() {
        super.onResume();

        removeUserFromChatOnExit = true;

        if (!updateThreadFromBundle(bundle)) {
            return;
        }

        if (thread != null && thread.typeIs(ThreadType.Public)) {
            User currentUser = ChatSDK.currentUser();
            ChatSDK.thread().addUsersToThread(thread, currentUser)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CrashReportingCompletableObserver(disposableList));
        }

        if (thread.typeIs(ThreadType.Private1to1) && thread.otherUser() != null && ChatSDK.lastOnline() != null) {
            disposableList.add(ChatSDK.lastOnline().getLastOnline(thread.otherUser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((date, throwable) -> {
                        if (throwable == null && date != null) {
                            Locale current = getResources().getConfiguration().locale;
                            PrettyTime pt = new PrettyTime(current);
                            if (thread.otherUser().getIsOnline()) {
                                setSubtitleText(ChatActivity.this.getString(R.string.online));
                            } else {
                                setSubtitleText(String.format(getString(R.string.last_seen__), pt.format(date)));
                            }
                        }
                    }));
        }

        // Set up the UI to dismiss keyboard on touch event, Option and Send buttons are not included.
        // If list is scrolling we ignoring the touch event.
        setupTouchUIToDismissKeyboard(findViewById(R.id.view_root), (v, event) -> {

            // Using small delay for better accuracy in catching the scrolls.
            v.postDelayed(() -> {
                if (!scrolling) {
                    hideKeyboard();
                    stopTyping(false);
                }
            }, 300);

            return false;
        }, R.id.button_send, R.id.button_options);

        markRead();

        // We have to do this because otherwise if we background the app
        // we will miss any messages that came through while we were in
        // the background
        loadMessages(messageListAdapter.getItemCount() == 0, -1, ListPosition.Bottom);

        // Show a local notification if the message is from a different thread
        ChatSDK.ui().setLocalNotificationHandler(thread -> !thread.equals(ChatActivity.this.thread));

    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    /**
     * Sending a broadcast that the chat was closed, Only if there were new messages on this chat.
     * This is used for example to update the thread list that messages has been read.
     */
    @Override
    protected void onStop() {
        super.onStop();

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

        if (requestCode == ADD_USERS) {
            if (resultCode == RESULT_OK) {
                updateChat();
            }
        }
        else if (requestCode == SHOW_DETAILS) {

            if (resultCode == RESULT_OK) {
                // Updating the selected chat id.
                if (data != null && data.getExtras() != null && data.getExtras().containsKey(Keys.IntentKeyThreadEntityID)) {
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
        if (thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator().isMe()) {
            MenuItem item = menu.add(Menu.NONE, R.id.action_add, 10, getString(R.string.chat_activity_show_users_item_text));
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

        if (id == R.id.action_add) {
            startAddUsersActivity();
        }
        else if (id == R.id.action_show) {
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
        Intent intent = new Intent(this, ChatSDK.ui().getAddUsersToThreadActivity());
        intent.putExtra(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        intent.putExtra(Keys.IntentKeyListPosSelectEnabled, listPos);
        intent.putExtra(Keys.IntentKeyAnimateExit, true);

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
        intent.putExtra(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        intent.putExtra(Keys.IntentKeyListPosSelectEnabled, listPos);
        intent.putExtra(Keys.IntentKeyAnimateExit, true);

        startActivityForResult(intent, SHOW_DETAILS);

        overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
    }

    /**
     * Get the current thread from the bundle bundle, CoreThread could be in the getIntent or in onNewIntent.
     */
    protected boolean updateThreadFromBundle(Bundle bundle) {

        if (bundle != null && (bundle.containsKey(Keys.IntentKeyThreadEntityID))) {
            this.bundle = bundle;
        }
        else {
            if (getIntent() == null || getIntent().getExtras() == null) {
                finish();
                return false;
            }
            this.bundle = getIntent().getExtras();
        }

        if (this.bundle.containsKey(Keys.IntentKeyThreadEntityID)) {
            String threadEntityID = this.bundle.getString(Keys.IntentKeyThreadEntityID);
            if(threadEntityID != null) {
                thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
            }
        }
        if (this.bundle.containsKey(Keys.IntentKeyListPosSelectEnabled)) {
            listPos = (Integer) this.bundle.get(Keys.IntentKeyListPosSelectEnabled);
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
     * Show the option popup when the menu key is pressed.
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

    public void loadMessages(final boolean showLoadingIndicator, final int amountToLoad, final ListPosition toPosition) {
        progressBar.setVisibility(showLoadingIndicator ? View.VISIBLE : View.INVISIBLE);

        disposableList.add(loadMoreMessages(false, false, true)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> progressBar.setVisibility(View.INVISIBLE))
                .subscribe(() -> scrollListTo(toPosition, !showLoadingIndicator), toastOnErrorConsumer()));
    }

    public void markAsDelivered(List<Message> messages){
        for (Message m : messages) {
            markAsDelivered(m);
        }
    }

    public void markAsDelivered(Message message){
        message.setMessageStatus(MessageSendStatus.Delivered);
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

//        optionsSpeedDialView.open();

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
        handleMessageSend(option.execute(this, thread));
    }

}
