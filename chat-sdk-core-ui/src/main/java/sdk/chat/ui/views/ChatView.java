package sdk.chat.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageWrapper;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.pmw.tinylog.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.Progress;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.core.utils.TimeLog;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.performance.MessageHoldersDiffCallback;
import sdk.chat.ui.provider.ChatDateProvider;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class ChatView extends LinearLayout implements MessagesListAdapter.OnLoadMoreListener {

    protected MessagesList messagesList;
    protected LinearLayout root;
    protected boolean listenersAdded = false;

    public interface Delegate {
        Thread getThread();
        void onClick(Message message);
        void onLongClick(Message message);
    }

    protected MessagesListAdapter<MessageHolder> messagesListAdapter;

    protected List<MessageHolder> messageHolders = new ArrayList<>();

    protected DisposableMap dm = new DisposableMap();


    protected Delegate delegate;
    protected boolean loadMoreEnabled = true;

    public ChatView(Context context) {
        super(context);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public @LayoutRes int getLayout() {
        return R.layout.view_chat;
    }

    public void initViews() {
        LayoutInflater.from(getContext()).inflate(getLayout(), this);
//        

        messagesList = findViewById(R.id.messagesList);
        root = findViewById(R.id.root);

        final MessageHolders holders = new MessageHolders();
        ChatSDKUI.shared().getMessageRegistrationManager().onBindMessageHolders(getContext(), holders);


        messagesListAdapter = new MessagesListAdapter<>(ChatSDK.currentUserID(), holders, null);

        messagesListAdapter.setLoadMoreListener(this);

        messagesListAdapter.setDateHeadersFormatter(date -> {
            ChatDateProvider provider = ChatSDK.feather().instance(ChatDateProvider.class);
            if (provider != null) {
                return provider.from(date);
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", CurrentLocale.get());
                return formatter.format(date);
            }
        });

        messagesListAdapter.setOnMessageClickListener(holder -> {
            Message message = holder.getMessage();
            if (message.isReply()) {
                String originalMessageEntityID = message.stringForKey(Keys.Id);
                if (originalMessageEntityID != null) {
                    Message originalMessage = ChatSDK.db().fetchEntityWithEntityID(originalMessageEntityID, Message.class);
                    MessageHolder originalHolder = ChatSDKUI.provider().holderProvider().getMessageHolder(originalMessage);
                    if (originalHolder != null) {
                        int index = messageHolders.indexOf(originalHolder);
                        if (index >= 0) {
                            messagesList.smoothScrollToPosition(index);
                            return;
                        }
                    }
                }
            }
            delegate.onClick(holder.getMessage());
        });

        messagesListAdapter.setOnMessageLongClickListener(holder -> {
            delegate.onLongClick(holder.getMessage());
        });
        messagesList.setAdapter(messagesListAdapter);

        messagesListAdapter.setUserClickListener(userID -> {
            ChatSDK.ui().startProfileActivity(getContext(), userID);
        });

        onLoadMore(0, 0);

    }

    public void addListeners() {
        if (listenersAdded) {
            return;
        }
        listenersAdded = true;

        dm.add(ChatSDK.events().sourceOnSingle()
                .filter(NetworkEvent.filterType(
                        EventType.ThreadMessagesUpdated
//                        EventType.MessageSendStatusUpdated,
//                        EventType.MessageReadReceiptUpdated
                ))
                .filter(NetworkEvent.filterThreadEntityID(delegate.getThread().getEntityID()))
                .subscribe(networkEvent -> {

                    Message message = networkEvent.getMessage();
                    if (message != null) {
                        MessageHolder holder = ChatSDKUI.provider().holderProvider().getMessageHolder(message);
                        if (holder != null && !messageHolders.contains(holder)) {
                            Logger.debug("Missing");
                        }
                    }

                    Logger.debug("ChatView: " + networkEvent.debugText());

                    messagesList.post(() -> {
                        synchronize(null, true);
                    });
                }));


//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(
//                        EventType.UserPresenceUpdated,
//                        EventType.UserMetaUpdated))
//                .filter(NetworkEvent.filterThreadContainsUser(delegate.getThread()))
//                .subscribe(networkEvent -> {
//                    messagesList.post(() -> {
//                        synchronize(null, true);
//                    });
//                }));

        dm.add(ChatSDK.events().sourceOnSingle()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .filter(NetworkEvent.filterThreadEntityID(delegate.getThread().getEntityID()))
                .subscribe(networkEvent -> {

                    if (!ChatSDK.appBackgroundMonitor().inBackground()) {
                        networkEvent.getMessage().markReadIfNecessary();
                    }

                    dm.add(delegate.getThread().markReadAsync().subscribe());

                    messagesList.post(() -> {
                        addMessageToStart(networkEvent.getMessage());
                    });
                }));

        dm.add(ChatSDK.events().sourceOnSingle()
                .filter(NetworkEvent.filterType(EventType.MessageProgressUpdated))
                .filter(NetworkEvent.filterThreadEntityID(delegate.getThread().getEntityID()))
                .subscribe(networkEvent -> {
                    Progress progress = networkEvent.getProgress();
                    if (progress != null && progress.error != null) {
                        messagesList.post(() -> {
                            ToastHelper.show(getContext(), progress.error.getLocalizedMessage());
                        });
                    }
                }));

        dm.add(ChatSDK.events().sourceOnSingle()
                .filter(NetworkEvent.filterType(EventType.MessageRemoved))
                .filter(NetworkEvent.filterThreadEntityID(delegate.getThread().getEntityID()))
                .subscribe(networkEvent -> {
                    messagesList.post(() -> {
                        removeMessage(networkEvent.getMessage());
                    });
                }));
    }

    protected int maxImageWidth() {
        // Prevent overly big messages in landscape mode
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return Math.min(Math.round(getResources().getDisplayMetrics().widthPixels), UIModule.config().maxImageSize);
        } else {
            return Math.min(Math.round(getResources().getDisplayMetrics().heightPixels), UIModule.config().maxImageSize);
        }
    }

    public void clearSelection() {
        messagesListAdapter.unselectAllItems();
    }

    public List<MessageHolder> getSelectedMessages() {
        return messagesListAdapter.getSelectedMessages();
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        // There is an issue with Firebase whereby the message date is
        // initially just estimated. When the message is added, the scrollview
        // scrolls and that triggers the on-load-more which then does a database query
        // while that is running the message date has been updated to a later date
        // so a duplicate message comes back...
        if (!loadMoreEnabled) {
            return;
        }

        Date loadMessagesFrom = delegate.getThread().getLoadMessagesFrom();

        // If there are already items in the list, load messages before oldest
        if (messageHolders.size() > 0) {
            loadMessagesFrom = messageHolders.get(messageHolders.size() - 1).getCreatedAt();
        }

        if (loadMessagesFrom != null) {
            Logger.debug("Load messages from: " + loadMessagesFrom.getTime());
        }

        dm.add(ChatSDK.thread()
                .loadMoreMessagesBefore(delegate.getThread(), loadMessagesFrom, true)
                .flatMap((Function<List<Message>, SingleSource<List<MessageHolder>>>) messages -> {
                    return getMessageHoldersAsync(messages, false);
                })
                .observeOn(RX.main())
                .subscribe(messages -> {
                    synchronize(() -> {
                        addMessageHoldersToEnd(messages, false);
                    });
                }));
}

    /**
     * Start means new messages to bottom of screen
     */
    protected void addMessageToStart(Message message) {

        Logger.debug("Add Message to start " + message.getText());

        boolean scroll = message.getSender().isMe();

        int offset = messagesList.computeVerticalScrollOffset();
        int extent = messagesList.computeVerticalScrollExtent();
        int range = messagesList.computeVerticalScrollRange();
        int distanceFromBottom = range - extent - offset;

        if (distanceFromBottom < 400) {
            scroll = true;
        }

        MessageHolder holder = ChatSDKUI.provider().holderProvider().getMessageHolder(message);
        if (!messageHolders.contains(holder)) {

            messageHolders.add(0, holder);

            updatePreviousMessage(holder);
            holder.updateReadStatus();

            loadMoreEnabled = false;
            messagesListAdapter.addToStart(holder, scroll, true);
            messagesList.post(() -> loadMoreEnabled = true);
        } else {
            Logger.debug("Exists already");
        }

    }

    protected void updatePreviousMessage(MessageHolder holder) {
        MessageHolder previous = ChatSDKUI.provider().holderProvider().getMessageHolder(holder.previousMessage());
        if (previous != null) {
            previous.updateNextAndPreviousMessages();
            if (previous.isDirty()) {
                previous.makeClean();
                messagesListAdapter.update(previous);
            }
        }
    }

    public void update(MessageHolder holder) {
        messagesListAdapter.update(holder);
    }

    protected void updateNextMessage(MessageHolder holder) {
        MessageHolder next = ChatSDKUI.provider().holderProvider().getMessageHolder(holder.nextMessage());
        if (next != null) {
            next.updateNextAndPreviousMessages();
            if (next.isDirty()) {
                next.makeClean();
                messagesListAdapter.update(next);
            }
        }
    }

    protected void removeMessage(Message message) {
        MessageHolder holder = ChatSDKUI.provider().holderProvider().getMessageHolder(message);
        messageHolders.remove(holder);
        messagesListAdapter.delete(holder, true);

        ChatSDKUI.provider().holderProvider().removeMessageHolder(message);

        updateNextMessage(holder);
        updatePreviousMessage(holder);
    }

    /**
     * End means historic messages to top of screen
     */
    protected void addMessageHoldersToEnd(List<MessageHolder> holders, boolean notify) {

        // Add to current holders at zero index
        // Newest first
        List<MessageHolder> toAdd = new ArrayList<>();
        for (MessageHolder holder: holders) {
            if (!messageHolders.contains(holder)) {
                messageHolders.add(holder);
                toAdd.add(holder);
            } else {
                Logger.error("We have a duplicate");
            }
        }

        // Reverse order because we are adding to end
        messagesListAdapter.addToEnd(toAdd, false, notify);
    }

    protected void synchronize(Runnable modifyList) {
        synchronize(modifyList, false);
    }

    protected void synchronize(Runnable modifyList, boolean sort) {

        long start = System.currentTimeMillis();

        if (messagesListAdapter != null) {
            final List<MessageWrapper<?>> oldHolders = new ArrayList<>(messagesListAdapter.getItems());

            if (modifyList != null) {
                modifyList.run();
            }

            final List<MessageWrapper<?>> newHolders = new ArrayList<>(messagesListAdapter.getItems());

//            RX.single().scheduleDirect(() -> {
                if (sort) {
                    sortMessageHolders();
                }

                MessageHoldersDiffCallback callback = new MessageHoldersDiffCallback(newHolders, oldHolders);
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

//                messagesList.post(() -> {
                    Parcelable recyclerViewState = messagesList.getLayoutManager().onSaveInstanceState();

                    messagesListAdapter.getItems().clear();
                    messagesListAdapter.getItems().addAll(newHolders);

                    result.dispatchUpdatesTo(messagesListAdapter);

                    messagesList.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//                });
//            });

        }

        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println("Diff: " + diff);

    }

    public void sortMessageHolders() {
        Collections.sort(messageHolders, (o1, o2) -> {
            return o1.getCreatedAt().compareTo(o2.getCreatedAt());
        });
    }

    public List<MessageHolder> getMessageHolders(final List<Message> messages, boolean reverse) {

        // Get the holders - they will be in asc order i.e. oldest at 0
        TimeLog log = new TimeLog("Get Holders - " + messages.size());

        final List<MessageHolder> holders = new ArrayList<>();
        for (Message message : messages) {
            MessageHolder holder = ChatSDKUI.provider().holderProvider().getMessageHolder(message);
            holders.add(holder);
        }
        if (reverse) {
            Collections.reverse(holders);
        }

        log.end();

        return holders;
    }

    public Single<List<MessageHolder>> getMessageHoldersAsync(final List<Message> messages, boolean reverse) {
        return Single.create((SingleOnSubscribe<List<MessageHolder>>) emitter -> {
            emitter.onSuccess(getMessageHolders(messages, reverse));
        }).subscribeOn(RX.computation()).observeOn(RX.main());
    }

    public void notifyDataSetChanged() {
        messagesListAdapter.notifyDataSetChanged();
    }

    public void clear() {
        if (messagesListAdapter != null) {
            messageHolders.clear();
            messagesListAdapter.clear();
        }
    }

    public void copySelectedMessagesText(Context context, MessagesListAdapter.Formatter<MessageHolder> formatter, boolean reverse) {
        messagesListAdapter.copySelectedMessagesText(context, formatter, reverse);
    }

    public void enableSelectionMode(MessagesListAdapter.SelectionListener selectionListener) {
        messagesListAdapter.enableSelectionMode(selectionListener);
    }

    public void filter(final String filter) {
        if (filter == null || filter.isEmpty()) {
            clearFilter();
        } else {
            final ArrayList<MessageHolder> filtered = new ArrayList<>();
            for (MessageHolder holder : messageHolders) {
                if (holder.getText().toLowerCase().contains(filter.trim().toLowerCase())) {
                    filtered.add(holder);
                }
            }
            synchronize(() -> {
                messagesListAdapter.getItems().clear();
                messagesListAdapter.addToEnd(filtered, false, false);
            });
        }
    }

    public void clearFilter() {
        synchronize(() -> {
            messagesListAdapter.getItems().clear();
            messagesListAdapter.addToEnd(messageHolders, false, false);
        });
    }

    public void removeListeners() {
        dm.dispose();
        listenersAdded = false;
    }

    public MessagesList getMessagesList() {
        return messagesList;
    }
}
