/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import co.chatsdk.ui.R;
import co.chatsdk.core.utils.Strings;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ThreadsListAdapter extends RecyclerView.Adapter<ThreadViewHolder> {

    public static int ThreadCellType = 0;

    protected WeakReference<Context> context;

    protected List<Thread> threads = new ArrayList<>();

    private HashMap<Thread, String> typing = new HashMap<>();
    protected PublishSubject<Thread> onClickSubject = PublishSubject.create();
    protected PublishSubject<Thread> onLongClickSubject = PublishSubject.create();

    public ThreadsListAdapter(Context context){
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

        holder.nameTextView.setText(Strings.nameForThread(thread));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSubject.onNext(thread);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongClickSubject.onNext(thread);
                return true;
            }
        });

        Date lastMessageAddedDate = thread.getLastMessageAddedDate();
        if(lastMessageAddedDate != null) {
            holder.dateTextView.setText(getLastMessageDateAsString(lastMessageAddedDate));

            Message message = thread.getLastMessage();
            if(message == null) {
                List<Message> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC, 1);
                if(messages.size() > 0) {
                    message = messages.get(0);
                    thread.setLastMessage(message);
                    thread.update();
                }
            }

            holder.lastMessageTextView.setText(getLastMessageText(message));
        }

        if(typing.get(thread) != null) {
            // TODO: Localize
            holder.lastMessageTextView.setText(typing.get(thread) + context.get().getString(R.string.typing));
        }

        int unreadMessageCount = thread.getUnreadMessagesCount();

        if (unreadMessageCount != 0 && thread.typeIs(ThreadType.Private)) {

            holder.unreadMessageCountTextView.setText(String.valueOf(unreadMessageCount));
            holder.unreadMessageCountTextView.setVisibility(View.VISIBLE);

            holder.showUnreadIndicator();
        }
        else {
            holder.hideUnreadIndicator();
            holder.unreadMessageCountTextView.setVisibility(View.INVISIBLE);
        }

        ThreadImageBuilder.load(holder.imageView, thread);
    }

    public String getLastMessageDateAsString (Date date) {
        if(date != null) {
            return Strings.dateTime(date);
        }
        return null;
    }

    public String getLastMessageText (Message lastMessage) {
        String messageText = Strings.t(R.string.no_messages);
        if(lastMessage != null) {
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

    public void addRow (Thread thread, boolean notify) {
        if(!threads.contains(thread)) {
            threads.add(thread);
            if(notify) {
                notifyDataSetChanged();
            }
        }
    }

    public void addRow(Thread thread){
        addRow(thread, true);
    }

    public void setTyping (Thread thread, String message) {
        if(message != null) {
            typing.put(thread, message);
        }
        else {
            typing.remove(thread);
        }
    }

    protected void sort(){
        Collections.sort(threads, new ThreadSorter());
    }

    public void clearData () {
        clearData(true);
    }

    public void clearData (boolean notify) {
        threads.clear();
        if(notify) {
            notifyDataSetChanged();
        }
    }

    public Observable<Thread> onClickObservable () {
        return onClickSubject;
    }

    public Observable<Thread> onLongClickObservable () {
        return onLongClickSubject;
    }

    public void setThreads(List<Thread> threads) {
        clearData(false);
        for(Thread t : threads) {
            addRow(t, false);
        }
        sort();
        notifyDataSetChanged();
    }
}