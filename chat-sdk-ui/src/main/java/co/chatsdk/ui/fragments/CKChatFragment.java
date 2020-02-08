package co.chatsdk.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.ui.R;
import co.chatsdk.ui.activities.CKChatActivity;
import co.chatsdk.ui.chat.ImageMessageOnClickHandler;
import co.chatsdk.ui.chat.LocationMessageOnClickHandler;
import co.chatsdk.ui.chat.TextInputDelegate;
import co.chatsdk.ui.chat.model.ImageMessageHolder;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.chat.view_holders.IncomingImageMessageViewHolder;
import co.chatsdk.ui.chat.view_holders.IncomingTextMessageViewHolder;
import co.chatsdk.ui.chat.view_holders.OutcomingImageMessageViewHolder;
import co.chatsdk.ui.chat.view_holders.OutcomingTextMessageViewHolder;
import co.chatsdk.ui.databinding.ChatkitActivityChatBinding;
import co.chatsdk.ui.databinding.FragmentChatBinding;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CKChatFragment extends BaseFragment implements TextInputDelegate, ChatOptionsDelegate,
        MessagesListAdapter.OnLoadMoreListener {

    public static final int messageForwardActivityCode = 998;

    protected ChatOptionsHandler optionsHandler;

    protected static boolean enableTrace = false;

    protected Thread thread;

    protected Bundle bundle;

    FragmentChatBinding b;

    protected MessagesListAdapter<MessageHolder> messagesListAdapter;

    protected HashMap<Message, MessageHolder> messageHolderHashMap = new HashMap<>();
    protected ArrayList<MessageHolder> messageHolders = new ArrayList<>();

    protected PrettyTime prettyTime = new PrettyTime();

    protected DisplayMetrics displayMetrics = new DisplayMetrics();

    protected WeakReference<Activity> activity;

    // Should we remove the user from the public chat when we stop this activity?
    // If we are showing a temporary screen like the sticker text screen
    // this should be set to no
    protected boolean removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        b = DataBindingUtil.inflate(inflater, getLayout(), container, false);
        rootView = b.getRoot();

        if(enableTrace) {
            android.os.Debug.startMethodTracing("chat");
        }

        if (thread != null) {
            initViews();
            addListeners();

            setChatState(TypingIndicatorHandler.State.active);

            activity.get().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            onLoadMore(0, 0);

            thread.markRead();
        }

        return rootView;
    }

    protected void addListeners() {
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
                .filter(NetworkEvent.filterType(EventType.MessageSendStatusChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                    MessageSendProgress progress = networkEvent.getMessageSendProgress();
                    addMessageToStartOrUpdate(progress.message, progress);
                }));

    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    protected int maxImageWidth() {
        // Prevent overly big messages in landscape mode
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            return Math.round(displayMetrics.widthPixels);
        } else {
            return Math.round(displayMetrics.heightPixels);
        }
    }

    protected @LayoutRes
    int getLayout() {
        return R.layout.fragment_chat;
    }

    protected void initViews () {

        IncomingTextMessageViewHolder.Payload holderPayload = new IncomingTextMessageViewHolder.Payload();
        holderPayload.avatarClickListener = user -> {
            ChatSDK.ui().startProfileActivity(getContext(), user.getEntityID());
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
                        ImageMessageOnClickHandler.onClick(activity.get(), rootView, content.getImageUrl());
                    }
                    if (message.getMessage().getMessageType().is(MessageType.Location)) {

                        double longitude = message.getMessage().doubleForKey(Keys.MessageLongitude);
                        double latitude = message.getMessage().doubleForKey(Keys.MessageLatitude);

                        LocationMessageOnClickHandler.onClick(activity.get(), new LatLng(latitude, longitude));
                    }
                }
            }
        });

        messagesListAdapter.enableSelectionMode(count -> {
            activity.get().invalidateOptionsMenu();
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

    }

    @Override
    public void clearData() {
        if (messagesListAdapter != null) {
            messageHolderHashMap.clear();
            messageHolders.clear();
            messagesListAdapter.clear();
        }
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }

    }

    public void reloadData () {
        messagesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        removeUserFromChatOnExit = !ChatSDK.config().publicChatAutoSubscriptionEnabled;

        if (thread != null && thread.typeIs(ThreadType.Public)) {
            User currentUser = ChatSDK.currentUser();
            ChatSDK.thread().addUsersToThread(thread, currentUser)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this);
        }

        // Show a local notification if the text is from a different thread
        ChatSDK.ui().setLocalNotificationHandler(thread -> !thread.getEntityID().equals(this.thread.getEntityID()));

    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard();
    }

    /**
     * Sending a broadcast that the chat was closed, Only if there were new messageHolders on this chat.
     * This is used for example to update the thread list that messageHolders has been read.
     */
    @Override
    public void onStop() {
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
    public void onDestroy() {
        if(enableTrace) {
            android.os.Debug.stopMethodTracing();
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (messagesListAdapter != null && !messagesListAdapter.getSelectedMessages().isEmpty()) {
            inflater.inflate(R.menu.chatkit_chat_actions, menu);

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
        }
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
            messagesListAdapter.copySelectedMessagesText(getContext(), holder -> {
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
            ChatSDK.ui().startForwardMessageActivityForResult(activity.get(), thread, messages, messageForwardActivityCode);
            messagesListAdapter.unselectAllItems();
        };

        if (id == R.id.action_reply) {
            Message message = messagesListAdapter.getSelectedMessages().get(0).getMessage();
            b.replyView.show(message.getSender().getName(), message.imageURL(), message.getText());
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void showOptions() {
        // We don't want to remove the user if we load another activity
        // Like the sticker activity
        removeUserFromChatOnExit = false;

        optionsHandler = ChatSDK.ui().getChatOptionsHandler(this);
        optionsHandler.show(activity.get());
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
        handleMessageSend(option.execute(activity.get(), thread));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.activity = new WeakReference<>((Activity) context);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    public MessagesListAdapter<MessageHolder> getMessagesListAdapter() {
        return messagesListAdapter;
    }

}
