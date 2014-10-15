/*
package com.braunster.chatsdk.adapter;

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

*/
/**
 * Created by itzik on 6/16/2014.
 *//*

public class AbstractThreadsListAdapterBackup extends BaseAdapter {

    protected static final String TAG = AbstractThreadsListAdapterBackup.class.getSimpleName();
    protected static final boolean DEBUG = Debug.ThreadsListAdapter;

    protected Activity mActivity;

    protected List<ThreadListItem> listData = new ArrayList<ThreadListItem>();

    protected static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");

    //View
    protected View row;

    protected ThreadListItem thread;

    public AbstractThreadsListAdapterBackup(Activity activity){
        mActivity = activity;
    }

    public AbstractThreadsListAdapterBackup(Activity activity, List<ThreadListItem> listData){
        mActivity = activity;

        // Prevent crash due to null pointer. When a thread will be found it could be added using AddRow or setListData.
        if (listData == null)
            listData = new ArrayList<ThreadListItem>();

        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData != null ? listData.size() : 0;
    }

    @Override
    public ThreadListItem getItem(int i) {
        return listData.get(i);
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
                imgIcon.setImageResource(R.drawable.ic_profile);
            else
                imgIcon.setImageResource(R.drawable.ic_users);
        }
    }

    public void setPic(final ViewHolder holder, final int position){
        int size = holder.imgIcon.getHeight();

        if (StringUtils.isNotEmpty(listData.get(position).getImageUrl()))
            VolleyUtils.getImageLoader().get(listData.get(position).getImageUrl(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (isImmediate && response.getBitmap() == null)
                    {
                        holder.setDefaultImg(listData.get(position));
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
                    holder.setDefaultImg(listData.get(position));
                }
            }, size, size);
        else  holder.setDefaultImg(listData.get(position));
    }

    public void addRow(ThreadListItem thread){
        listData.add(thread);

        notifyDataSetChanged();
    }

    public void addRow(BThread thread){
        addRow(ThreadListItem.fromBThread(thread));
    }

    public void setListData(List<ThreadListItem> listData) {
        this.listData = listData;

        notifyDataSetChanged();
    }

    public ThreadListItem replaceOrAddItem(BThread thread){
        boolean replaced = false, exist = false;
        ThreadListItem item = ThreadListItem.fromBThread(thread);

        for (int i = 0 ; i <listData.size() ; i++)
        {
            if (listData.get(i).entityId.equals(thread.getEntityID()))
            {
                exist = true;
                if (ThreadListItem.compare(item, listData.get(i)))
                {
                    listData.set(i, item);
                    replaced = true;
                }
                else
                {
                    replaced = false;
                }
            }
        }

        if (!exist)
            listData.add(ThreadListItem.fromBThread(thread));

        if (replaced || !exist) {
            if (DEBUG) Log.d(TAG, "Notify!, " + (replaced?"Replaced":!exist?"Not Exist":""));
            sort();
            notifyDataSetChanged();
        }

        return item;
    }

    protected void sort(){
        Collections.sort(listData, new ThreadsItemSorter());
    }

    public List<ThreadListItem> getListData() {
        return listData;
    }

    public static class ThreadListItem{
        public String entityId, name, imageUrl, lastMessageDate, lastMessageText;
        public int usersAmount = 0, unreadMessagesAmount = 0;
        public long id;
        public Date date;


        public ThreadListItem(String entityId, String name, String imageUrl, String lastMessageDate, String lastMessageText, int usersAmount, int unreadMessagesAmount, long id, Date date) {
            this.entityId = entityId;
            this.name = name;
            this.imageUrl = imageUrl;
            this.lastMessageDate = lastMessageDate;
            this.lastMessageText = lastMessageText;
            this.usersAmount = usersAmount;
            this.unreadMessagesAmount = unreadMessagesAmount;
            this.id = id;
            this.date = date;
        }

        public static ThreadListItem fromBThread(BThread thread){
            String[] data = new String[2];

            getLastMessageTextAndDate(thread, data);

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
                                      thread.getLastMessageAdded());
        }

        public static List<ThreadListItem> makeList(List<BThread> threads){
            List<ThreadListItem > list = new ArrayList<ThreadListItem>();
            TimingLogger logger;
            if (DEBUG) logger = new TimingLogger(TAG.substring(0, 20), "makeList");

            int count= 0;
            for (BThread thread : threads)
            {
                count++;
                if (DEBUG) logger.addSplit("fromThread" + count);
                list.add(fromBThread(thread));
            }

            if (DEBUG){
                logger.dumpToLog();
                logger.reset(TAG.substring(0, 20), "makeList");
            }
            return list;
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

        private static String[] getLastMessageTextAndDate(BThread thread, String[] data){
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
}*/
