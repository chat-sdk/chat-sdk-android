/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.ViewHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ThreadsListAdapter extends RecyclerView.Adapter<ThreadViewHolder> {

    public static int ThreadCellType = 0;

    protected WeakReference<Context> context;

    protected List<Thread> threads = new ArrayList<>();

    protected HashMap<Thread, String> typing = new HashMap<>();
    protected PublishSubject<Thread> onClickSubject = PublishSubject.create();
    protected PublishSubject<Thread> onLongClickSubject = PublishSubject.create();

    public ThreadsListAdapter(Context context) {
        this.context = new WeakReference(context);
    }

    @Override
    public ThreadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.chat_sdk_row_thread, null);
        return new ThreadViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final ThreadViewHolder holder, int position) {

        final Thread thread = threads.get(position);

        ViewHelper.setText(holder.nameTextView, Strings.nameForThread(thread));

        ViewHelper.setOnClickListener(holder.itemView, view -> onClickSubject.onNext(thread));

        ViewHelper.setOnLongClickListener(holder.itemView, view -> {
            onLongClickSubject.onNext(thread);
            return true;
        });

        Date lastMessageAddedDate = thread.getLastMessageAddedDate();
        if (lastMessageAddedDate != null) {
            ViewHelper.setText(holder.dateTextView, getLastMessageDateAsString(lastMessageAddedDate));

            Message message = thread.getLastMessage();
            if (message == null) {
                List<Message> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC, 1);
                if (messages.size() > 0) {
                    message = messages.get(0);
                    thread.setLastMessage(message);
                    thread.update();
                }
            }


            ViewHelper.setText(holder.lastMessageTextView, getLastMessageText(message));
        }

        if (typing.get(thread) != null) {
            ViewHelper.setText(holder.lastMessageTextView, String.format(context.get().getString(R.string.__typing), typing.get(thread)));
        }

        int unreadMessageCount = thread.getUnreadMessagesCount();

        if (unreadMessageCount != 0 && (thread.typeIs(ThreadType.Private) || ChatSDK.config().unreadMessagesCountForPublicChatRoomsEnabled)) {

            ViewHelper.setText(holder.unreadMessageCountTextView, String.valueOf(unreadMessageCount));
            ViewHelper.setVisible(holder.unreadMessageCountTextView, true);

            holder.showUnreadIndicator();
        }
        else {
            holder.hideUnreadIndicator();
            ViewHelper.setVisible(holder.unreadMessageCountTextView, false);
        }

        ThreadImageBuilder.load(holder.imageView, thread);
    }

    public String getLastMessageDateAsString (Date date) {
        if (date != null) {
            return Strings.dateTime(date);
        }
        return null;
    }

    public String getLastMessageText (Message lastMessage) {
        String messageText = Strings.t(R.string.no_messages);
        if (lastMessage != null) {

            messageText = Strings.payloadAsString(lastMessage);
        }
        return messageText;
    }

    @Override
    public int getItemViewType(int position) {
        return ThreadCellType;
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    public boolean addRow (Thread thread, boolean notify) {
        for (Thread t : threads) {
            if (t.getEntityID().equals(thread.getEntityID())) {
                return false;
            }
        }

        threads.add(thread);
        if (notify) {
            notifyDataSetChanged();
        }
        return true;
    }

    public void addRow(Thread thread) {
        addRow(thread, true);
    }

    public void setTyping (Thread thread, String message) {
        if (message != null) {
            typing.put(thread, message);
        }
        else {
            typing.remove(thread);
        }
    }

    protected void sort() {
        Collections.sort(threads, new ThreadSorter());
    }

    public void clearData () {
        clearData(true);
    }

    public void clearData (boolean notify) {
        threads.clear();
        if (notify) {
            notifyDataSetChanged();
        }
    }

    public Observable<Thread> onClickObservable () {
        return onClickSubject;
    }

    public Observable<Thread> onLongClickObservable () {
        return onLongClickSubject;
    }


    public void updateThreads (List<Thread> threads) {
        boolean added = false;
        for (Thread t : threads) {
            added = addRow(t, false) || added;
        }
        // Maybe the last message has changed. I think this can lead to a race condition
        // Which causes the thread not to update when a new message comes in
//        if (added) {
            sort();
            notifyDataSetChanged();
//        }
    }

    public void setThreads(List<Thread> threads) {
        clearData(false);
        updateThreads(threads);
    }


}
