/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.dao.DaoCore;
import wanderingdevelopment.tk.sdkbaseui.R;
import co.chatsdk.core.defines.Debug;

import wanderingdevelopment.tk.sdkbaseui.UiHelpers.MakeThreadImage;
import co.chatsdk.core.utils.volley.VolleyUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import wanderingdevelopment.tk.sdkbaseui.utils.Strings;
import timber.log.Timber;

/**
 * Created by itzik on 6/16/2014.
 */
public abstract class AbstractThreadsListAdapter extends BaseAdapter {

    protected static final String TAG = AbstractThreadsListAdapter.class.getSimpleName();
    protected static final boolean DEBUG = Debug.ThreadsListAdapter;

    protected AppCompatActivity mActivity;

    protected List<ThreadListItem> threadItems = new ArrayList<>();
    protected List<ThreadListItem> listData = new ArrayList<>();

    public static final int TYPE_THREAD = 0;

    protected String filterText = "";
    protected boolean filtering = false;

    protected ThreadListItem thread;


    public AbstractThreadsListAdapter(AppCompatActivity activity){
        mActivity = activity;
    }

    public AbstractThreadsListAdapter(AppCompatActivity activity, List<ThreadListItem> threadItems){
        mActivity = activity;

        if (threadItems != null)
            this.threadItems = threadItems;

    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getCount() {
        return threadItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return threadItems.get(position).getType();
    }

    /** Disabling the header vies from clicks.*/
    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_THREAD;
    }

    @Override
    public ThreadListItem getItem(int i) {
        return threadItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        return null;
    }

    public void setPic(final ThreadViewHolder holder, final int position){

        // Canceling the old task if has any.
        if (holder.makeThreadImage!=null)
            holder.makeThreadImage.cancel(false);

        //If has image url saved load it.
        int size = holder.imgIcon.getHeight();

        // Splitting the url to see if there is more then one url.
        final String urls[] = thread.getImageURL().split(",");

        // Kill the old loader and make a new.
        holder.initPicLoader(thread);

        // Check if the url isn't empty and if only contains one url. If so we load the image using volley.
        if (StringUtils.isNotEmpty(getItem(position).getImageURL()) && urls.length == 1)
            VolleyUtils.getImageLoader().get(getItem(position).getImageURL(), holder.picLoader);
        else {
            // If thread image url contain more then one url.
            if (urls.length > 1)
            {
                // If we do not yet have size post the creation
                if (size==0)
                    holder.imgIcon.post(new Runnable() {
                        @Override
                        public void run() {
                            int size = holder.imgIcon.getHeight();

                            if (DEBUG) Timber.d("Making thread image.");
                            //Default image while loading
                            holder.setMultipleUserDefaultImg();

                            holder.makeThreadImage = new MakeThreadImage(urls, size, size, thread.getEntityID(), holder.imgIcon);
                        }
                    });
                else
                {
                    if (DEBUG) Timber.d("Making thread image.");
                    //Default image while loading
                    holder.setMultipleUserDefaultImg();

                    holder.makeThreadImage = new MakeThreadImage(urls, size, size, thread.getEntityID(), holder.imgIcon);
                }
            }
            // Url is empty show default.
            else
            {
                holder.setDefaultImg(getItem(position));
            }
        }
    }

    public void addRow (ThreadListItem thread){
        threadItems.add(thread);

        notifyDataSetChanged();
    }

    public void addRow(BThread thread){
        addRow(new ThreadListItem(thread));
    }

    public void setThreadItems(List<BThread> threads) {

        this.listData.clear();
        this.threadItems.clear();

        for (BThread thread : threads) {
            addRow(thread);
        }

        this.listData = threadItems;

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

        if (StringUtils.isBlank(text) || StringUtils.isEmpty(text))
        {
            this.threadItems = listData;
            filtering = false;
        }
        else
        {

            List<ThreadListItem> filteredItems = new ArrayList<>();

            for (ThreadListItem t : listData)
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

            this.threadItems = filteredItems;
        }

        notifyDataSetChanged();
    }

    public ThreadListItem replaceOrAddItem(BThread thread){

        boolean replaced = false;
        boolean exist = false;

        ThreadListItem item = new ThreadListItem(thread);

        for (int i = 0 ; i < threadItems.size() ; i++)
        {
            if (threadItems.get(i).getEntityID().equals(thread.getEntityID())) {

                exist = true;

                if (ThreadListItem.compare(item, threadItems.get(i)))
                {
                    threadItems.set(i, item);
                    replaced = true;
                }
                else
                {
                    replaced = false;
                }
            }
        }

        if (!exist)
            threadItems.add(new ThreadListItem(thread));

        if (replaced || !exist) {
            if (DEBUG) Timber.d("Notify!, %s", (replaced ? "Replaced": !exist ? "Not Exist":""));
            sort();
            notifyDataSetChanged();
        }

        return item;
    }

    protected void sort(){
        Collections.sort(threadItems, new ThreadsItemSorter());
    }

    public List<ThreadListItem> getThreadItems() {
        return threadItems;
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

        public String getImageURL () {
            return thread.threadImageUrl();
        }

        public String getLastMessageDateAsString () {
            return Strings.dateTime(getLastMessageDate());
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

        public int getUnreadMessagesAmount () {
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

            if (StringUtils.isEmpty(newThread.getImageURL()) && StringUtils.isEmpty(oldThread.getImageURL()))
            {
                return false;
            }

            return !newThread.getImageURL().equals(oldThread.getImageURL());
        }

    }
}