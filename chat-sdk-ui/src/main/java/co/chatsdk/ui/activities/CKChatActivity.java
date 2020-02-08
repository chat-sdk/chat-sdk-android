/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.stfalcon.chatkit.commons.models.MessageContentType;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.ocpsoft.prettytime.PrettyTime;
import org.pmw.tinylog.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
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
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;

import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ImageMessageOnClickHandler;
import co.chatsdk.ui.chat.LocationMessageOnClickHandler;
import co.chatsdk.ui.chat.TextInputDelegate;
import co.chatsdk.ui.chat.model.ImageMessageHolder;
import co.chatsdk.ui.chat.view_holders.IncomingImageMessageViewHolder;
import co.chatsdk.ui.chat.view_holders.IncomingTextMessageViewHolder;
import co.chatsdk.ui.chat.view_holders.OutcomingImageMessageViewHolder;
import co.chatsdk.ui.chat.view_holders.OutcomingTextMessageViewHolder;
import co.chatsdk.ui.databinding.ChatkitActivityChatBinding;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import co.chatsdk.ui.chat.model.MessageHolder;


public class CKChatActivity extends BaseActivity implements TextInputDelegate, ChatOptionsDelegate,
        MessagesListAdapter.OnLoadMoreListener {

    public static final int messageForwardActivityCode = 998;

    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker text screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

    protected static boolean enableTrace = false;

    protected Thread thread;

    protected Bundle bundle;

    ChatkitActivityChatBinding b;

    protected MessagesListAdapter<MessageHolder> messagesListAdapter;

    protected HashMap<Message, MessageHolder> messageHolderHashMap = new HashMap<>();
    protected ArrayList<MessageHolder> messageHolders = new ArrayList<>();

    protected PrettyTime prettyTime = new PrettyTime();


    protected DisplayMetrics displayMetrics = new DisplayMetrics();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

        if (!updateThreadFromBundle(savedInstanceState)) {
            return;
        }

        initViews();

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
                    addMessageToStartOrUpdate(message);
                    message.markRead();
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadReadReceiptUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    Message message = networkEvent.message;

                    if (ChatSDK.readReceipts() != null && message.getSender().isMe()) {
                        addMessageToStartOrUpdate(message);
                    }
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    removeMessage(networkEvent.message);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadDetailsUpdated, EventType.ThreadUsersChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> b.chatActionBar.reload(thread)));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .filter(networkEvent -> thread.containsUser(networkEvent.user))
                .subscribe(networkEvent -> {
                    reloadData();
                    b.chatActionBar.reload(thread);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    String typingText = networkEvent.text;
                    if(typingText != null) {
                        typingText += getString(R.string.typing);
                    }
                    Logger.debug(typingText);
                    b.chatActionBar.setSubtitleText(thread, typingText);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageSendStatusChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    MessageSendProgress progress = networkEvent.getMessageSendProgress();
                    addMessageToStartOrUpdate(progress.message, progress);
        }));

        onLoadMore(0, 0);

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        thread.markRead();

    }

    protected int maxImageWidth() {
        // Prevent overly big messages in landscape mode
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            return Math.round(displayMetrics.widthPixels);
        } else {
            return Math.round(displayMetrics.heightPixels);
        }
    }

    @Override
    protected Bitmap getTaskDescriptionBitmap() {
        return super.getTaskDescriptionBitmap();
    }

    protected @LayoutRes int getLayout() {
        return R.layout.chatkit_activity_chat;
    }

    protected void initViews () {
        super.initViews();

        IncomingTextMessageViewHolder.Payload holderPayload = new IncomingTextMessageViewHolder.Payload();
        holderPayload.avatarClickListener = user -> {
            ChatSDK.ui().startProfileActivity(this, user.getEntityID());
        };

        MessageHolders holders = new MessageHolders()
                .setIncomingTextConfig(IncomingTextMessageViewHolder.class, R.layout.chatkit_item_incoming_text_message, holderPayload)
                .setOutcomingTextConfig(OutcomingTextMessageViewHolder.class, R.layout.chatkit_item_outcoming_text_message, null)
                .setIncomingImageConfig(IncomingImageMessageViewHolder.class, R.layout.chatkit_item_incoming_image_message, null)
                .setOutcomingImageConfig(OutcomingImageMessageViewHolder.class, R.layout.chatkit_item_outcoming_image_message);

        messagesListAdapter = new MessagesListAdapter<>(ChatSDK.currentUserID(), holders, (imageView, url, payload) -> {
            if (url == null || url.isEmpty()) {
                if (payload == null) {
                    imageView.setImageResource(R.drawable.icn_100_profile);
                } else if (payload instanceof ImageMessageHolder) {
                    imageView.setImageResource(R.drawable.icn_200_image_message_placeholder);
                }
            } else {

                RequestCreator request = Picasso.get().load(url)
                        .resize(maxImageWidth(), maxImageWidth());

                if (payload == null) {
                    request.placeholder(R.drawable.icn_100_profile);
                } else if (payload instanceof ImageMessageHolder) {
                    request.placeholder(R.drawable.icn_200_image_message_placeholder);
                }

                request.into(imageView);
            }
        });

        messagesListAdapter.setLoadMoreListener(this);
        messagesListAdapter.setDateHeadersFormatter(date -> prettyTime.format(date));

        messagesListAdapter.setOnMessageClickListener(message -> {
            if (message instanceof MessageContentType.Image) {

                MessageContentType.Image content = (MessageContentType.Image) message;

                if (content.getImageUrl() != null) {
                    if (message.getMessage().getMessageType().is(MessageType.Image)) {
                        View rootView = getContentView();
                        ImageMessageOnClickHandler.onClick(this, rootView, content.getImageUrl());
                    }
                    if (message.getMessage().getMessageType().is(MessageType.Location)) {

                        double longitude = message.getMessage().doubleForKey(Keys.MessageLongitude);
                        double latitude = message.getMessage().doubleForKey(Keys.MessageLatitude);

                        LocationMessageOnClickHandler.onClick(this, new LatLng(latitude, longitude));
                    }
                }
            }
        });

        messagesListAdapter.enableSelectionMode(count -> {
            invalidateOptionsMenu();
        });

        b.messagesList.setAdapter(messagesListAdapter);

        b.input.setInputListener(input -> {
            sendMessage(String.valueOf(input));
            return true;
        });

        b.input.setAttachmentsListener(this::showOptions);

        b.input.setTypingListener(new MessageInput.TypingListener() {
            @Override
            public void onStartTyping() {
                startTyping();
            }

            @Override
            public void onStopTyping() {
                stopTyping();
            }
        });

        b.replyView.setOnCancelListener(v -> hideReplyView());

        // Action bar
        b.chatActionBar.setOnClickListener(v -> openThreadDetailsActivity());
        setSupportActionBar(b.chatActionBar.getToolbar());
        b.chatActionBar.reload(thread);

    }

    public void hideReplyView() {
        messagesListAdapter.unselectAllItems();
        b.replyView.hide();
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Date loadFromDate = null;
        if (totalItemsCount != 0) {
            // This list has the newest first
            loadFromDate = messageHolders.get(messageHolders.size()-1).getCreatedAt();
        }
        dm.add(ChatSDK.thread()
                .loadMoreMessagesForThread(loadFromDate, thread, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addMessagesToEnd));
    }

    public void removeMessage(Message message) {
        MessageHolder holder = messageHolderHashMap.get(message);

        if (holder != null) {
            messagesListAdapter.delete(holder);
            messageHolders.remove(holder);
            messageHolderHashMap.remove(message);
        }

        updatePrevious(message);
        updateNext(message);
    }

    public void updatePrevious(Message message) {
        MessageHolder holder = previous(message);
        if (holder != null) {
            messagesListAdapter.update(holder);
        }
    }

    public MessageHolder previous(Message message) {
        Message previous = message.getPreviousMessage();
        if (previous != null) {
            return messageHolderHashMap.get(previous);
        }
        return null;
    }

    public void updateNext(Message message) {
        MessageHolder holder = next(message);
        if (holder != null) {
            messagesListAdapter.update(holder);
        }
    }

    public MessageHolder next(Message message) {
        Message next = message.getNextMessage();
        if (next != null) {
            return messageHolderHashMap.get(next);
        }
        return null;
    }

    public void addMessageToStartOrUpdate(Message message) {
        addMessageToStartOrUpdate(message, null);
    }

    public void addMessageToStartOrUpdate(Message message, MessageSendProgress progress) {
        MessageHolder holder = messageHolderHashMap.get(message);

        if (holder == null) {
            holder = MessageHolder.fromMessage(message);

            messageHolders.add(0, holder);
            messageHolderHashMap.put(holder.getMessage(), holder);

            // This means that we only scroll down if we were already at the bottom
            // it can be annoying if you have scrolled up and then a new message
            // comes in and scrolls the screen down
            boolean scroll = message.getSender().isMe();

            RecyclerView.LayoutManager layoutManager = b.messagesList.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager llm = (LinearLayoutManager) layoutManager;

                if (llm.findLastVisibleItemPosition() > messageHolders.size() - 5) {
                    scroll = true;
                }
            }
            messagesListAdapter.addToStart(holder, scroll);

            // Update the previous holder so that we can hide the
            // name if necessary
            updatePrevious(message);

        } else {
            holder.setProgress(progress);
            messagesListAdapter.update(holder);
        }
    }

    public void addMessagesToEnd(List<Message> messages) {
        // Check to see if the holders already exist
        ArrayList<MessageHolder> holders = new ArrayList<>();
        for (Message message: messages) {
            MessageHolder holder = messageHolderHashMap.get(message);
            if (holder == null) {
                holder = MessageHolder.fromMessage(message);
                messageHolderHashMap.put(message, holder);
                holders.add(holder);
            }
        }
        messageHolders.addAll(holders);
        messagesListAdapter.addToEnd(holders, false);
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

        if (b.replyView.isVisible()) {
            Message message = MessageHolder.toMessages(messagesListAdapter.getSelectedMessages()).get(0);
            handleMessageSend(ChatSDK.thread().replyToMessage(thread, message, text));
            hideReplyView();
        }
        else {
            handleMessageSend(ChatSDK.thread().sendMessageWithText(text.trim(), thread));
        }
    }

    protected void handleMessageSend (Completable completable) {
        completable.observeOn(AndroidSchedulers.mainThread()).subscribe(this);
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
                    .subscribe(this);
        }

        if (thread.typeIs(ThreadType.Private1to1) && thread.otherUser() != null && ChatSDK.lastOnline() != null) {
            dm.add(ChatSDK.lastOnline().getLastOnline(thread.otherUser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((date, throwable) -> {
                        if (throwable == null && date != null) {
                            Locale current = getResources().getConfiguration().locale;
                            PrettyTime pt = new PrettyTime(current);
                            if (thread.otherUser().getIsOnline()) {
                                b.chatActionBar.setSubtitleText(thread, CKChatActivity.this.getString(R.string.online));
                            } else {
                                b.chatActionBar.setSubtitleText(thread, String.format(getString(R.string.last_seen__), pt.format(date)));
                            }
                        }
                    }));
        } else {
            b.chatActionBar.setSubtitleText(thread, null);
        }

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

        if (thread != null && thread.typeIs(ThreadType.Public) && (removeUserFromChatOnExit || thread.isMuted())) {
            ChatSDK.thread()
                    .removeUsersFromThread(thread, ChatSDK.currentUser())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(this);
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

        clear();
    }

    public void clear() {
        if (messagesListAdapter != null) {
            messageHolderHashMap.clear();
            messageHolders.clear();
            messagesListAdapter.clear();
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

            b.chatActionBar.hideText();
        } else {
            b.chatActionBar.showText();
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            List<Message> messages = MessageHolder.toMessages(messagesListAdapter.getSelectedMessages());
            ChatSDK.thread().deleteMessages(messages).subscribe(this);
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

            List<Message> messages = MessageHolder.toMessages(messagesListAdapter.getSelectedMessages());

            dm.put(messageForwardActivityCode, ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {
                if (activityResult.requestCode == messageForwardActivityCode) {
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        showToast(R.string.success);
                    } else {
                        if (activityResult.data != null) {
                            String errorMessage = activityResult.data.getStringExtra(Keys.IntentKeyErrorMessage);
                            showToast(errorMessage);
                        }
                    }
                    dm.dispose(messageForwardActivityCode);
                }
            }));
            ChatSDK.ui().startForwardMessageActivityForResult(this, thread, messages, messageForwardActivityCode);
            messagesListAdapter.unselectAllItems();
        };

        if (id == R.id.action_reply) {
            Message message = messagesListAdapter.getSelectedMessages().get(0).getMessage();
            b.replyView.show(message.getSender().getName(), message.imageURL(), message.getText());
        }

        return super.onOptionsItemSelected(item);
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
                    .subscribe(this);
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
