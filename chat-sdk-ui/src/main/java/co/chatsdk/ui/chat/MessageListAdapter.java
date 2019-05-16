/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.chatsdk.core.base.AbstractMessageViewHolder;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.Progress;
import co.chatsdk.ui.chat.handlers.TextMessageDisplayHandler;
import co.chatsdk.core.message_action.MessageAction;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class MessageListAdapter extends RecyclerView.Adapter<AbstractMessageViewHolder> {

    public static int ViewTypeMine = 1;
    public static int ViewTypeReply = 2;

    protected WeakReference<Activity> activity;
    protected PublishSubject<List<MessageAction>> messageActionPublishSubject = PublishSubject.create();

    protected List<MessageListItem> messageItems = new ArrayList<>();

    public MessageListAdapter(Activity activity) {
        this.activity = new WeakReference<>(activity);

        // Optimization
        setHasStableIds(true);
    }

    @Override
    public AbstractMessageViewHolder onCreateViewHolder(ViewGroup parent, int type) {

        if (ChatSDK.config().debug) {
            System.out.println("onCreateViewHolder: " + type);
        }

        int viewType = (int) Math.ceil(type / MessageType.Max);
        int messageType = type - viewType * MessageType.Max;
        boolean isReply = viewType == ViewTypeReply;

        MessageDisplayHandler handler = ChatSDK.ui().getMessageHandler(new MessageType(messageType));
        if (handler != null) {
            return handler.newViewHolder(isReply, activity.get(), messageActionPublishSubject);
        } else {
            // TODO: Handler this better
            Timber.w("Message handler not available for message type");
            handler = new TextMessageDisplayHandler();
            return handler.newViewHolder(isReply, activity.get(), messageActionPublishSubject);
        }
    }

    @Override
    public void onBindViewHolder(AbstractMessageViewHolder holder, int position) {

        if (ChatSDK.config().debug) {
            System.out.println("onBindViewHolder: pos: " + position);
        }

        MessageListItem messageItem = getMessageItems().get(position);
        Message message = messageItem.getMessage();

        holder.setMessage(message);

        if (message.getMessageStatus().equals(MessageSendStatus.Uploading) || (messageItem.progress > 0 && messageItem.progress < 1)) {
            holder.showProgressBar(messageItem.progress);
        }
        else {
            holder.hideProgressBar();
        }

        for(MessageDisplayHandler handler : ChatSDK.ui().getMessageHandlers()) {
            handler.updateMessageCellView(messageItem.message, holder, activity.get());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageItems.get(position).getMessage();
        int viewType = message.getSender().isMe() ? ViewTypeMine : ViewTypeReply;
        int messageType = message.getType();

        // Multiply by message.max so the two types don't clash
        return viewType * MessageType.Max + messageType;
    }

    @Override
    public long getItemId(int i) {
        return messageItems.get(i).getMessage().getId();
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }



    public List<MessageListItem> getMessageItems() {
        return messageItems;
    }

    protected boolean addRow(MessageListItem item, boolean sort, boolean notify, boolean toEnd){
        if (item == null)
            return false;

        // Don't add message that does not have entity id and the status of the message is not sending.
        if (item.getMessage().getEntityID() == null) {
            return false;
        }


        if (toEnd) {
            messageItems.add(item);
        } else {
            messageItems.add(0, item);
        }

        if(sort) {
            sort();
        }

        if(notify) {
            if (toEnd) {
                notifyItemInserted(messageItems.size() - 1);
            } else {
                notifyItemInserted(0);
            }
        }

        return true;
    }

    public void sort () {
        Collections.sort(messageItems, new MessageItemSorter(DaoCore.ORDER_DESC));
    }

    public void sortAndNotify () {
        sort();
        notifyDataSetChanged();
    }

    public void notifyMessageChanged (Message message) {
        int index = indexOf(message);
        if (index > 0) {
            notifyItemChanged(index);
        }
    }

    public int indexOf(Message message) {
        MessageListItem item = messageItemForMessage(message);
        if (item != null) {
            return messageItems.indexOf(item);
        }
        return -1;
    }

    /**
     * Add a new message to the list.
     * @return true if the item is added to the list.
     * */
    public boolean addRow(Message message){
        return addRow(message, true, true);
    }

    public boolean addRow(Message message, boolean sort, boolean notify, Progress progress, boolean toEnd){
        MessageListItem item = messageItemForMessage(message);
        boolean returnStatus = false;
        if (item == null) {
            item = new MessageListItem(message);
            returnStatus = addRow(item, sort, notify, toEnd);
        }
        if (progress != null) {
            item.progress = progress.asFraction();
        }
        return returnStatus;
    }

    public boolean addRow(Message message, boolean sort, boolean notify, boolean toEnd) {
        return addRow(message, sort, notify, null, toEnd);
    }

    public boolean addRow(Message message, boolean sort, boolean notify) {
        return addRow(message, sort, notify, null, true);
    }

    public boolean removeRow (Message message, boolean notify) {
        MessageListItem item = messageItemForMessage(message);
        if (item != null) {
            int index = messageItems.indexOf(item);
            messageItems.remove(item);
            if (notify) {
                notifyItemRemoved(index);
            }
            return true;
        }
        return false;
    }

    protected boolean messageExists (Message message) {
        return messageItemForMessage(message) != null;
    }

    protected MessageListItem messageItemForMessage (Message message) {
        for(MessageListItem i : messageItems) {
            if(i.message.equalsEntity(message)) {
                return i;
            }
        }
        return null;
    }


    /**
     * Clear the messages list.
     * */
    public void clear() {
        clear(true);
    }

    public void clear(boolean notify) {
        messageItems.clear();
        if(notify) {
            notifyDataSetChanged();
        }
    }

    public void setMessages(List<Message> messages) {
        clear(false);
        for (Message message : messages) {
            addRow(message, false, false);
        }
        sortAndNotify();
    }

    // Untested because upload progress doesn't work
    public void setProgressForMessage (Message message, float progress) {
        MessageListItem item = messageListItemForMessage(message);
        if(item != null) {
            item.progress = progress;
        }
        notifyDataSetChanged();
    }

    public MessageListItem messageListItemForMessage (Message message) {
        for(MessageListItem i : messageItems) {
            if(i.message.equals(message)) {
                return i;
            }
        }
        return null;
    }

    public int size () {
        return messageItems.size();
    }

    public Observable<List<MessageAction>> getMessageActionObservable () {
        return messageActionPublishSubject;
    }

}
