/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Debug;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.stfalcon.chatkit.messages.MessageInput;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.TypingIndicatorHandler;
import sdk.chat.core.interfaces.ChatOption;
import sdk.chat.core.interfaces.ChatOptionsDelegate;
import sdk.chat.core.interfaces.ChatOptionsHandler;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.ActivityResultPushSubjectHolder;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.appbar.ChatActionBar;
import sdk.chat.ui.audio.AudioBinder;
import sdk.chat.ui.chat.model.ImageMessageHolder;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.interfaces.TextInputDelegate;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.views.ChatView;
import sdk.chat.ui.views.ReplyView;
import sdk.guru.common.RX;

@Deprecated // Use ChatActivityWrapper instead
public class ChatActivity extends BaseActivity implements TextInputDelegate, ChatOptionsDelegate, ChatView.Delegate {

    public static final int messageForwardActivityCode = 998;

    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker text screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = true;

    protected static boolean enableTrace = false;

    protected Thread thread;

    protected ChatActionBar chatActionBar;
    protected ChatView chatView;
    protected View divider;
    protected ReplyView replyView;
    protected MessageInput input;
    protected CoordinatorLayout viewContainer;
    protected MaterialSearchView searchView;
    protected FrameLayout root;
    protected LinearLayout messageInputLinearLayout;

    protected AudioBinder audioBinder = null;

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatActionBar = findViewById(R.id.chatActionBar);
        chatView = findViewById(R.id.chatView);
        divider = findViewById(R.id.divider);
        replyView = findViewById(R.id.replyView);
        input = findViewById(R.id.input);
        viewContainer = findViewById(R.id.viewContainer);
        searchView = findViewById(R.id.searchView);
        root = findViewById(R.id.root);
        messageInputLinearLayout = findViewById(R.id.messageInputLinearLayout);

        updateThread(savedInstanceState);
        initViews();

    }

    public void updateThread(Bundle bundle) {
        thread = null;

        if (bundle == null) {
            bundle = getIntent().getExtras();
        }

        if (bundle != null && bundle.containsKey(Keys.IntentKeyThreadEntityID)) {
            String threadEntityID = bundle.getString(Keys.IntentKeyThreadEntityID);
            if (threadEntityID != null) {
                thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
            }
        }

        if (thread == null) {
            finish();
        }

    }

    public void updateOptionsButton() {
        input.findViewById(R.id.attachmentButton).setVisibility(chatView.getSelectedMessages().isEmpty() ? View.VISIBLE : View.GONE);
        input.findViewById(R.id.attachmentButtonSpace).setVisibility(chatView.getSelectedMessages().isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void hideTextInput() {
        input.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        updateChatViewMargins();
    }

    public void showTextInput() {
        input.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        updateChatViewMargins();
    }

    public void hideReplyView() {
        if (audioBinder != null) {
            audioBinder.hideReplyView();
        }
        chatView.clearSelection();
        replyView.hide();
        updateOptionsButton();

        // We need this otherwise the margin isn't updated when the view is gone
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) chatView.getLayoutParams();
//        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, messageInputLinearLayout.getHeight() - replyView.getHeight());
//        chatView.setLayoutParams(params);

        updateChatViewMargins();

    }

    public void updateChatViewMargins() {
        input.post(() -> {
            int bottomMargin = 0;
            if (replyView.isVisible()) {
                bottomMargin += replyView.getHeight();
            }
            if (input.getVisibility() != View.GONE) {
                bottomMargin += input.getHeight() + divider.getHeight();
            }

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) chatView.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin);
            chatView.setLayoutParams(params);
        });
    }

    public void showReplyView(String title, String imageURL, String text) {
        updateOptionsButton();
        if (audioBinder != null) {
            audioBinder.showReplyView();
        }
        replyView.show(title, imageURL, text);

        // We need this otherwise the margin isn't updated when the view is gone
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) chatView.getLayoutParams();
//        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, messageInputLinearLayout.getHeight() + replyView.getHeight());
//        chatView.setLayoutParams(params);

        updateChatViewMargins();

    }

    @Override
    protected Bitmap getTaskDescriptionBitmap() {
        return super.getTaskDescriptionBitmap();
    }

    protected void initViews() {
        super.initViews();

        if (getThread() == null) {
            finish();
            return;
        }

        chatView.setDelegate(this);

        chatActionBar.onSearchClicked(v -> {
            searchView.showSearch();
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                chatView.filter(query);
                chatActionBar.hideSearchIcon();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chatView.filter(newText);
                chatActionBar.hideSearchIcon();
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                chatView.clearFilter();
                chatActionBar.showSearchIcon();
            }
        });

        chatView.initViews();

        if (UIModule.config().messageSelectionEnabled) {
            chatView.enableSelectionMode(count -> {
                invalidateOptionsMenu();
                updateOptionsButton();
            });
        }

        if (!hasVoice(ChatSDK.currentUser())) {
            hideTextInput();
        }

        if (ChatSDK.audioMessage() != null) {
            audioBinder = new AudioBinder(this, this, input);
        } else {
            input.setInputListener(input -> {
                sendMessage(String.valueOf(input));
                return true;
            });
        }

        input.setTypingListener(new MessageInput.TypingListener() {
            @Override
            public void onStartTyping() {
                startTyping();
            }

            @Override
            public void onStopTyping() {
                stopTyping();
            }
        });

        input.setAttachmentsListener(this::showOptions);

        replyView.setOnCancelListener(v -> hideReplyView());

        // Action bar
        chatActionBar.setOnClickListener(v -> {
            chatActionBar.setEnabled(false);
            openThreadDetailsActivity();
        });
        setSupportActionBar(chatActionBar.getToolbar());
        chatActionBar.reload(thread);

        setChatState(TypingIndicatorHandler.State.active);

        if (enableTrace) {
            Debug.startMethodTracing("chat");
        }

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadMetaUpdated, EventType.ThreadUserAdded, EventType.ThreadUserRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    chatActionBar.reload(thread);
                    // If we are added, we will get voice...
                    User user = networkEvent.getUser();
                    if (user != null && user.isMe()) {
                        showOrHideTextInputView();
                    }
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .filter(networkEvent -> thread.containsUser(networkEvent.getUser()))
                .subscribe(networkEvent -> {
                    reloadData();
                    chatActionBar.reload(thread);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    String typingText = networkEvent.getText();
                    if (typingText != null) {
                        typingText += getString(R.string.typing);
                    }
                    Logger.debug(typingText);
                    chatActionBar.setSubtitleText(thread, typingText);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterRoleUpdated(thread, ChatSDK.currentUser()))
                .subscribe(networkEvent -> {
                    invalidateOptionsMenu();
                    showOrHideTextInputView();
                }));


        if (chatView != null) {
            chatView.addListeners();
//            chatView.onLoadMore(0, 0);
        }

        invalidateOptionsMenu();
    }

    public boolean hasVoice(User user) {
        return ChatSDK.thread().hasVoice(thread, user) && !thread.isReadOnly();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void showOrHideTextInputView() {
        if (hasVoice(ChatSDK.currentUser())) {
            showTextInput();
        } else {
            hideTextInput();
        }
    }
    /**
     * Send text text
     *
     * @param text to send.
     */
    public void sendMessage(String text) {

        // Clear the draft text
        thread.setDraft(null);

        if (text == null || text.isEmpty() || text.replace(" ", "").isEmpty()) {
            return;
        }

        if (replyView.isVisible()) {
            MessageHolder holder = chatView.getSelectedMessages().get(0);
            handleMessageSend(ChatSDK.thread().replyToMessage(thread, holder.getMessage(), text));
            hideReplyView();
        } else {
            handleMessageSend(ChatSDK.thread().sendMessageWithText(text.trim(), thread));
        }

    }

    protected void handleMessageSend(Completable completable) {
        completable.observeOn(RX.main()).subscribe(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }

    }

    protected void reloadData() {
        chatView.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

        if (thread.typeIs(ThreadType.Public)) {
            User currentUser = ChatSDK.currentUser();
            ChatSDK.thread().addUsersToThread(thread, currentUser).subscribe();
        }

        chatActionBar.setSubtitleText(thread, null);
        chatActionBar.setEnabled(true);

        // Show a local notification if the text is from a different thread
        ChatSDK.ui().setLocalNotificationHandler(thread -> !thread.getEntityID().equals(this.thread.getEntityID()));

        if (audioBinder != null) {
            audioBinder.updateRecordMode();
        }

        if (!StringChecker.isNullOrEmpty(thread.getDraft())) {
            input.getInputEditText().setText(thread.getDraft());
        }

        // Put it here in the case that they closed the app with this screen open
        thread.markReadAsync().subscribe();
        showOrHideTextInputView();

    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();

        if (!StringChecker.isNullOrEmpty(input.getInputEditText().getText())) {
            thread.setDraft(input.getInputEditText().getText().toString());
        } else {
            thread.setDraft(null);
        }


    }

    /**
     * Sending a broadcast that the chat was closed, Only if there were new messageHolders on this chat.
     * This is used for example to update the thread list that messageHolders has been read.
     */
    @Override
    protected void onStop() {
        super.onStop();
        doOnStop();

    }

    protected void doOnStop() {
        becomeInactive();

        if (thread != null && thread.typeIs(ThreadType.Public) && (removeUserFromChatOnExit || thread.isMuted())) {
            // Don't add this to activity disposable map because otherwise it can be cancelled before completion
            ChatSDK.events().disposeOnLogout(ChatSDK.thread()
                    .removeUsersFromThread(thread, ChatSDK.currentUser())
                    .observeOn(RX.main()).subscribe());
        }
    }

    /**
     * Not used, There is a piece of code here that could be used to clean all images that was loaded for this chat from cache.
     */
    @Override
    protected void onDestroy() {
        if (enableTrace) {
            Debug.stopMethodTracing();
        }
        if (chatView != null) {
            chatView.removeListeners();
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        updateThread(intent.getExtras());

        clear();
        chatView.onLoadMore(0, 0);
        chatActionBar.reload(thread);
    }

    public void clear() {
        chatView.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (thread != null) {
            if (!chatView.getSelectedMessages().isEmpty()) {

                chatActionBar.hideSearchIcon();

                getMenuInflater().inflate(R.menu.activity_chat_actions_menu, menu);

                menu.findItem(R.id.action_copy).setIcon(Icons.get(this, Icons.choose().copy, Icons.shared().actionBarIconColor));
                menu.findItem(R.id.action_delete).setIcon(Icons.get(this, Icons.choose().delete, Icons.shared().actionBarIconColor));
                menu.findItem(R.id.action_forward).setIcon(Icons.get(this, Icons.choose().forward, Icons.shared().actionBarIconColor));
                menu.findItem(R.id.action_reply).setIcon(Icons.get(this, Icons.choose().reply, Icons.shared().actionBarIconColor));

                if (!UIModule.config().messageForwardingEnabled) {
                    menu.removeItem(R.id.action_forward);
                }

                if (!UIModule.config().messageReplyEnabled) {
                    menu.removeItem(R.id.action_reply);
                }

                if (chatView.getSelectedMessages().size() != 1) {
                    menu.removeItem(R.id.action_reply);
                }

                if (!hasVoice(ChatSDK.currentUser())) {
                    menu.removeItem(R.id.action_reply);
                    menu.removeItem(R.id.action_delete);
                    menu.removeItem(R.id.action_forward);
                }

                // Check that the messages could be deleted
                boolean canBeDeleted = true;
                for (MessageHolder holder: chatView.getSelectedMessages()) {
                    if (!ChatSDK.thread().canDeleteMessage(holder.getMessage())) {
                        canBeDeleted = false;
                    }
                }
                if (!canBeDeleted) {
                    menu.removeItem(R.id.action_delete);
                }

                chatActionBar.hideText();
            } else {

                chatActionBar.showSearchIcon();

                if (ChatSDK.thread().canAddUsersToThread(thread)) {
                    getMenuInflater().inflate(R.menu.add_menu, menu);
                    menu.findItem(R.id.action_add).setIcon(Icons.get(this, Icons.choose().add, Icons.shared().actionBarIconColor));
                }

                chatActionBar.showText();
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            List<MessageHolder> holders = chatView.getSelectedMessages();
            ChatSDK.thread().deleteMessages(MessageHolder.toMessages(holders)).subscribe(this);
            clearSelection();
        }
        if (id == R.id.action_copy) {
            chatView.copySelectedMessagesText(this, holder -> {
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", CurrentLocale.get());
                return dateFormatter.format(holder.getCreatedAt()) + ", " + holder.getUser().getName() + ": " + holder.getText();
            }, false);
            showToast(R.string.copied_to_clipboard);
        }
        if (id == R.id.action_forward) {

            List<MessageHolder> holders = chatView.getSelectedMessages();

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

            // We don't want to remove the user if we load another activity
            // Like the sticker activity
            removeUserFromChatOnExit = false;

            ChatSDK.ui().startForwardMessageActivityForResult(this, thread, MessageHolder.toMessages(holders), messageForwardActivityCode);
            clearSelection();
        }

        if (id == R.id.action_reply) {
            MessageHolder holder = chatView.getSelectedMessages().get(0);
            String imageURL = null;
            if (holder instanceof ImageMessageHolder) {
                imageURL = ((ImageMessageHolder) holder).getImageUrl();
            }
            showReplyView(holder.getUser().getName(), imageURL, holder.getText());
            input.requestFocus();
            showKeyboard();
        }

        if (id == R.id.action_add) {

            // We don't want to remove the user if we load another activity
            // Like the sticker activity
            removeUserFromChatOnExit = false;

            ChatSDK.ui().startAddUsersToThreadActivity(this, thread.getEntityID());
        }

        return super.onOptionsItemSelected(item);
    }

    public void clearSelection() {
        chatView.clearSelection();
        updateOptionsButton();
    }

    /**
     * Open the thread details context, Admin user can change thread name an messageImageView there.
     */
    protected void openThreadDetailsActivity() {

        // We don't want to remove the user if we load another activity
        // Like the sticker activity
        removeUserFromChatOnExit = false;

        ChatSDK.ui().startThreadDetailsActivity(this, thread.getEntityID());
    }

    @Override
    public void sendAudio(final File file, String mimeType, long duration) {
        if (ChatSDK.audioMessage() != null) {
            handleMessageSend(ChatSDK.audioMessage().sendMessage(this, file, mimeType, duration, thread));
        }
    }

    public void startTyping() {
        setChatState(TypingIndicatorHandler.State.composing);
    }

    public void becomeInactive() {
        setChatState(TypingIndicatorHandler.State.inactive);
    }

    public void stopTyping() {
        setChatState(TypingIndicatorHandler.State.active);
    }

    protected void setChatState(TypingIndicatorHandler.State state) {
        if (ChatSDK.typingIndicator() != null) {
            ChatSDK.typingIndicator().setChatState(state, thread)
                    .observeOn(RX.main())
                    .doOnError(throwable -> {
                        System.out.println("Catch disconnected error");
                        //
                    })
                    .subscribe();
        }
    }

    /**
     * Show the option popup when the add_menu key is pressed.
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

    public void showOptions() {
        // We don't want to remove the user if we load another activity
        // Like the sticker activity
        removeUserFromChatOnExit = false;

        optionsHandler = ChatSDK.ui().getChatOptionsHandler(this);
        optionsHandler.show(this);
    }

    @Override
    public void executeChatOption(ChatOption option) {
        handleMessageSend(option.execute(this, thread));
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    @Override
    public void onClick(Message message) {
        ChatSDKUI.shared().getMessageCustomizer().onClick(this, root, message);
    }

    @Override
    public void onLongClick(Message message) {
        ChatSDKUI.shared().getMessageCustomizer().onLongClick(this, root, message);
    }

    @Override
    public void onBackPressed() {
        // Do this so that even if we were editing the thread, we always go back to the
        // main activity
        ChatSDK.ui().startMainActivity(this);
    }

}
