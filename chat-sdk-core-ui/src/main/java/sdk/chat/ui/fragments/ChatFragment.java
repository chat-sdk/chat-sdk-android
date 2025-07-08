package sdk.chat.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentContainerView;

import com.lassi.common.utils.KeyUtils;
import com.stfalcon.chatkit.messages.MessageInput;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;
import materialsearchview.MaterialSearchView;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.TypingIndicatorHandler;
import sdk.chat.core.interfaces.ChatOption;
import sdk.chat.core.interfaces.ChatOptionsDelegate;
import sdk.chat.core.interfaces.ChatOptionsHandler;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.chat.core.ui.Sendable;
import sdk.chat.core.utils.ActivityResultPushSubjectHolder;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.activities.preview.ChatPreviewActivity;
import sdk.chat.ui.appbar.ChatActionBar;
import sdk.chat.ui.audio.AudioBinder;
import sdk.chat.ui.chat.model.ImageMessageHolder;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.interfaces.TextInputDelegate;
import sdk.chat.ui.keyboard.ChatFragmentKeyboardOverlayHelper;
import sdk.chat.ui.keyboard.KeyboardAwareFrameLayout;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.provider.MenuItemProvider;
import sdk.chat.ui.utils.ToastHelper;
import sdk.chat.ui.views.ChatView;
import sdk.chat.ui.views.ReplyView;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class ChatFragment extends AbstractChatFragment implements ChatView.Delegate, TextInputDelegate, ChatOptionsDelegate, KeyboardOverlayHandler {

    protected View rootView;

    protected ThreadX thread;


    protected ChatOptionsHandler optionsHandler;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker text screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = true;

    protected static boolean enableTrace = false;

    protected ChatActionBar chatActionBar;
    protected ChatView chatView;
    protected View divider;
    protected ReplyView replyView;
    protected MessageInput input;
    protected CoordinatorLayout listContainer;
    protected MaterialSearchView searchView;
    protected KeyboardAwareFrameLayout root;
    protected LinearLayout messageInputLinearLayout;
    protected FragmentContainerView keyboardOverlay;

    protected ActivityResultLauncher<Intent> launcher;

    protected AudioBinder audioBinder = null;
    protected DisposableMap dm = new DisposableMap();

    protected ChatFragmentKeyboardOverlayHelper koh;

    public ChatFragment() {
        super();
    }

    public ChatFragment(ThreadX thread) {
        super();
        setThread(thread);
    }

    public void setThread(ThreadX thread) {
        this.thread = thread;
        Bundle bundle = new Bundle();
        bundle.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        setArguments(bundle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = super.onCreateView(inflater, container, savedInstanceState);

        restoreState(savedInstanceState);
        setHasOptionsMenu(true);

        initViews();


        koh = new ChatFragmentKeyboardOverlayHelper(this);
        koh.showOptionsKeyboardOverlay();

        input.getInputEditText().setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && getActivity() != null) {
                    Serializable media = data.getSerializableExtra(KeyUtils.SELECTED_MEDIA);
                    if (media != null) {
                        // Pass these extras to the chat preview activity
                        Intent intent = new Intent(getActivity(), ChatPreviewActivity.class);
                        intent.putExtra(KeyUtils.SELECTED_MEDIA, media);
                        intent.putExtra(Keys.IntentKeyThreadEntityID, thread.getEntityID());
                        getActivity().startActivity(intent);
                    }
                }
            }
        });

        return rootView;
    }

    public View inflate(@NonNull LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(getLayout(), container, false);
    }

//    public void setChatViewBottomMargin(int margin) {
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) chatView.getLayoutParams();
//        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, margin);
//        chatView.setLayoutParams(params);
//    }

    protected @LayoutRes
    int getLayout() {
        return R.layout.fragment_chat;
    }

    public void updateOptionsButton() {
        input.findViewById(R.id.attachmentButton).setVisibility(chatView.getSelectedMessages().isEmpty() ? View.VISIBLE : View.GONE);
        input.findViewById(R.id.attachmentButtonSpace).setVisibility(chatView.getSelectedMessages().isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void hideTextInput() {
        input.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        updateChatViewMargins(true);
    }

    public void showTextInput() {
        input.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        updateChatViewMargins(true);
    }

    public void hideReplyView() {
        if (audioBinder != null) {
            audioBinder.hideReplyView();
        }
        chatView.clearSelection();
        replyView.hide();
        updateOptionsButton();
        updateChatViewMargins(true);
    }

    public void updateChatViewMargins(boolean post) {
        Runnable runnable = () -> {
            int bottomMargin = bottomMargin();

            if (koh.keyboardOverlayVisible()) {
                bottomMargin += getKeyboardAwareView().getKeyboardHeight();
            }

            // TODO: Margins
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) chatView.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin);
            chatView.setLayoutParams(params);
        };

        if (post) {
            input.post(runnable);
        } else {
            runnable.run();
        }

    }

    public int bottomMargin() {
        int bottomMargin = 0;
        if (replyView.isVisible()) {
            bottomMargin += replyView.getHeight();
        }
        if (input.getVisibility() != View.GONE) {
            bottomMargin += input.getHeight() + divider.getHeight();
        }
        return bottomMargin;
    }

    public void showReplyView(String title, String imageURL, String text) {
        koh.hideKeyboardOverlayAndShowKeyboard();
        updateOptionsButton();
        if (audioBinder != null) {
            audioBinder.showReplyView();
        }
        replyView.show(title, imageURL, text);

        updateChatViewMargins(true);
    }

    protected void initViews() {

        chatActionBar = rootView.findViewById(R.id.chatActionBar);
        chatView = rootView.findViewById(R.id.chatView);
        divider = rootView.findViewById(R.id.divider);
        replyView = rootView.findViewById(R.id.replyView);
        input = rootView.findViewById(R.id.input);
        listContainer = rootView.findViewById(R.id.listContainer);
        searchView = rootView.findViewById(R.id.searchView);
        root = rootView.findViewById(R.id.root);
        messageInputLinearLayout = rootView.findViewById(R.id.messageInputLinearLayout);
        keyboardOverlay = rootView.findViewById(R.id.keyboardOverlay);

        chatView.setDelegate(this);

        chatActionBar.onSearchClicked(v -> {
            searchView.showSearch();
        });

        addOnSearchListener();

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

        if (ChatSDK.audioMessage() != null && getActivity() != null) {
            addAudioBinder();
        } else {
            input.setInputListener(input -> {
                sendMessage(String.valueOf(input));
                return true;
            });
        }

        addTypingListener();

        input.setAttachmentsListener(this::toggleOptions);

        replyView.setOnCancelListener(v -> hideReplyView());

        // Action bar
        chatActionBar.setOnClickListener(v -> {
            chatActionBar.setEnabled(false);
            openThreadDetailsActivity();
        });

        chatActionBar.reload(thread);

        setChatState(TypingIndicatorHandler.State.active);

        if (enableTrace) {
            Debug.startMethodTracing("chat");
        }

        addListeners();


    }

    protected void addListeners() {
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
                        typingText += getString(sdk.chat.core.R.string.typing);
                    }
                    Logger.debug(typingText);
                    chatActionBar.setTypingText(thread, typingText);
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

    }

    protected void addAudioBinder() {
        audioBinder = new AudioBinder(getActivity(), this, input);
    }

    @Override
    public void clearData() {

    }

    public boolean hasVoice(User user) {
        return ChatSDK.thread().hasVoice(thread, user) && !thread.isReadOnly();
    }

    @Override
    public void onStart() {
        super.onStart();

        invalidateOptionsMenu();
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).setSupportActionBar(chatActionBar.getToolbar());
        }

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
            handleMessageSend(ChatSDK.thread().replyToMessage(thread, holder.getMessage(), text.trim()));
            hideReplyView();
        } else {
            handleMessageSend(ChatSDK.thread().sendMessageWithText(text.trim(), thread));
        }

    }

    protected void handleMessageSend(Completable completable) {
        completable.observeOn(RX.main()).doOnError(throwable -> {
            Logger.warn("");
            showToast(throwable.getLocalizedMessage());
        }).subscribe(this);
    }

    public void reloadData() {
//        chatView.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
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
    public void onPause() {
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
    public void onStop() {
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
    public void onDestroy() {
        if (enableTrace) {
            Debug.stopMethodTracing();
        }
        if (chatView != null) {
            chatView.removeListeners();
        }

        // TODO: Test this - in some situations where we are not using the
        // main activity this can be important
        ChatSDK.ui().setLocalNotificationHandler(thread -> true);

        super.onDestroy();
    }

    @Override
    public void onNewIntent(ThreadX thread) {
        this.thread = thread;
        clear();
        chatView.onLoadMore(0, 0);
        chatActionBar.reload(thread);
    }

    public void clear() {
        chatView.clear();
    }

    @Override
    public void onCreateOptionsMenu(@androidx.annotation.NonNull Menu menu, @androidx.annotation.NonNull MenuInflater inflater) {

        if (thread != null) {
            if (!chatView.getSelectedMessages().isEmpty()) {

                chatActionBar.hideSearchIcon();

                if (getContext() != null) {
                    ChatSDKUI.provider().menuItems().addSaveItem(getContext(), menu, 0);
                    ChatSDKUI.provider().menuItems().addCopyItem(getContext(), menu, 1);
                    ChatSDKUI.provider().menuItems().addDeleteItem(getContext(), menu, 2);
                    ChatSDKUI.provider().menuItems().addForwardItem(getContext(), menu, 3);
                    ChatSDKUI.provider().menuItems().addReplyItem(getContext(), menu, 4);
                }

                if (!UIModule.config().messageForwardingEnabled) {
                    menu.removeItem(MenuItemProvider.forwardItemId);
                }

                if (!UIModule.config().messageReplyEnabled) {
                    menu.removeItem(MenuItemProvider.replyItemId);
                }

                if (!UIModule.config().messageDownloadEnabled) {
                    menu.removeItem(MenuItemProvider.saveItemId);
                }

                if (chatView.getSelectedMessages().size() != 1) {
                    menu.removeItem(MenuItemProvider.replyItemId);
                    menu.removeItem(MenuItemProvider.saveItemId);
                } else {
                    if (!chatView.getSelectedMessages().get(0).canSave()) {
                        menu.removeItem(MenuItemProvider.saveItemId);
                    }
                }

                if (!hasVoice(ChatSDK.currentUser())) {
                    menu.removeItem(MenuItemProvider.replyItemId);
                    menu.removeItem(MenuItemProvider.deleteItemId);
                    menu.removeItem(MenuItemProvider.forwardItemId);
                }

                // Check that the messages could be deleted
                boolean canBeDeleted = true;
                for (MessageHolder holder: chatView.getSelectedMessages()) {
                    if (!ChatSDK.thread().canDeleteMessage(holder.getMessage())) {
                        canBeDeleted = false;
                    }
                }
                if (!canBeDeleted) {
                    menu.removeItem(MenuItemProvider.deleteItemId);
                }

                chatActionBar.hideText();
            } else {

                chatActionBar.showSearchIcon();

                if (ChatSDK.thread().canAddUsersToThread(thread) && getActivity() != null) {
                    ChatSDKUI.provider().menuItems().addAddItem(getContext(), menu, 1);
                }
                if (ChatSDK.call() != null && ChatSDK.call().callEnabled(this, thread.getEntityID())) {
                    ChatSDKUI.provider().menuItems().addCallItem(getContext(), menu, 2);
                }

                chatActionBar.showText();
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();
        if (id == MenuItemProvider.saveItemId) {
            List<MessageHolder> holders = chatView.getSelectedMessages();

            Activity activity = getActivity();
            if (activity != null) {
                dm.add(PermissionRequestHandler.requestWriteExternalStorage(activity).subscribe(() -> {
                    for (MessageHolder holder: holders) {

                        dm.add(holder.save(activity).subscribe(s -> {
                            ToastHelper.show(activity, s);
                        }, throwable -> {
                            ToastHelper.show(activity, throwable.getLocalizedMessage());
                        }));

                    }
                }, throwable -> ToastHelper.show(activity, throwable.getLocalizedMessage())));
            }

            clearSelection();
        }
        if (id == MenuItemProvider.deleteItemId) {
            List<MessageHolder> holders = chatView.getSelectedMessages();
            ChatSDK.thread().deleteMessages(MessageHolder.toMessages(holders)).subscribe(this);
            clearSelection();
        }
        if (id == MenuItemProvider.copyItemId && getActivity() != null) {
            chatView.copySelectedMessagesText(getActivity(), holder -> {
                if (chatView.getSelectedMessages().size() > 1 && UIModule.config().includeDateAndNameWhenCopyingMessages) {
                    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", CurrentLocale.get());
                    return dateFormatter.format(holder.getCreatedAt()) + ", " + holder.getUser().getName() + ": " + holder.getText();
                } else {
                    return holder.getText();
                }
            }, false);
            showToast(sdk.chat.core.R.string.copied_to_clipboard);
        }
        if (id == MenuItemProvider.forwardItemId) {

            List<MessageHolder> holders = chatView.getSelectedMessages();

            dm.put(Keys.messageForwardActivityCode, ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {
                if (activityResult.requestCode == Keys.messageForwardActivityCode) {
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        showToast(sdk.chat.core.R.string.success);
                    } else {
                        if (activityResult.data != null) {
                            String errorMessage = activityResult.data.getStringExtra(Keys.IntentKeyErrorMessage);
                            showToast(errorMessage);
                        }
                    }
                    dm.dispose(Keys.messageForwardActivityCode);
                }
            }));

            // We don't want to remove the user if we load another activity
            // Like the sticker activity
            removeUserFromChatOnExit = false;

            if (getActivity() != null) {
                ChatSDK.ui().startForwardMessageActivityForResult(getActivity(), thread, MessageHolder.toMessages(holders), Keys.messageForwardActivityCode);
            }
            clearSelection();
        }

        if (id == MenuItemProvider.replyItemId) {
            MessageHolder holder = chatView.getSelectedMessages().get(0);
            String imageURL = null;
            if (holder instanceof ImageMessageHolder) {
                imageURL = ((ImageMessageHolder) holder).getImageUrl();
            }
            showReplyView(holder.getUser().getName(), imageURL, holder.getPayload().lastMessageText());
            input.requestFocus();
            showKeyboard();
        }

        if (id == MenuItemProvider.addItemId && getActivity() != null) {

            // We don't want to remove the user if we load another activity
            // Like the sticker activity
            removeUserFromChatOnExit = false;

            ChatSDK.ui().startAddUsersToThreadActivity(getActivity(), thread.getEntityID());
        }
        if (id == MenuItemProvider.callItemId) {
            ChatSDK.call().startCall(this, thread.otherUser().getEntityID());
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

        if (getActivity() != null) {
            ChatSDK.ui().startThreadDetailsActivity(getActivity(), thread.getEntityID());
        }
    }

    @Override
    public void sendAudio(final File file, String mimeType, long duration) {
        if (ChatSDK.audioMessage() != null && getActivity() != null) {
            handleMessageSend(ChatSDK.audioMessage().sendMessage(getActivity(), file, mimeType, duration, thread));
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public static InputMethodManager getInputMethodManager(Context context) {
        return (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void showKeyboard() {
        EditText et = input.getInputEditText();
        et.post(() -> {
            et.requestFocus();
            imm().showSoftInput(et, 0);
        });
    }

    public void hideKeyboard() {
        EditText et = input.getInputEditText();
        imm().hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    public InputMethodManager imm() {
        EditText et = input.getInputEditText();
        return getInputMethodManager(et.getContext());
    }

    public void toggleOptions() {
        // If the keyboard overlay is available
        if (root.keyboardOverlayAvailable() && getActivity() != null) {
            koh.toggle();
        } else {
            // We don't want to remove the user if we load another activity
            // Like the sticker activity
            removeUserFromChatOnExit = false;

            if (getActivity() != null) {
                optionsHandler = ChatSDK.ui().getChatOptionsHandler(this);
                optionsHandler.show(getActivity());
            }

        }
    }

    @Override
    public void executeChatOption(ChatOption option) {
        if (getActivity() != null) {
            handleMessageSend(option.execute(getActivity(), launcher, thread));
        }
    }

    @Override
    public ThreadX getThread() {
        return thread;
    }

    @Override
    public void onClick(Message message) {
        if (getActivity() != null) {
            ChatSDKUI.shared().getMessageRegistrationManager().onClick(getActivity(), root, message);
        }
    }

    @Override
    public void onLongClick(Message message) {
        if (getActivity() != null) {
            ChatSDKUI.shared().getMessageRegistrationManager().onLongClick(getActivity(), root, message);
        }
    }

    public boolean onBackPressed() {
        if (!chatView.getSelectedMessages().isEmpty()) {
            chatView.clearSelection();
            return true;
        }
        // If the keyboard overlay is showing, we go back to the keyboard
        return koh.back();
    }

    @Override
    public void send(Sendable sendable) {
        if (getActivity() != null) {
            handleMessageSend(sendable.send(getActivity(), launcher, getThread()));
        }
    }

    @Override
    public void showOverlay(AbstractKeyboardOverlayFragment fragment) {
        koh.showOverlay(fragment);
    }

    @Override
    public boolean keyboardOverlayAvailable() {
        return getKeyboardAwareView().keyboardOverlayAvailable() && UIModule.config().keyboardOverlayEnabled;
    }

    protected void addOnSearchListener() {

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
    }

    protected void addTypingListener() {
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
    }

    public KeyboardAwareFrameLayout getKeyboardAwareView() {
        return root;
    }

    public FragmentContainerView getKeyboardOverlay() {
        return keyboardOverlay;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(Keys.IntentKeyThreadEntityID, thread.getEntityID());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreState(savedInstanceState);
    }

    public void restoreState(@Nullable Bundle savedInstanceState) {
        String threadEntityID = null;
        if (savedInstanceState != null) {
            threadEntityID = savedInstanceState.getString(Keys.IntentKeyThreadEntityID);
        }
        if (threadEntityID == null) {
            Bundle args = getArguments();
            if (args != null) {
                threadEntityID = args.getString(Keys.IntentKeyThreadEntityID);
            }
        }
        if (threadEntityID != null && thread == null) {
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        }
    }

    public boolean isOverlayVisible() {
        return koh.keyboardOverlayVisible();
    }

    public boolean isOverlayOrKeyboardVisible() {
        return isOverlayVisible() || getKeyboardAwareView().isKeyboardOpen();
    }

    public boolean isOverlayVisible(String key) {
        if (key == null) {
            return false;
        }
        return isOverlayVisible() && key.equals(currentOverlayKey());
    }

    public String currentOverlayKey() {
        return koh.currentOverlay() != null ? koh.currentOverlay().key() : null;
    }

    protected void invalidateOptionsMenu() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

}
