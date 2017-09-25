/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;
import timber.log.Timber;

public class ThreadsListAdapter extends BaseAdapter {

    protected static final String TAG = ThreadsListAdapter.class.getSimpleName();
    protected static final boolean DEBUG = Debug.ThreadsListAdapter;

    protected AppCompatActivity activity;

    protected List<ThreadListItem> allItems = new ArrayList<>();
    protected List<ThreadListItem> listItems = new ArrayList<>();


    protected String filterText = "";
    protected boolean filtering = false;

    protected ThreadListItem thread;

    public ThreadsListAdapter(AppCompatActivity activity){
        this.activity = activity;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getCount() {
        return allItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return allItems.get(position).getType();
    }

    /** Disabling the header vies from clicks.*/
    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == ThreadListItem.CELL_TYPE_THREAD;
    }

    @Override
    public ThreadListItem getItem(int i) {
        return allItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        View row = view;

        final ThreadViewHolder holder;

        thread = allItems.get(position);

        if ( row == null) {

            holder = new ThreadViewHolder();
            row =  ( (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.chat_sdk_row_threads, null);
            holder.nameTextView = (TextView) row.findViewById(R.id.chat_sdk_txt);
            holder.lastMessageTextView = (TextView) row.findViewById(R.id.txt_last_message);
            holder.dateTextView = (TextView) row.findViewById(R.id.txt_last_message_date);
            holder.imageView = (CircleImageView) row.findViewById(R.id.img_thread_image);
            holder.unreadMessageCountTextView = (TextView) row.findViewById(R.id.txt_unread_messages);
            holder.indicator = row.findViewById(R.id.chat_sdk_indicator);

            row.setTag(holder);
        }
        else {
            holder = (ThreadViewHolder) row.getTag();
        }

        holder.nameTextView.setText(thread.getName());

        if(thread.getThread().getLastMessageAddedDate() != null) {
            holder.dateTextView.setText(thread.getLastMessageDateAsString());
            holder.lastMessageTextView.setText(thread.getLastMessageText());
        }
        else {
            // TODO: Maybe add a messages saying no messages
        }

        int unreadMessageCount = thread.getUnreadMessagesCount();
        if (DEBUG) Timber.d("Unread messages amount: %s", unreadMessageCount);

        if (unreadMessageCount != 0 && thread.getIsPrivate()) {

            holder.unreadMessageCountTextView.setText(String.valueOf(unreadMessageCount));
            holder.unreadMessageCountTextView.setVisibility(View.VISIBLE);

            holder.showUnreadIndicator();
        }
        else {
            holder.hideUnreadIndicator();
            holder.unreadMessageCountTextView.setVisibility(View.INVISIBLE);
        }

        // This should be quick because we're loding the image using iON so
        // they will be cached
        ThreadImageBuilder.getBitmapForThread(AppContext.shared().context(), getItem(position).getThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumer<Bitmap, Throwable>() {
            @Override
            public void accept(Bitmap bitmap, Throwable throwable) throws Exception {
                if(throwable == null) {
                    holder.imageView.setImageBitmap(bitmap);
                }
                else {
                    // TODO: Handle Error
                }
            }
        });

        return row;
    }


    public void addRow (ThreadListItem thread){
        addRow(thread, true);
    }

    public void addRow (ThreadListItem thread, boolean notify){
        allItems.add(thread);
        if(notify) {
            notifyDataSetChanged();
        }
    }

    public void addRow(Thread thread){
        addRow(thread, true);
    }

    public void addRow(Thread thread, boolean notify){
        addRow(new ThreadListItem(thread));
    }

    public void setAllItems(List<Thread> threads) {

        this.listItems.clear();
        this.allItems.clear();

        for (Thread thread : threads) {
            addRow(thread, false);
        }

        this.listItems = allItems;

        notifyDataSetChanged();
    }

    /*Filtering option's of the list to make searches.*/
    public void filterItems(String text){
        filtering = true;
        filterText = text.trim().toLowerCase();

        List<ThreadListItem> startWith = new ArrayList<>();
        List<ThreadListItem> contain = new ArrayList<>();
        List<ThreadListItem> groups = new ArrayList<>();

        if (StringUtils.isBlank(text) || StringUtils.isEmpty(text)) {
            allItems = listItems;
            filtering = false;
        }
        else {

            List<ThreadListItem> filteredItems = new ArrayList<>();

            for (ThreadListItem t : listItems)
            {
                // Check if group and if has the filter
                if (t.getUserCount() > 2 && t.getName().toLowerCase().contains(filterText))
                    groups.add(t);
                    // Not group check if start with
                else if (t.getName().toLowerCase().startsWith(filterText))
                    startWith.add(t);
                    // Check if contained.
                else if (t.getName().toLowerCase().contains(filterText))
                {
                    contain.add(t);
                }
            }

            filteredItems.addAll(startWith);
            filteredItems.addAll(contain);
            filteredItems.addAll(groups);

            this.allItems = filteredItems;
        }

        notifyDataSetChanged();
    }

    public ThreadListItem replaceOrAddItem(Thread thread){

        boolean replaced = false;
        boolean exist = false;

        ThreadListItem item = new ThreadListItem(thread);

        for (int i = 0; i < allItems.size() ; i++)
        {
            if (allItems.get(i).getEntityID().equals(thread.getEntityID())) {

                exist = true;

                if (ThreadListItem.compare(item, allItems.get(i)))
                {
                    allItems.set(i, item);
                    replaced = true;
                }
                else
                {
                    replaced = false;
                }
            }
        }

        if (!exist)
            allItems.add(new ThreadListItem(thread));

        if (replaced || !exist) {
            if (DEBUG) Timber.d("Notify!, %s", (replaced ? "Replaced": !exist ? "Not Exist":""));
            sort();
            notifyDataSetChanged();
        }

        return item;
    }

    protected void sort(){
        Collections.sort(allItems, new ThreadsItemSorter());
    }

    public List<ThreadListItem> getAllItems() {
        return allItems;
    }

}