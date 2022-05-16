package sdk.chat.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageWrapper;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.ocpsoft.prettytime.PrettyTime;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.core.utils.Dimen;
import sdk.chat.core.utils.TimeLog;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.performance.MessageHoldersDiffCallback;
import sdk.chat.ui.utils.ImageLoaderPayload;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class ChatView extends LinearLayout implements MessagesListAdapter.OnLoadMoreListener {

    @BindView(R2.id.messagesList) protected MessagesList messagesList;
    @BindView(R2.id.root) protected LinearLayout root;
    protected boolean listenersAdded = false;

    public interface Delegate {
        Thread getThread();
        void onClick(Message message);
        void onLongClick(Message message);
    }

    protected MessagesListAdapter<MessageHolder> messagesListAdapter;

//    protected Map<Message, MessageHolder> messageHolderHashMap = new HashMap<>();

    protected List<MessageHolder> messageHolders = new ArrayList<>();

    protected DisposableMap dm = new DisposableMap();

    protected final PrettyTime prettyTime = new PrettyTime(CurrentLocale.get());

    protected Delegate delegate;

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
        ButterKnife.bind(this);

        final MessageHolders holders = new MessageHolders();
        ChatSDKUI.shared().getMessageRegistrationManager().onBindMessageHolders(getContext(), holders);

        messagesListAdapter = new MessagesListAdapter<>(ChatSDK.currentUserID(), holders, (imageView, url, payload) -> {

            ImageLoaderPayload ilp;
            if (payload instanceof ImageLoaderPayload) {
                ilp = (ImageLoaderPayload) payload;
            } else {
                ilp = new ImageLoaderPayload();
            }

            if (ilp.ar > 0) {
                ilp.width = maxImageWidth();
                ilp.height = Math.round(ilp.width / ilp.ar);
            } else {
                ilp.width = Math.max(ilp.width, maxImageWidth());
                ilp.height = Math.max(ilp.height, maxImageWidth());
            }

            if (ilp.placeholder == 0) {
                ilp.placeholder = R.drawable.icn_200_image_message_placeholder;
            }
            if (ilp.error == 0) {
                ilp.error = R.drawable.icn_200_image_message_placeholder;
//                ilp.error = R.drawable.icn_200_image_message_loading;
            }

            if (url == null) {
                Logger.debug("Stop here");
                return;
            }

            RequestManager request = Glide.with(this);
            RequestBuilder<?> builder;
            if (ilp.isAnimated) {
                builder = request.asGif();
            } else {
                builder = request.asDrawable().dontAnimate();
            }

            if (payload == null) {
                // User avatar
                builder.load(url)
                        .placeholder(R.drawable.icn_100_profile)
                        .override(Dimen.from(getContext(), R.dimen.small_avatar_width), Dimen.from(getContext(), R.dimen.small_avatar_height))
                        .into(imageView);
            } else {

                // If this is a local image
                Uri uri = Uri.parse(url);
                if (uri != null && uri.getScheme() != null && uri.getScheme().equals("android.resource")) {
                    builder = builder.load(uri);
                } else {
                    builder = builder.load(url);
                }

                builder.override(ilp.width, ilp.height)
                        .placeholder(ilp.placeholder)
                        .error(ilp.error)
                        .centerCrop()
                        .into(imageView);
            }
        });

        messagesListAdapter.setLoadMoreListener(this);
        messagesListAdapter.setDateHeadersFormatter(date -> prettyTime.format(date));

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

        onLoadMore(0, 0);

    }

    public void addListeners() {
        if (listenersAdded) {
            return;
        }
        listenersAdded = true;

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(
                        EventType.ThreadMessagesUpdated
//                        EventType.MessageSendStatusUpdated,
//                        EventType.MessageReadReceiptUpdated
                ))
                .filter(NetworkEvent.filterThreadEntityID(delegate.getThread().getEntityID()))
                .subscribe(networkEvent -> {

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

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .filter(NetworkEvent.filterThreadEntityID(delegate.getThread().getEntityID()))
                .subscribe(networkEvent -> {
                    if (!ChatSDK.appBackgroundMonitor().inBackground()) {
                        networkEvent.getMessage().markReadIfNecessary();
                    }
                    messagesList.post(() -> {
                        addMessageToStart(networkEvent.getMessage());
                    });
                }));

        dm.add(ChatSDK.events().sourceOnMain()
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

        Date loadMessagesFrom = delegate.getThread().getLoadMessagesFrom();

        // If there are already items in the list, load messages before oldest
        if (messageHolders.size() > 0) {
            loadMessagesFrom = messageHolders.get(messageHolders.size() - 1).getCreatedAt();
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

        Logger.debug("Add Message to start" + message.getText());

        boolean scroll = message.getSender().isMe();

        int offset = messagesList.computeVerticalScrollOffset();
        int extent = messagesList.computeVerticalScrollExtent();
        int range = messagesList.computeVerticalScrollRange();
        int distanceFromBottom = range - extent - offset;

        if (distanceFromBottom < 400) {
            scroll = true;
        }

        MessageHolder holder = ChatSDKUI.provider().holderProvider().getMessageHolder(message);
        messageHolders.add(0, holder);

        updatePreviousMessage(holder);
        holder.updateReadStatus();
        messagesListAdapter.addToStart(holder, scroll, true);
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

        updateNextMessage(holder);
        updatePreviousMessage(holder);
    }

    /**
     * End means historic messages to top of screen
     */
    protected void addMessageHoldersToEnd(List<MessageHolder> holders, boolean notify) {

        // Add to current holders at zero index
        // Newest first
        messageHolders.addAll(holders);

        // Reverse order because we are adding to end
        messagesListAdapter.addToEnd(holders, false, notify);
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
}
