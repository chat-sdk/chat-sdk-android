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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActionBar;
import co.chatsdk.ui.chat.TextInputDelegate;
import co.chatsdk.ui.chat.message_action.MessageActionHandler;
import co.chatsdk.ui.chatkit.custom.IncomingTextMessageViewHolder;
import co.chatsdk.ui.chatkit.custom.OutcomingTextMessageViewHolder;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.chatkit.model.MessageHolder;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class CKChatActivity extends BaseActivity implements TextInputDelegate, ChatOptionsDelegate,
        MessagesListAdapter.OnLoadMoreListener {

    public static final int ADD_USERS = 103;
    public static final int SHOW_DETAILS = 200;

    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker text screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

    protected static boolean enableTrace = false;

    protected Thread thread;

    protected Bundle bundle;
    protected boolean loadingMoreMessages;

    protected SpeedDialView messageActionsSpeedDialView;
    protected MessageActionHandler messageActionHandler;

    protected MessagesList messagesList;
    protected MessagesListAdapter<MessageHolder> messagesListAdapter;

    protected MessageInput messageInput;

    protected ArrayList<MessageHolder> messageHolders = new ArrayList<>();
    protected PrettyTime prettyTime = new PrettyTime();

    protected ChatActionBar chatActionBar;

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
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    Message message = networkEvent.message;

                    messagesListAdapter.addToStart(new MessageHolder(message), true);

                    // Check if the text from the current user, If so return so we wont vibrate for the user messageHolders.
//                    if (message.getSender().isMe() && isAdded) {
//                        scrollListTo(ListPosition.Bottom, layoutManager().findLastVisibleItemPosition() > messagesListAdapter.size() - 2);
//                    }
//                    else {
//                        // If the user is near the bottom, then we scroll down when a text comes in
//                        if(layoutManager().findLastVisibleItemPosition() > messagesListAdapter.size() - 5) {
//                            scrollListTo(ListPosition.Bottom, true);
//                        }
//                    }
                    message.markRead();
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadReadReceiptUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    Message message = networkEvent.message;

                    if (ChatSDK.readReceipts() != null && message.getSender().isMe()) {
                        reloadDataForMessage(message);
                    }
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
//                    messagesListAdapter.removeRow(networkEvent.message, true);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadDetailsUpdated, EventType.ThreadUsersChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> chatActionBar.reload(thread)));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .filter(networkEvent -> thread.containsUser(networkEvent.user))
                .subscribe(networkEvent -> {
                    reloadData();
                    chatActionBar.reload(thread);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    String typingText = networkEvent.text;
                    if(typingText != null) {
                        typingText += getString(R.string.typing);
                    }
                    Timber.v(typingText);
                    chatActionBar.setSubtitleText(thread, typingText);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageSendStatusChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    MessageSendProgress progress = networkEvent.getMessageSendProgress();
                    MessageSendStatus status = progress.getStatus();

                    if (status == MessageSendStatus.Sending || status == MessageSendStatus.Created) {
                        // Add this so that the message only appears after it's been sent
                        if (ChatSDK.encryption() == null) {
//                            if(messagesListAdapter.addRow(progress.message, false, true, progress.uploadProgress, true)) {
//                                scrollListTo(ListPosition.Bottom, false);
//                            }
                        }
                    }
                    if (status == MessageSendStatus.Uploading || status == MessageSendStatus.Sent) {
                        reloadDataForMessage(progress.message);
                    }
        }));

        onLoadMore(0, 0);

        thread.markRead();

    }

    @Override
    protected Bitmap getTaskDescriptionBitmap() {
        return super.getTaskDescriptionBitmap();
    }

    protected ActionBar readyActionBarToCustomView () {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowHomeEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayShowCustomEnabled(true);
        }
        return ab;
    }

    protected void initActionBar () {

        final ActionBar ab = readyActionBarToCustomView();

        // http://stackoverflow.com/questions/16026818/actionbar-custom-view-with-centered-imageview-action-items-on-sides

        if (chatActionBar == null) {
            chatActionBar = new ChatActionBar(getLayoutInflater());
            chatActionBar.setOnClickListener(this::openThreadDetailsActivity);

            ab.setCustomView(chatActionBar.get());
        }

        chatActionBar.reload(thread);
    }

    protected @LayoutRes int activityLayout() {
        return R.layout.chatkit_activity_chat;
    }

    protected void initViews () {

        messagesList = findViewById(R.id.messagesList);

        IncomingTextMessageViewHolder.Payload holderPayload = new IncomingTextMessageViewHolder.Payload();

        holderPayload.avatarClickListener = () -> Toast.makeText(this,
                "Text message avatar clicked", Toast.LENGTH_SHORT).show();

        MessageHolders holders = new MessageHolders()
                .setIncomingTextConfig(IncomingTextMessageViewHolder.class, R.layout.chatkit_item_incoming_text_message, holderPayload)
                .setOutcomingTextConfig(OutcomingTextMessageViewHolder.class, R.layout.chatkit_item_outcoming_text_message);
//                .setIncomingImageConfig(IncomingImageMessageViewHolder.class, R.layout.chatkit_item_incoming_image_message)
//                .setOutcomingImageConfig(OutcomingImageMessageViewHolder.class, R.layout.chatkit_item_outcoming_image_message);

        messagesListAdapter = new MessagesListAdapter<>(ChatSDK.currentUserID(), holders, (imageView, url, payload) -> {
            Picasso.get().load(url).into(imageView);
        });
        messagesListAdapter.setLoadMoreListener(this);
        messagesListAdapter.setDateHeadersFormatter(date -> prettyTime.format(date));

        messagesListAdapter.enableSelectionMode(count -> {
            invalidateOptionsMenu();
        });

        messagesList.setAdapter(messagesListAdapter);


        messageInput = findViewById(R.id.input);
        messageInput.setInputListener(input -> {
            sendMessage(String.valueOf(input));
            return true;
        });

        messageInput.setAttachmentsListener(this::showOptions);

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
//        messageActionsSpeedDialView = findViewById(R.id.speed_dial_message_actions);
//        messageActionHandler = new MessageActionHandler(messageActionsSpeedDialView);

//        messagesListAdapter.setOnMessageLongClickListener(message -> {
//            // TODO:
//        });

//        dm.add(messagesListAdapter.getMessageActionObservable()
//                .flatMapSingle((Function<List<MessageAction>, SingleSource<String>>) messageActions -> {
//                    // Open the text action sheet
//                    hideKeyboard();
//                    return messageActionHandler.open(messageActions, CKChatActivity.this);
//        }).subscribe(this::showSnackbar, snackbarOnErrorConsumer()));
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Date loadFromDate = null;
        if (totalItemsCount != 0) {
            // This list has the newest first
            loadFromDate = messageHolders.get(messageHolders.size()-1).getCreatedAt();
        }

        if (!loadingMoreMessages) {
            loadingMoreMessages = true;
            dm.add(ChatSDK.thread()
                    .loadMoreMessagesForThread(loadFromDate, thread, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(messages -> {
                        List<MessageHolder> holders = MessageHolder.toHolders(messages);
                        messageHolders.addAll(holders);

                        messagesListAdapter.addToEnd(holders, false);
                        loadingMoreMessages = false;
            }));
        }
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

    protected void reloadData () {
        messagesListAdapter.notifyDataSetChanged();
    }

//    protected void reloadDataInRange (int startingIndex, int itemCount) {
//        messagesListAdapter.notifyItemRangeChanged(startingIndex, itemCount);
//    }

    protected void reloadDataForMessage (Message message) {
        messagesListAdapter.update(new MessageHolder(message));
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
                    .subscribe(new CrashReportingCompletableObserver(dm));
        }

        if (thread.typeIs(ThreadType.Private1to1) && thread.otherUser() != null && ChatSDK.lastOnline() != null) {
            dm.add(ChatSDK.lastOnline().getLastOnline(thread.otherUser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((date, throwable) -> {
                        if (throwable == null && date != null) {
                            Locale current = getResources().getConfiguration().locale;
                            PrettyTime pt = new PrettyTime(current);
                            if (thread.otherUser().getIsOnline()) {
                                chatActionBar.setSubtitleText(thread, CKChatActivity.this.getString(R.string.online));
                            } else {
                                chatActionBar.setSubtitleText(thread, String.format(getString(R.string.last_seen__), pt.format(date)));
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


        // We have to do this because otherwise if we background the app
        // we will miss any messageHolders that came through while we were in
        // the background
//        loadMessages(messagesListAdapter.getItemCount() == 0, -1, ListPosition.Bottom);

        // Show a local notification if the text is from a different thread
        ChatSDK.ui().setLocalNotificationHandler(thread -> !thread.getEntityID().equals(this.thread.getEntityID()));

    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    /**
     * Sending a broadcast that the chat was closed, Only if there were new messageHolders on this chat.
     * This is used for example to update the thread list that messageHolders has been read.
     */
    @Override
    protected void onStop() {
        super.onStop();

        becomeInactive();

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

        if (messagesListAdapter != null)
            messagesListAdapter.clear();

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

                    if (messagesListAdapter != null) {
                        messagesListAdapter.clear();
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

        if (messagesListAdapter != null && !messagesListAdapter.getSelectedMessages().isEmpty()) {
            getMenuInflater().inflate(R.menu.chatkit_chat_actions, menu);

            if (messagesListAdapter.getSelectedMessages().size() != 1) {
                menu.removeItem(R.id.action_reply);
            }

            // Check that the messages could be deleted
            boolean canBeDeleted = true;
            for (MessageHolder holder: messagesListAdapter.getSelectedMessages()) {
                if (!ChatSDK.thread().deleteMessageEnabled(holder.getMessage())) {
                    canBeDeleted = false;
                }
            }
            if (!canBeDeleted) {
                menu.removeItem(R.id.action_delete);
            }

            chatActionBar.hideText();
        } else {
            chatActionBar.showText();
            // Adding the add user option only if group chat is enabled.
            if (thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe()) {
                MenuItem item = menu.add(Menu.NONE, R.id.action_add, 10, getString(R.string.chat_activity_show_users_item_text));
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                item.setIcon(R.drawable.ic_plus);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            List<Message> messages = MessageHolder.toMessages(messagesListAdapter.getSelectedMessages());
            dm.add(ChatSDK.thread().deleteMessages(messages).subscribe(() -> {}, toastOnErrorConsumer()));
            messagesListAdapter.unselectAllItems();
        }
        if (id == R.id.action_copy) {
            messagesListAdapter.copySelectedMessagesText(this, holder -> {
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return dateFormatter.format(holder.getCreatedAt()) + ", " + holder.getUser().getName() + ": " + holder.getText();
            }, false);
            showToast(R.string.copied_to_clipboard);
        }
        if (id == R.id.action_forward) {
        }
        if (id == R.id.action_reply) {
        }
        if (id == R.id.action_add) {
            startAddUsersActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open the add users context, Here you can see your contact list and add users to this chat.
     * The default add users context will remove contacts that is already in this chat.
     */
    protected void startAddUsersActivity() {
        ChatSDK.ui().startAddUsersToThreadActivity(this, thread.getEntityID());
    }

    /**
     * Open the thread details context, Admin user can change thread name an messageImageView there.
     */
    protected void openThreadDetailsActivity() {
        ChatSDK.ui().startThreadDetailsActivity(this, thread.getEntityID());
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
                    .subscribe(new CrashReportingCompletableObserver(dm));
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
