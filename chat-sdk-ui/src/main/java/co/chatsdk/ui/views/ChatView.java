package co.chatsdk.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import co.chatsdk.core.utils.Dimen;
import co.chatsdk.ui.R2;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.custom.Customiser;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ChatView extends LinearLayout implements MessagesListAdapter.OnLoadMoreListener {

    @BindView(R2.id.messagesList) protected MessagesList messagesList;
    @BindView(R2.id.root) protected LinearLayout root;

    public interface Delegate {
        Thread getThread();
        void onClick(Message message);
        void onLongClick(Message message);
    }

    protected MessagesListAdapter<MessageHolder> messagesListAdapter;

    protected HashMap<Message, MessageHolder> messageHolderHashMap = new HashMap<>();
    protected ArrayList<MessageHolder> messageHolders = new ArrayList<>();

    protected DisposableMap dm = new DisposableMap();

    protected PrettyTime prettyTime = new PrettyTime();

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

    public void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_chat, this);
        ButterKnife.bind(this);

        MessageHolders holders = new MessageHolders();

        Customiser.shared().onBindMessageHolders(getContext(), holders);

        messagesListAdapter = new MessagesListAdapter<>(ChatSDK.currentUserID(), holders, (imageView, url, payload) -> {
            int placeholder = R.drawable.icn_100_profile;
            if (payload instanceof Integer) {
                placeholder = (Integer) payload;
            }

            if (url == null || url.isEmpty()) {
                imageView.setImageResource(placeholder);
            } else {
                if (payload == null) {
                    // User avatar
                    Glide.with(this)
                            .load(url)
                            .dontAnimate()
                            .override(Dimen.from(getContext(), R.dimen.small_avatar_width), Dimen.from(getContext(), R.dimen.small_avatar_height))
                            .into(imageView);
                } else {
                    // Image message
                    Glide.with(this)
                            .load(url)
                            .dontAnimate()
                            .placeholder(placeholder)
                            .error(R.drawable.icn_200_image_message_error)
                            .override(maxImageWidth(), maxImageWidth()).centerCrop().into(imageView);
                }
            }
        });

        messagesListAdapter.setLoadMoreListener(this);
        messagesListAdapter.setDateHeadersFormatter(date -> prettyTime.format(date));

        messagesListAdapter.setOnMessageClickListener(holder -> {
            delegate.onClick(holder.getMessage());
        });

        messagesListAdapter.setOnMessageLongClickListener(holder -> {
            delegate.onLongClick(holder.getMessage());
        });
        messagesList.setAdapter(messagesListAdapter);

        addListeners();
        onLoadMore(0, 0);

    }

    protected void addListeners() {

        // Add the event listeners
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded, EventType.MessageUpdated, EventType.MessageRemoved, EventType.MessageReadReceiptUpdated, EventType.MessageSendStatusUpdated))
                .filter(NetworkEvent.filterThreadEntityID(delegate.getThread().getEntityID()))
                .subscribe(networkEvent -> {
                    Message message = networkEvent.message;
                    if (networkEvent.typeIs(EventType.MessageAdded)) {
                        addMessageToStartOrUpdate(message);
                        message.markReadIfNecessary();
                    }
                    if (networkEvent.typeIs(EventType.MessageUpdated)) {
                        addMessageToStartOrUpdate(message);
                    }
                    if (networkEvent.typeIs(EventType.MessageRemoved)) {
                        removeMessage(networkEvent.message);
                    }
                    if (networkEvent.typeIs(EventType.MessageReadReceiptUpdated) && ChatSDK.readReceipts() != null && message.getSender().isMe()) {
                        addMessageToStartOrUpdate(message);
                    }
                    if (networkEvent.typeIs(EventType.MessageSendStatusUpdated)) {
                        MessageSendProgress progress = networkEvent.getMessageSendProgress();
                        addMessageToStartOrUpdate(progress.message, progress);
                    }
                }));

    }

    protected int maxImageWidth() {
        // Prevent overly big messages in landscape mode
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return Math.round(getResources().getDisplayMetrics().widthPixels);
        } else {
            return Math.round(getResources().getDisplayMetrics().heightPixels);
        }
    }

    public void clearSelection() {
        messagesListAdapter.unselectAllItems();
    }

    public List<Message> getSelectedMessages() {
        return MessageHolder.toMessages(messagesListAdapter.getSelectedMessages());
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Date loadFromDate = null;
        if (totalItemsCount != 0) {
            // This list has the newest first
            loadFromDate = messageHolders.get(messageHolders.size() - 1).getCreatedAt();
        }
        dm.add(ChatSDK.thread()
                .loadMoreMessagesForThread(loadFromDate, delegate.getThread(), true)
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
            holder.update();
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
            holder = Customiser.shared().onNewMessageHolder(message);

            messageHolders.add(0, holder);
            messageHolderHashMap.put(holder.getMessage(), holder);

            // This means that we only scroll down if we were already at the bottom
            // it can be annoying if you have scrolled up and then a new message
            // comes in and scrolls the screen down
            boolean scroll = message.getSender().isMe();

            int offset = messagesList.computeVerticalScrollOffset();
            int extent = messagesList.computeVerticalScrollExtent();
            int range = messagesList.computeVerticalScrollRange();
            int distanceFromBottom = range - extent - offset;

            if (distanceFromBottom < 400) {
                scroll = true;
            }

            messagesListAdapter.addToStart(holder, scroll);

            // Update the previous holder so that we can hide the
            // name if necessary
//            updatePrevious(message);

        } else {
            holder.update();
            holder.setProgress(progress);
            messagesListAdapter.update(holder);
        }
    }

    public void addMessagesToEnd(List<Message> messages) {
        // Check to see if the holders already exist
        ArrayList<MessageHolder> holders = new ArrayList<>();
        for (Message message : messages) {
            MessageHolder holder = messageHolderHashMap.get(message);
            if (holder == null) {
                holder = Customiser.shared().onNewMessageHolder(message);
                messageHolderHashMap.put(message, holder);
                holders.add(holder);
            }
        }
        messageHolders.addAll(holders);
        messagesListAdapter.addToEnd(holders, false);
    }

    public void notifyDataSetChanged() {
        messagesListAdapter.notifyDataSetChanged();
    }

    public void clear() {
        if (messagesListAdapter != null) {
            messageHolderHashMap.clear();
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

    public void filter(String filter) {
        if (filter == null || filter.isEmpty()) {
            clearFilter();
        } else {
            filter = filter.trim();

            ArrayList<MessageHolder> filtered = new ArrayList<>();
            for (MessageHolder holder : messageHolders) {
                if (holder.getText().toLowerCase().contains(filter.toLowerCase())) {
                    filtered.add(holder);
                }
            }

            messagesListAdapter.clear();
            messagesListAdapter.addToEnd(filtered, true);
        }
    }

    public void clearFilter() {
        messagesListAdapter.clear();
        messagesListAdapter.addToEnd(messageHolders, true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dm.dispose();
    }

}
