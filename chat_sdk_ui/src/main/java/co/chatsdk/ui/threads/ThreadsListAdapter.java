/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.ui.R;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ThreadsListAdapter extends RecyclerView.Adapter<ThreadViewHolder> {

    protected Activity activity;
    protected List<ThreadListItem> items = new ArrayList<>();
    private HashMap<Thread, String> typing = new HashMap<>();
    protected PublishSubject<Thread> onClickSubject = PublishSubject.create();
    protected PublishSubject<Thread> onLongClickSubject = PublishSubject.create();

    public ThreadsListAdapter(Activity activity){
        this.activity = activity;
    }

    @Override
    public ThreadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.chat_sdk_row_thread, null);
        return new ThreadViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final ThreadViewHolder holder, int position) {

        final ThreadListItem thread = items.get(position);

        holder.nameTextView.setText(thread.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSubject.onNext(thread.getThread());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongClickSubject.onNext(thread.getThread());
                return true;
            }
        });

        if(thread.getThread().getLastMessageAddedDate() != null) {
            holder.dateTextView.setText(thread.getLastMessageDateAsString());
            holder.lastMessageTextView.setText(thread.getLastMessageText());
        }
        if(typing.get(thread.getThread()) != null) {
            holder.lastMessageTextView.setText(typing.get(thread.getThread()) + activity.getString(R.string.typing));
        }

        int unreadMessageCount = thread.getUnreadMessagesCount();

        if (unreadMessageCount != 0 && thread.getIsPrivate()) {

            holder.unreadMessageCountTextView.setText(String.valueOf(unreadMessageCount));
            holder.unreadMessageCountTextView.setVisibility(View.VISIBLE);

            holder.showUnreadIndicator();
        }
        else {
            holder.hideUnreadIndicator();
            holder.unreadMessageCountTextView.setVisibility(View.INVISIBLE);
        }

        ThreadImageBuilder.load(holder.imageView, thread.getThread());

    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addRow (ThreadListItem thread){
        addRow(thread, true);
    }

    public void addRow (ThreadListItem thread, boolean notify) {
        items.add(thread);
        if(notify) {
            notifyDataSetChanged();
        }
    }

    public void addRow(Thread thread){
        addRow(thread, true);
    }

    public void addRow(Thread thread, boolean notify){
        addRow(new ThreadListItem(thread), notify);
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
        Collections.sort(items, new ThreadsItemSorter());
    }

    public void clearData () {
        clearData(true);
    }

    public void clearData (boolean notify) {
        items.clear();
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

    public void setItems (List<Thread> items) {
        clearData(false);
        for(Thread t : items) {
            addRow(t, false);
        }
        sort();
        notifyDataSetChanged();
    }
}