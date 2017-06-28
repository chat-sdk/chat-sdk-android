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

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import co.chatsdk.ui.utils.Strings;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.BiConsumer;
import timber.log.Timber;

public class ThreadsListAdapter extends BaseAdapter {

    protected static final String TAG = ThreadsListAdapter.class.getSimpleName();
    protected static final boolean DEBUG = Debug.ThreadsListAdapter;

    protected AppCompatActivity activity;

    protected List<ThreadListItem> allItems = new ArrayList<>();
    protected List<ThreadListItem> listItems = new ArrayList<>();

    public static final int TYPE_THREAD = 0;

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
        return getItemViewType(position) == TYPE_THREAD;
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
            holder.txtName = (TextView) row.findViewById(R.id.chat_sdk_txt);
            holder.txtLastMsg = (TextView) row.findViewById(R.id.txt_last_message);
            holder.txtDate = (TextView) row.findViewById(R.id.txt_last_message_date);
            holder.imgIcon = (CircleImageView) row.findViewById(R.id.img_thread_image);
            holder.txtUnreadMessagesAmount= (TextView) row.findViewById(R.id.txt_unread_messages);
            holder.indicator = row.findViewById(R.id.chat_sdk_indicator);

            row.setTag(holder);
        }
        else
            holder = (ThreadViewHolder) row.getTag();

        holder.txtName.setText(thread.getName());

        if(thread.getThread().getLastMessageAdded() != null) {
            holder.txtDate.setText(thread.getLastMessageDateAsString());
            holder.txtLastMsg.setText(thread.getLastMessageText());
        }
        else {
            // TODO: Maybe add a messages saying no messages
        }

        int unreadMessageCount = thread.getUnreadMessagesCount();
        if (DEBUG) Timber.d("Unread messages amount: %s", unreadMessageCount);

        if (unreadMessageCount != 0 &&  thread.getIsPrivate()) {

            holder.txtUnreadMessagesAmount.setText(String.valueOf(unreadMessageCount));
            holder.txtUnreadMessagesAmount.setVisibility(View.VISIBLE);

            holder.showUnreadIndicator();
        }
        else {
            holder.hideUnreadIndicator();
            holder.txtUnreadMessagesAmount.setVisibility(View.INVISIBLE);
        }

        // This should be quick because we're loding the image using iON so
        // they will be cached
        ThreadImageBuilder.getBitmapForThread(AppContext.context, getItem(position).getThread()).subscribe(new BiConsumer<Bitmap, Throwable>() {
            @Override
            public void accept(Bitmap bitmap, Throwable throwable) throws Exception {
                if(throwable == null) {
                    holder.imgIcon.setImageBitmap(bitmap);
                }
                else {
                    // TODO: Handle Error
                }
            }
        });

        return row;
    }


    public void addRow (ThreadListItem thread){
        allItems.add(thread);

        notifyDataSetChanged();
    }

    public void addRow(BThread thread){
        addRow(new ThreadListItem(thread));
    }

    public void setAllItems(List<BThread> threads) {

        this.listItems.clear();
        this.allItems.clear();

        for (BThread thread : threads) {
            addRow(thread);
        }

        this.listItems = allItems;

        if (filtering) {
            filterItems(filterText);
        }
        else sort();

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

    public ThreadListItem replaceOrAddItem(BThread thread){

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

    public static class ThreadListItem {

        private BThread thread;

        public ThreadListItem (BThread thread) {
            this.thread = thread;
        }

        public String getEntityID () {
            return thread.getEntityID();
        }

        public String getName () {
            return Strings.nameForThread(thread);
        }

        public boolean getIsPrivate () {
            return thread.typeIs(ThreadType.Private);
        }

        public Integer getType () {
            return TYPE_THREAD;
        }

        public String getLastMessageDateAsString () {
            if(getLastMessageDate() != null) {
                return Strings.dateTime(getLastMessageDate());
            }
            return null;
        }

        public String getLastMessageText () {
            String messageText = Strings.t(R.string.not_no_messages);

            List<BMessage> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC);

            if (messages.size() > 0) {
                BMessage message = messages.get(0);
                messageText = Strings.payloadAsString(message);
            }
            return messageText;
        }

        public int getUserCount() {
            return thread.getUsers().size();
        }

        public int getUnreadMessagesCount() {
            return thread.getUnreadMessagesAmount();
        }

        public long getId () {
            return thread.getId();
        }

        public Date getLastMessageDate () {
            return thread.getLastMessageAdded();
        }

        public BThread getThread () {
            return thread;
        }

        public static boolean compare(ThreadListItem newThread , ThreadListItem oldThread){

            if (newThread.getLastMessageDate() == null || oldThread.getLastMessageDate() == null)
                return true;

            if (newThread.getLastMessageDate().getTime() > oldThread.getLastMessageDate().getTime()) {
                return true;
            }

            if (!newThread.getName().equals(oldThread.getName()))
            {
                return true;
            }

            if (newThread.getUserCount() != oldThread.getUserCount())
            {
                return true;
            }

            if (StringUtils.isEmpty(newThread.thread.getImageURL()) && StringUtils.isEmpty(oldThread.thread.getImageURL()))
            {
                return false;
            }

            return !newThread.thread.getImageURL().equals(oldThread.thread.getImageURL());
        }

    }
}