/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.maps.model.LatLng;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.stfalcon.chatkit.messages.MessageInput;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.ocpsoft.prettytime.PrettyTime;
import org.pmw.tinylog.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
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
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;

import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.audio.AudioBinder;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ToastHelper;
import co.chatsdk.ui.views.ChatView;
import co.chatsdk.ui.interfaces.TextInputDelegate;
import co.chatsdk.ui.databinding.ActivityChatBinding;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ChatActivity extends BaseActivity implements TextInputDelegate, ChatOptionsDelegate, ChatView.Delegate {

    public static final int messageForwardActivityCode = 998;

    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker text screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

    protected static boolean enableTrace = false;

    protected Thread thread;

    protected Bundle bundle;

    ActivityChatBinding b;

    protected DisplayMetrics displayMetrics = new DisplayMetrics();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        if (!updateThreadFromBundle(savedInstanceState)) {
            return;
        }

        b.chatView.setDelegate(this);

        b.chatActionBar.onSearchClicked(v -> {
          b.searchView.showSearch();
        });

        b.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                b.chatView.filter(query);
                b.chatActionBar.hideSearchIcon();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                b.chatView.filter(newText);
                b.chatActionBar.hideSearchIcon();
                return false;
            }
        });

        b.searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                b.chatView.clearFilter();
                b.chatActionBar.showSearchIcon();
            }
        });

        initViews();

        setChatState(TypingIndicatorHandler.State.active);

        if(enableTrace) {
            android.os.Debug.startMethodTracing("chat");
        }

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

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        thread.markRead();

        b.chatView.enableSelectionMode(count -> {
            invalidateOptionsMenu();
        });

    }

    public void hideReplyView() {
        b.chatView.clearSelection();
        b.replyView.hide();
    }

    @Override
    protected Bitmap getTaskDescriptionBitmap() {
        return super.getTaskDescriptionBitmap();
    }

    protected @LayoutRes int getLayout() {
        return R.layout.activity_chat;
    }

    protected void initViews () {
        super.initViews();

        b.chatView.initViews();

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

        if (ChatSDK.audioMessage() != null) {
            new AudioBinder(this, b.input.getButton(), b.input.getInputEditText());
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

        if (b.replyView.isVisible()) {
            Message message = b.chatView.getSelectedMessages().get(0);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

//        dm.add(PermissionRequestHandler.requestRecordAudio(this).subscribe(() -> {
//
//        }, throwable -> ToastHelper.show(this, throwable.getLocalizedMessage())));

    }

    protected void reloadData () {
        b.chatView.notifyDataSetChanged();
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
                                b.chatActionBar.setSubtitleText(thread, ChatActivity.this.getString(R.string.online));
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
        b.chatView.onLoadMore(0, 0);
        b.chatActionBar.reload(thread);
    }

    public void clear() {
        b.chatView.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!b.chatView.getSelectedMessages().isEmpty()) {
            getMenuInflater().inflate(R.menu.activity_chat_actions_menu, menu);

            menu.findItem(R.id.action_copy).setIcon(Icons.get(Icons.choose().copy, R.color.app_bar_icon_color));
            menu.findItem(R.id.action_delete).setIcon(Icons.get(Icons.choose().delete, R.color.app_bar_icon_color));
            menu.findItem(R.id.action_forward).setIcon(Icons.get(Icons.choose().forward, R.color.app_bar_icon_color));
            menu.findItem(R.id.action_reply).setIcon(Icons.get(Icons.choose().reply, R.color.app_bar_icon_color));

            if (b.chatView.getSelectedMessages().size() != 1) {
                menu.removeItem(R.id.action_reply);
            }

            // Check that the messages could be deleted
            boolean canBeDeleted = true;
            for (Message message: b.chatView.getSelectedMessages()) {
                if (!ChatSDK.thread().deleteMessageEnabled(message)) {
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
            List<Message> messages = b.chatView.getSelectedMessages();
            ChatSDK.thread().deleteMessages(messages).subscribe(this);
            b.chatView.clearSelection();
        }
        if (id == R.id.action_copy) {
            b.chatView.copySelectedMessagesText(this, holder -> {
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return dateFormatter.format(holder.getCreatedAt()) + ", " + holder.getUser().getName() + ": " + holder.getText();
            }, false);
            showToast(R.string.copied_to_clipboard);
        }
        if (id == R.id.action_forward) {

            List<Message> messages = b.chatView.getSelectedMessages();

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
            b.chatView.clearSelection();
        };

        if (id == R.id.action_reply) {
            Message message = b.chatView.getSelectedMessages().get(0);
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

    @Override
    public void sendAudio(Recording recording) {
        if(ChatSDK.audioMessage() != null) {
//            handleMessageSend(ChatSDK.audioMessage().sendMessage(recording, thread));
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

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    @Override
    public void onClick(Message message) {
        Customiser.shared().onClick(this, b.getRoot(), message);
    }

    @Override
    public void onLongClick(Message message) {
        Customiser.shared().onLongClick(this, b.getRoot(), message);
    }

    @Override
    public void onBackPressed() {
        // Do this so that even if we were editing the thread, we always go back to the
        // main activity
        ChatSDK.ui().startMainActivity(this);
    }


}
