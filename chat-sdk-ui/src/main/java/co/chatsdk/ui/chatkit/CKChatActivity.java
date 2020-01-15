/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chatkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBar;

import com.leinardi.android.speeddial.SpeedDialView;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
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
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.ImageBuilder;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.TextInputDelegate;
import co.chatsdk.ui.chat.message_action.MessageActionHandler;
import co.chatsdk.ui.chatkit.custom.IncomingImageMessageViewHolder;
import co.chatsdk.ui.chatkit.custom.IncomingTextMessageViewHolder;
import co.chatsdk.ui.chatkit.custom.OutcomingImageMessageViewHolder;
import co.chatsdk.ui.chatkit.custom.OutcomingTextMessageViewHolder;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.threads.ThreadImageBuilder;
import co.chatsdk.ui.chatkit.model.MessageHolder;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CKChatActivity extends BaseActivity implements TextInputDelegate, ChatOptionsDelegate, MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener {

    public static final int ADD_USERS = 103;
    public static final int SHOW_DETAILS = 200;

    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker text screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

    protected static boolean enableTrace = false;

    protected View actionBarView;

    protected Thread thread;
    protected TextView titleTextView;
    protected TextView subtitleTextView;
    protected ImageView threadImageView;

    protected Bundle bundle;
    protected boolean loadingMoreMessages;

    protected SpeedDialView messageActionsSpeedDialView;
    protected MessageActionHandler messageActionHandler;

    protected MessagesList messagesList;
    protected MessagesListAdapter<MessageHolder> messageListAdapter;

    protected MessageInput messageInput;

    /**
     * If set to false in onCreate the menu threads wont be inflated in the menu.
     * This can be useful if you want to customize the action bar.
     */
    protected boolean inflateMenuItems = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activityLayout());

        initViews();

        if (!updateThreadFromBundle(savedInstanceState)) {
            return;
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

                    messageListAdapter.addToStart(new MessageHolder(message), true);

                    // Check if the text from the current user, If so return so we wont vibrate for the user messages.
//                    if (message.getSender().isMe() && isAdded) {
//                        scrollListTo(ListPosition.Bottom, layoutManager().findLastVisibleItemPosition() > messageListAdapter.size() - 2);
//                    }
//                    else {
//                        // If the user is near the bottom, then we scroll down when a text comes in
//                        if(layoutManager().findLastVisibleItemPosition() > messageListAdapter.size() - 5) {
//                            scrollListTo(ListPosition.Bottom, true);
//                        }
//                    }
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
//                    messageListAdapter.removeRow(networkEvent.message, true);
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
                        // Add this so that the message only appears after it's been sent
                        if (ChatSDK.encryption() == null) {
//                            if(messageListAdapter.addRow(progress.message, false, true, progress.uploadProgress, true)) {
//                                scrollListTo(ListPosition.Bottom, false);
//                            }
                        }
                    }
                    if (status == MessageSendStatus.Uploading || status == MessageSendStatus.Sent) {
                        reloadDataForMessage(progress.message);
                    }
        }));

        // Load the messages
        messageListAdapter.addToEnd(messageViewsFromMessages(thread.getMessages()), false);

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
        return R.layout.activity_chatkit_chat;
    }

    protected void initViews () {
        // Set up the text box - this is the box that sits above the keyboard



        messagesList = findViewById(R.id.messagesList);

        IncomingTextMessageViewHolder.Payload holderPayload = new IncomingTextMessageViewHolder.Payload();

        holderPayload.avatarClickListener = () -> Toast.makeText(this,
                "Text message avatar clicked", Toast.LENGTH_SHORT).show();

        MessageHolders holders = new MessageHolders()
                .setIncomingTextConfig(IncomingTextMessageViewHolder.class, R.layout.chatkit_item_incoming_text_message, holderPayload)
                .setOutcomingTextConfig(OutcomingTextMessageViewHolder.class, R.layout.chatkit_item_outcoming_text_message);
//                .setIncomingImageConfig(IncomingImageMessageViewHolder.class, R.layout.chatkit_item_incoming_image_message)
//                .setOutcomingImageConfig(OutcomingImageMessageViewHolder.class, R.layout.chatkit_item_outcoming_image_message);

        messageListAdapter = new MessagesListAdapter<>(ChatSDK.currentUserID(), holders, (imageView, url, payload) -> {
            Picasso.get().load(url).into(imageView);
        });

        messagesList.setAdapter(messageListAdapter);

        messageInput = findViewById(R.id.input);
        messageInput.setInputListener(input -> {
            sendMessage(String.valueOf(input));
            return true;
        });

        messageInput.setAttachmentsListener(() -> {
            showOptions();
        });

        messageInput.setTypingListener(new MessageInput.TypingListener() {
            @Override
            public void onStartTyping() {
                startTyping();
            }

            @Override
            public void onStopTyping() {
                stopTyping();
            }
        });

        setupChatActions();
    }

    protected void setupChatActions() {
        messageActionsSpeedDialView = findViewById(R.id.speed_dial_message_actions);
        messageActionHandler = new MessageActionHandler(messageActionsSpeedDialView);

        messageListAdapter.setOnMessageLongClickListener(message -> {
            // TODO:
        });

//        disposableList.add(messageListAdapter.getMessageActionObservable()
//                .flatMapSingle((Function<List<MessageAction>, SingleSource<String>>) messageActions -> {
//                    // Open the text action sheet
//                    hideKeyboard();
//                    return messageActionHandler.open(messageActions, CKChatActivity.this);
//        }).subscribe(this::showSnackbar, snackbarOnErrorConsumer()));
    }

//    public Completable loadMoreMessages (boolean loadFromServer, boolean saveScrollPosition, boolean notify) {
//        return Maybe.create((MaybeOnSubscribe<Date>) emitter -> {
//            if (loadingMoreMessages) {
//                emitter.onComplete();
//            } else {
//                loadingMoreMessages = true;
//
//                List<MessageListItem> items = messageListAdapter.getMessageItems();
//                if(items.size() > 0) {
//                    emitter.onSuccess(items.get(0).message.getDate().toDate());
//                } else {
//                    emitter.onSuccess(new Date(0));
//                }
//            }
//        }).flatMapCompletable(date -> ChatSDK.thread().loadMoreMessagesForThread(date, thread, loadFromServer)
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSuccess(messages -> {
//
//                    int addedCount = 0;
//                    for(Message m : messages) {
//                        if(messageListAdapter.addRow(m, false, false, false)) {
//                            addedCount++;
//                        }
//                    }
//                    if (notify) {
//                        reloadDataInRange(0, messages.size());
//                    }
//
//                    int firstVisible = layoutManager().findFirstVisibleItemPosition();
//                    int lastVisible = layoutManager().findLastVisibleItemPosition();
//
//                    if (addedCount > 0 && saveScrollPosition) {
//                        scrollListTo(messages.size() + lastVisible - firstVisible, false);
//                    }
//
//                    firstVisible = layoutManager().findFirstVisibleItemPosition();
//                    System.out.println(firstVisible);
//
//                    loadingMoreMessages = false;
//                }).ignoreElement());
//    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Message firstMessage = null;
        if (!thread.getMessages().isEmpty()) {
            firstMessage = thread.getMessages().get(0);
        }
        if (!loadingMoreMessages) {
            loadingMoreMessages = true;
            disposableList.add(ChatSDK.thread().loadMoreMessagesForThread(firstMessage != null ? firstMessage.getDate().toDate() : null, thread, true).subscribe(messages -> {
                messageListAdapter.addToEnd(messageViewsFromMessages(messages), true);
                loadingMoreMessages = false;
            }));
        }
    }

    @Override
    public void onSelectionChanged(int count) {

    }

    public ArrayList<MessageHolder> messageViewsFromMessages(List<Message> messages) {
        ArrayList<MessageHolder> messageHolders = new ArrayList<>();
        for (Message m: messages) {
            messageHolders.add(new MessageHolder(m));
        }
        return messageHolders;
    }

    /**
     * Send text text
     *
     * @param text to send.
     */
    public void sendMessage(String text) {

        if (text == null || text.isEmpty() || text.replace(" ", "").isEmpty()) {
            return;
        }

        handleMessageSend(ChatSDK.thread().sendMessageWithText(text.trim(), thread));
    }

    protected void handleMessageSend (Completable completable) {
        completable.observeOn(AndroidSchedulers.mainThread()).doOnError(throwable -> {
            ChatSDK.logError(throwable);
            ToastHelper.show(getApplicationContext(), throwable.getLocalizedMessage());
        }).subscribe(ChatSDK.shared().getCrashReporter());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }

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

    protected void reloadDataForMessage (Message message) {
        messageListAdapter.update(new MessageHolder(message));
    }

    @Override
    protected void onResume() {
        super.onResume();

        removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

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
                                setSubtitleText(CKChatActivity.this.getString(R.string.online));
                            } else {
                                setSubtitleText(String.format(getString(R.string.last_seen__), pt.format(date)));
                            }
                        }
                    }));
        }

        // Set up the UI to dismiss keyboard on touch event, Option and Send buttons are not included.
        // If list is scrolling we ignoring the touch event.
//        setupTouchUIToDismissKeyboard(findViewById(R.id.view_root), (v, event) -> {
//
//            // Using small delay for better accuracy in catching the scrolls.
//            v.postDelayed(() -> {
//                if (!scrolling) {
//                    hideKeyboard();
//                    stopTyping(false);
//                }
//            }, 300);
//
//            return false;
//        }, R.id.button_send, R.id.button_options);

        markRead();

        // We have to do this because otherwise if we background the app
        // we will miss any messages that came through while we were in
        // the background
//        loadMessages(messageListAdapter.getItemCount() == 0, -1, ListPosition.Bottom);

        // Show a local notification if the text is from a different thread
        ChatSDK.ui().setLocalNotificationHandler(thread -> !thread.getEntityID().equals(this.thread.getEntityID()));

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

        becomeInactive();
        markRead();

        if (thread != null && thread.typeIs(ThreadType.Public) && (removeUserFromChatOnExit || thread.metaValueForKey(Keys.Mute) != null)) {
            ChatSDK.thread().removeUsersFromThread(thread, ChatSDK.currentUser()).observeOn(AndroidSchedulers.mainThread()).subscribe(ChatSDK.shared().getCrashReporter());
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
        if (thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe()) {
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

        if (thread == null) {
            finish();
            return false;
        }

        return true;
    }

    /**
     * Update chat current thread using the {@link CKChatActivity#bundle} bundle saved.
     * Also calling the option menu to update it self. Used for showing the thread users icon if thread users amount is bigger then 2.
     * Finally update the action bar for thread messageImageView and name, The update will occur only if needed so free to call.
     */
    protected void updateChat() {
        updateThreadFromBundle(this.bundle);
        supportInvalidateOptionsMenu();
        initActionBar();
    }


    @Override
    public void sendAudio(Recording recording) {
        if(ChatSDK.audioMessage() != null) {
            handleMessageSend(ChatSDK.audioMessage().sendMessage(recording, thread));
        }
    }

    public void startTyping () {
        setChatState(TypingIndicatorHandler.State.composing);
    }

    public void becomeInactive () {
        setChatState(TypingIndicatorHandler.State.inactive);
    }

    @Override
    public void stopTyping() {
        setChatState(TypingIndicatorHandler.State.active);
    }

    @Override
    public void onKeyboardShow() {
//        scrollListTo(ListPosition.Bottom, false);
    }

    @Override
    public void onKeyboardHide() {
//        scrollListTo(ListPosition.Bottom, false);
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

    public void markAsDelivered(List<Message> messages){
        for (Message m : messages) {
            markAsDelivered(m);
        }
    }

    public void markAsDelivered(Message message){
        message.setMessageStatus(MessageSendStatus.Delivered);
    }

    @Override
    public void showOptions() {
        // We don't want to remove the user if we load another activity
        // Like the sticker activity
       removeUserFromChatOnExit = false;

       optionsHandler = ChatSDK.ui().getChatOptionsHandler(this);
       optionsHandler.show(this);
    }

    @Override
    public void hideOptions() {
        removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;
        if(optionsHandler != null) {
            optionsHandler.hide();
        }
    }

    @Override
    public void onSendPressed(String text) {
        sendMessage(text);
    }

    @Override
    public void executeChatOption(ChatOption option) {
        handleMessageSend(option.execute(this, thread));
    }

}
