package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.core.DaoCore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.LOCATION;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.TEXT;

/**
 * Created by itzik on 6/16/2014.
 */
public class ThreadsListAdapter extends BaseAdapter {

    private static final String TAG = ThreadsListAdapter.class.getSimpleName();
    public static final boolean DEBUG = true;

    private Activity mActivity;

    private List<ThreadListItem> listData = new ArrayList<ThreadListItem>();

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm dd/MM/yy");

    //View
    private View row;

    private ThreadListItem thread;

    public ThreadsListAdapter(Activity activity){
        mActivity = activity;
    }

    public ThreadsListAdapter(Activity activity, List<ThreadListItem> listData){
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
    public View getView(int position, View view, ViewGroup viewGroup) {

        row = view;

        ViewHolder holder;

        thread = listData.get(position);

        if ( row == null)
        {
            holder = new ViewHolder();
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.chat_sdk_row_threads, null);
            holder.txtName = (TextView) row.findViewById(R.id.chat_sdk_txt);
            holder.txtLastMsg = (TextView) row.findViewById(R.id.txt_last_message);
            holder.txtDate = (TextView) row.findViewById(R.id.txt_last_message_date);
            holder.imgIcon = (CircleImageView) row.findViewById(R.id.img_thread_image);

            row.setTag(holder);
        }
        else
            holder = (ViewHolder) row.getTag();

//        if (holder == null)
//            if (DEBUG) Log.e(TAG, "Holder is null");
//
//        if (holder.txtName == null)
//            if (DEBUG) Log.e(TAG, "textview name is null");

        holder.txtName.setText(thread.getName());
        holder.txtDate.setText(thread.getLastMessageDate());
        holder.txtLastMsg.setText(thread.getLastMessageText());

//        messageLogic(holder, position);

        if (listData.get(position).getUsersAmount() > 2)
            holder.imgIcon.setImageResource(R.drawable.icn_user_x_2);
        else
            holder.imgIcon.setImageResource(R.drawable.icn_group_x_2);
        // TODO set thread icon to custom one if supported.

        return row;
    }

    private class ViewHolder{
        TextView txtName, txtDate, txtLastMsg;
        CircleImageView imgIcon;
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

    public List<ThreadListItem> getListData() {
        return listData;
    }

    /*private void messageLogic(ViewHolder holder, int position){
            if (DEBUG) Log.v(TAG, "messageLogic");

            BMessage message;

            List<BMessage> messages = thread.getMessagesWithOrder(DaoCore.ORDER_DESC);

            // If no message create dummy message.
            if ( messages.size() == 0)
            {
                if (DEBUG) Log.d(TAG, "No messages");
    //            message = new BMessage();
    //            message.setText("No Messages...");
    //            message.setType(bText.ordinal());
                holder.txtLastMsg.setText("No Messages...");
                return;
            }
            else message = messages.get(0);

            if (DEBUG) Log.d(TAG, "Message text: " + message.getText());

            if (message.getId() == null)
            {
                Log.e(TAG, "Message has no id");
                Log.e(TAG, "Messages Amount: " + thread.getMessages().size()
                        + (message.getText() == null ? "No Text" : message.getText()));

                // Replace the problematic message with this dummy.
                message.setText("Defected Message");
                message.setEntityID(DaoCore.generateEntity());
                message.setType(bText.ordinal());
                message.setDate(new Date());
            }

            switch (types[message.getType()])
            {
                case bText:
                    // TODO cut string if needed.
                    //http://stackoverflow.com/questions/3630086/how-to-get-string-width-on-android
                    holder.txtLastMsg.setText(message.getText());
                    break;

                case bImage:
                    holder.txtLastMsg.setText("Image message");
                    break;

                case bLocation:
                    holder.txtLastMsg.setText("Location message");
                    break;
            }

            // Check if not dummy message.
            if (message.getDate() != null)
                holder.txtDate.setText(String.valueOf(simpleDateFormat.format(message.getDate())));
            else
                holder.txtDate.setText(String.valueOf(simpleDateFormat.format(new Date(System.currentTimeMillis()))));

        }
    */
    public static class ThreadListItem{
        private String entityId, name, lastMessageDate, imageUrl, lastMessageText;
        private int usersAmount = 0;
        private long id;

        ThreadListItem(long id, String entityId, String name, String lastMessageDate, String imageUrl, String lastMessageText, int usersAmount) {
            this.name = name;
            this.id = id;
            this.entityId = entityId;
            this.usersAmount = usersAmount;
            this.lastMessageDate = lastMessageDate;
            this.imageUrl = imageUrl;
            this.lastMessageText = lastMessageText;
        }

        public static ThreadListItem fromBThread(BThread thread){
            String[] data = getLastMessageTextAndDate(thread);
            return new ThreadListItem(thread.getId(), thread.getEntityID(), thread.displayName(), data[1], "", data[0], thread.getUsers().size());
        }

        public static List<ThreadListItem> makeList(List<BThread> threads){
            List<ThreadListItem > list = new ArrayList<ThreadListItem>();

            for (BThread thread : threads)
                list.add(fromBThread(thread));

            return list;
        }

        private static String[] getLastMessageTextAndDate(BThread thread){
            String[] data = new String[2];
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

 /*           if (message.getId() == null)
            {
                Log.e(TAG, "Message has no id");
                Log.e(TAG, "Messages Amount: " + thread.getMessages().size()
                        + (message.getText() == null ? "No Text" : message.getText()));

                // Replace the problematic message with this dummy.
                message.setText("Defected Message");
                message.setEntityID(DaoCore.generateEntity());
                message.setType(bText.ordinal());
                message.setDate(new Date());
            }*/

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

        public String getLastMessageDate() {
            return lastMessageDate;
        }

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
    }
}