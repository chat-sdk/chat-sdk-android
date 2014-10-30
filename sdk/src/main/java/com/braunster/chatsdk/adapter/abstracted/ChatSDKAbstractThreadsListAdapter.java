package com.braunster.chatsdk.adapter.abstracted;

import android.app.Activity;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
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

        public void setDefaultImg(ThreadListItem item){
            if (item.getUsersAmount() > 2)
                imgIcon.setImageResource(R.drawable.ic_users);
            else
                imgIcon.setImageResource(R.drawable.ic_profile);
        }
    }

    public void setPic(final ViewHolder holder, final int position){
        int size = holder.imgIcon.getHeight();

        if (StringUtils.isNotEmpty(threadItems.get(position).getImageUrl()))
            VolleyUtils.getImageLoader().get(threadItems.get(position).getImageUrl(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (isImmediate && response.getBitmap() == null)
                    {
                        holder.setDefaultImg(threadItems.get(position));
                        return;
                    }

                    if (response.getBitmap() != null) {
                        if (DEBUG) Log.i(TAG, "Loading thread picture from url");

                        // load image into imageview
                        holder.imgIcon.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (DEBUG) Log.e(TAG, "Image Load Error: " + error.getMessage());
                    holder.setDefaultImg(threadItems.get(position));
                }
            }, size, size);
        else  holder.setDefaultImg(threadItems.get(position));
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
            if (DEBUG) Log.v(TAG, "filterItems, Empty Filter");
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
            if (DEBUG) Log.d(TAG, "Notify!, " + (replaced?"Replaced":!exist?"Not Exist":""));
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

    public interface ThreadListItemMaker<E>{
        public E fromBThread(BThread thread);
        public E getGroupsHeader();
        public E getMorePeopleHeader();
    }

    public List<ThreadListItem> makeList(List<BThread> threads){
        List<ThreadListItem > list = new ArrayList<ThreadListItem>();
        TimingLogger logger;
        if (DEBUG) logger = new TimingLogger(TAG.substring(0, 20), "makeList");

        int count= 0;
        for (BThread thread : threads)
        {
            count++;
            if (DEBUG) logger.addSplit("fromThread" + count);
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
                if (DEBUG) Log.d(TAG, "compare, Date");
                return true;
            }

            if (!newThread.name.equals(oldThread.name))
            {
                if (DEBUG) Log.d(TAG, "compare, Name");
                return true;
            }

            if (newThread.getUsersAmount() != oldThread.getUsersAmount())
            {
                if (DEBUG) Log.d(TAG, "compare, Users");
                return true;
            }

            if (StringUtils.isEmpty(newThread.imageUrl) && StringUtils.isEmpty(oldThread.imageUrl))
            {
                if (DEBUG) Log.d(TAG, "compare false, Empty");
                return false;
            }

            return !newThread.imageUrl.equals(oldThread.imageUrl);
        }

        public static String[] getLastMessageTextAndDate(BThread thread, String[] data){
            List<BMessage> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC);

            // If no message create dummy message.
            if ( messages.size() == 0)
            {
                if (DEBUG) Log.d(TAG, "No messages");
//            message = new BMessage();
//            message.setText("No Messages...");
//            message.setType(bText.ordinal());
                data[0] = "No Message";
                data[1] = "";
                return data;
            }

            if (DEBUG) Log.d(TAG, "Message text: " + messages.get(0).getText());

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
                String url  = thread.threadImageUrl(users);

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
                        thread.getType() == BThread.Type.Private);
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