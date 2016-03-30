/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.adapter.abstracted;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.TimingLogger;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.asynctask.MakeThreadImage;
import com.braunster.chatsdk.Utils.sorter.ThreadsItemSorter;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.LOCATION;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.TEXT;

/**
 * Created by itzik on 6/16/2014.
 */
public abstract class ChatSDKAbstractThreadsListAdapter<E extends ChatSDKAbstractThreadsListAdapter.ThreadListItem> extends BaseAdapter {

    protected static final String TAG = ChatSDKAbstractThreadsListAdapter.class.getSimpleName();
    protected static final boolean DEBUG = Debug.ThreadsListAdapter;

    protected Activity mActivity;

    protected List<E> threadItems = new ArrayList<E>();
    protected List<E> listData = new ArrayList<E>();

    public static final String H_GROUPS = "Group Conversations";
    public static final String H_MORE_PEOPLE = "More People";

    public static final int TYPE_THREAD = 0;
    public static final int TYPE_HEADER = 1;

    protected boolean withHeaders = true;
    protected String filterText = "";

    protected static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");

    protected ThreadListItemMaker<E> itemMaker;

    //View
    protected View row;

    protected E thread;

    protected boolean filtering = false;

    public ChatSDKAbstractThreadsListAdapter(Activity activity){
        mActivity = activity;
        initMaker();
    }

    public ChatSDKAbstractThreadsListAdapter(Activity activity, List<E> threadItems){
        mActivity = activity;

        // Prevent crash due to null pointer. When a thread will be found it could be added using AddRow or setThreadItems.
        if (threadItems == null)
            threadItems = new ArrayList<E>();

        this.threadItems = threadItems;

        initMaker();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return threadItems != null ? threadItems.size() : 0;
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

    public class ViewHolder{
        public TextView txtName;
        public TextView txtDate;
        public TextView txtLastMsg;
        public TextView txtUnreadMessagesAmount;
        public CircleImageView imgIcon;
        public View indicator;

        public void setDefaultImg(ThreadListItem item){
            if (item.getUsersAmount() > 2)
                setMultipleUserDefaultImg();
            else
                setTwoUsersDefaultImg();
        }

        public void setMultipleUserDefaultImg(){
            imgIcon.setImageResource(R.drawable.ic_users);
        }

        public void setTwoUsersDefaultImg(){
            imgIcon.setImageResource(R.drawable.ic_profile);
        }

        public void showUnreadIndicator(){
            indicator.setVisibility(View.VISIBLE);
        }

        public void hideUnreadIndicator(){
            indicator.setVisibility(View.GONE);
        }

        public PicLoader picLoader;

        public PicLoader initPicLoader(ThreadListItem threadListItem){
            if (picLoader!=null)
                picLoader.kill();

            picLoader = new PicLoader(threadListItem);
            return picLoader;
        }

        class PicLoader implements ImageLoader.ImageListener{

            private boolean killed = false;

            private ThreadListItem threadListItem;


            PicLoader(ThreadListItem threadListItem) {
                this.threadListItem = threadListItem;
            }

            private void kill() {
                this.killed = true;
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                if (killed)
                    return;

                // If response was not immidate, i.e  image was cached we show the default image while loading
                if (isImmediate && response.getBitmap() == null)
                {
                    setDefaultImg(threadListItem);
                    return;
                }

                // Set the response to the image.
                if (response.getBitmap() != null) {
                    if (DEBUG) Timber.i("Loading thread picture from url");

                    // load image into imageview
                    imgIcon.setImageBitmap(response.getBitmap());
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (DEBUG) Timber.e("Image Load Error: %s", error.getMessage());

                if (killed)
                    return;

                // in case of error we show the default.
                setDefaultImg(threadListItem);
            }
        }

        public MakeThreadImage makeThreadImage;
    }

    public void setPic(final ViewHolder holder, final int position){

        // Canceling the old task if has any.
        if (holder.makeThreadImage!=null)
            holder.makeThreadImage.cancel(false);

        //If has image url saved load it.
        int size = holder.imgIcon.getHeight();

        // Splitting the url to see if there is more then one url.
        final String urls[] = thread.getImageUrl().split(",");

        // Kill the old loader and make a new.
        holder.initPicLoader(thread);

        // Check if the url isn't empty and if only contains one url. If so we load the image using volley.
        if (StringUtils.isNotEmpty(getItem(position).getImageUrl()) && urls.length == 1)
            VolleyUtils.getImageLoader().get(getItem(position).getImageUrl(), holder.picLoader);
        else {
//            if (DEBUG) Log.d(TAG, "UrlsString: " + thread.getImageUrl() + ", Urls length: " + urls.length);

            // If thread image url contain more then one url.
            if (urls.length > 1)
            {
//                if (DEBUG) Log.d(TAG, "Thread has more then 2 users.");

                // If we do not yet have size post the creation
                if (size==0)
                    holder.imgIcon.post(new Runnable() {
                        @Override
                        public void run() {
                            int size = holder.imgIcon.getHeight();

                            if (DEBUG) Timber.d("Making thread image.");
                            //Default image while loading
                            holder.setMultipleUserDefaultImg();

                            holder.makeThreadImage = new MakeThreadImage(urls, size, size, thread.getEntityId(), holder.imgIcon);
                        }
                    });
                else
                {
                    if (DEBUG) Timber.d("Making thread image.");
                    //Default image while loading
                    holder.setMultipleUserDefaultImg();

                    holder.makeThreadImage = new MakeThreadImage(urls, size, size, thread.getEntityId(), holder.imgIcon);
                }
            }
            // Url is empty show default.
            else
            {
                holder.setDefaultImg(getItem(position));
            }
        }
    }

    public void addRow(E thread){
        threadItems.add(thread);

        notifyDataSetChanged();
    }

    public void addRow(BThread thread){
        addRow(itemMaker.fromBThread(thread));
    }

    public void setThreadItems(List<E> threadItems) {

        this.listData.clear();
        this.threadItems.clear();

        this.threadItems = threadItems;
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

        List<E> startWith = new ArrayList<E>();
        List<E> contain = new ArrayList<E>();
        List<E> groups = new ArrayList<E>();

        if (StringUtils.isBlank(text) || StringUtils.isEmpty(text))
        {
            this.threadItems = listData;
            filtering = false;
        }
        else
        {

            List<E> filteredUsers = new ArrayList<E>();

            for (E t : listData)
            {
                // Check if group and if has the filter
                if (t.getUsersAmount() > 2 && t.getName().toLowerCase().contains(filterText))
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

            filteredUsers.addAll(startWith);
            filteredUsers.addAll(contain);

            E headGroup = itemMaker.getGroupsHeader();

            if (withHeaders && headGroup != null && groups.size() > 0)
                filteredUsers.add(headGroup);

            filteredUsers.addAll(groups);

            this.threadItems = filteredUsers;
        }


        notifyDataSetChanged();
    }

    public E replaceOrAddItem(BThread thread){
        boolean replaced = false, exist = false;
        E item = itemMaker.fromBThread(thread);

        for (int i = 0 ; i < threadItems.size() ; i++)
        {
            if (threadItems.get(i).entityId.equals(thread.getEntityID()))
            {
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
            threadItems.add(itemMaker.fromBThread(thread));

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

    public List<E> getThreadItems() {
        return threadItems;
    }

    /**
     * Used for creating the items that would fill the list.
     *
     * If you want to add more data to your list you can extend the absract adapter after that you can extend the {@link com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractThreadsListAdapter.ThreadListItem ThreadListItem}
     * And add new variables to keep it and populate then in with your maker.
     *
     * @see com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractThreadsListAdapter.ThreadListItemMaker#fromBThread(com.braunster.chatsdk.dao.BThread)
     * @see com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractThreadsListAdapter.ThreadListItemMaker#getGroupsHeader()
     * @see @see com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractThreadsListAdapter.ThreadListItemMaker#getMorePeopleHeader
     * */
    public interface ThreadListItemMaker<E>{
        public E fromBThread(BThread thread);
        public E getGroupsHeader();
        public E getMorePeopleHeader();
    }

    public List<ThreadListItem> makeList(List<BThread> threads){
        List<ThreadListItem > list = new ArrayList<ThreadListItem>();
        TimingLogger logger;
        if (DEBUG) logger = new TimingLogger(TAG.substring(0, 20), "makeList");

//        int count= 0;
        for (BThread thread : threads)
        {
//            count++;
//            if (DEBUG) logger.addSplit("fromThread" + count);
            list.add(itemMaker.fromBThread(thread));
        }

        if (DEBUG){
            logger.dumpToLog();
            logger.reset(TAG.substring(0, 20), "makeList");
        }

        return list;
    }

    public static class ThreadListItem{
        public String entityId, name, imageUrl, lastMessageDate, lastMessageText;
        public int usersAmount = 0, unreadMessagesAmount = 0;
        public long id;
        public Date date;
        public boolean isPrivate;

        public int type;

        public ThreadListItem(String title){
            this.name = title;
            type = TYPE_HEADER;
        }

        public ThreadListItem(String entityId, String name, String imageUrl, String lastMessageDate, String lastMessageText, int usersAmount, int unreadMessagesAmount, long id, Date date, boolean isPrivate) {
            this.entityId = entityId;
            this.name = name;
            this.isPrivate = isPrivate;
            this.type = TYPE_THREAD;
            this.imageUrl = imageUrl;
            this.lastMessageDate = lastMessageDate;
            this.lastMessageText = lastMessageText;
            this.usersAmount = usersAmount;
            this.unreadMessagesAmount = unreadMessagesAmount;
            this.id = id;
            this.date = date;
        }

        public int getType() {
            return type;
        }

        public static boolean compare(ThreadListItem newThread , ThreadListItem oldThread){

            if (newThread.getLastMessageDate() == null || oldThread.getLastMessageDate() == null)
                return true;

            if (newThread.getLastMessageDate().getTime() > oldThread.getLastMessageDate().getTime()) {
                return true;
            }

            if (!newThread.name.equals(oldThread.name))
            {
                return true;
            }

            if (newThread.getUsersAmount() != oldThread.getUsersAmount())
            {
                return true;
            }

            if (StringUtils.isEmpty(newThread.imageUrl) && StringUtils.isEmpty(oldThread.imageUrl))
            {
                return false;
            }

            return !newThread.imageUrl.equals(oldThread.imageUrl);
        }

        public static String[] getLastMessageTextAndDate(BThread thread, String[] data){
            List<BMessage> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC);

            // If no message create dummy message.
            if ( messages.size() == 0)
            {
//                if (DEBUG) Log.d(TAG, "No messages");
//            message = new BMessage();
//            message.setText("No Messages...");
//            message.setType(bText.ordinal());
                data[0] = "No Message";
                data[1] = "";
                return data;
            }

//            if (DEBUG) Log.d(TAG, "Message text: " + messages.get(0).getText());

            if (messages.get(0).getType() == null)
                data[0] = "Bad Data";
            else
                switch (messages.get(0).getType())
                {
                    case TEXT:
                        // TODO cut string if needed.
                        //http://stackoverflow.com/questions/3630086/how-to-get-string-width-on-android
                        data[0] = messages.get(0).getText();
                        break;

                    case IMAGE:
                        data[0] = "Image message";
                        break;

                    case LOCATION:
                        data[0] = "Location message";
                        break;
                }

            data[1] = simpleDateFormat.format(messages.get(0).getDate());

            return data;
        }

        public String getName() {
            return name;
        }

        public String getLastMessageDateAsString() {
            return lastMessageDate;
        }

        public Date getLastMessageDate() { return date;}

        public String getImageUrl() {
            return imageUrl;
        }

        public String getLastMessageText() {
            return lastMessageText;
        }

        public int getUsersAmount() {
            return usersAmount;
        }

        public String getEntityId() {
            return entityId;
        }

        public long getId() {
            return id;
        }

        public int getUnreadMessagesAmount() {
            return unreadMessagesAmount;
        }

        public boolean isImageChached(){
            return VolleyUtils.getBitmapCache().contains(getEntityId());
        }

        public Bitmap getCachedImage(){
            return VolleyUtils.getBitmapCache().get(getEntityId());
        }

        public void cacheImage(Bitmap bitmap){
            VolleyUtils.getBitmapCache().put(getEntityId(), bitmap);
        }
    }

    public void setItemMaker(ThreadListItemMaker<E> itemMaker) {
        this.itemMaker = itemMaker;
    }

    public ThreadListItemMaker<E> getItemMaker() {
        return itemMaker;
    }

    /*############################################*/
    public abstract void initMaker();

    protected ThreadListItemMaker<ThreadListItem> getDefaultMaker(){
        return new ThreadListItemMaker<ThreadListItem>() {
            @Override
            public ThreadListItem fromBThread(BThread thread) {
                String[] data = new String[2];

                ThreadListItem.getLastMessageTextAndDate(thread, data);

                List<BUser> users = thread.getUsers();
                String url  = thread.threadImageUrl();

                String displayName = thread.displayName(users);

                return new ThreadListItem(thread.getEntityID(),
                        StringUtils.isEmpty(displayName) ? "No name." : displayName,
                        url,
                        data[1],
                        data[0],
                        users.size(),
                        thread.getUnreadMessagesAmount(),
                        thread.getId(),
                        thread.getLastMessageAdded(),
                        thread.getTypeSafely() == BThread.Type.Private);
            }

            @Override
            public ThreadListItem getGroupsHeader() {
                return null;
            }

            @Override
            public ThreadListItem getMorePeopleHeader() {
                return null;
            }
        };
    }

}