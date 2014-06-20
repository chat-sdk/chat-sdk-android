package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.volley.RoundedCornerNetworkImageView;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.braunster.chatsdk.dao.BMessage.Type.bText;
import static com.braunster.chatsdk.dao.BMessage.Type.values;

/**
 * Created by itzik on 6/16/2014.
 */
public class ThreadsListAdapter extends BaseAdapter {

    private static final String TAG = ThreadsListAdapter.class.getSimpleName();

    private Activity mActivity;

    private List<BThread> listData = new ArrayList<BThread>();

    private BMessage.Type[] types;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm dd/MM/yy");

    //View
    private View row;

    private BThread thread;

    public ThreadsListAdapter(Activity activity){
        mActivity = activity;

        init();
    }

    public ThreadsListAdapter(Activity activity, List<BThread> listData){
        mActivity = activity;

        // Prevent crash due to null pointer. When a thread will be found it could be added using AddRow or setListData.
        if (listData == null)
            listData = new ArrayList<BThread>();

        this.listData = listData;
        init();
    }

    private void init(){
        types = values();
    }
    @Override
    public int getCount() {
        return listData != null ? listData.size() : 0;
    }

    @Override
    public BThread getItem(int i) {
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
            holder.txtName = (TextView) row.findViewById(R.id.txt_name);
            holder.txtLastMsg = (TextView) row.findViewById(R.id.txt_last_message);
            holder.txtDate = (TextView) row.findViewById(R.id.txt_last_message_date);
            holder.imgIcon = (CircleImageView) row.findViewById(R.id.img_thread_image);

            row.setTag(holder);
        }
        else
            holder = (ViewHolder) row.getTag();


        // Check if thread has a name,
        // For private and one on one threads with no name we will put the other user name.
        if (thread.getName() == null || thread.getName().equals(""))
        {
            if (thread.getType() == BThread.Type.Private.ordinal())
            {
                if (BNetworkManager.getInstance().currentUser().getEntityID().equals(thread.getUsers().get(0).getUserID()))
                    thread.setName(thread.getUsers().get(1).getBUser().getName());
                else thread.setName(thread.getUsers().get(0).getBUser().getName());
            }
            else
                thread.setName("Chat Room");
        }

        if (holder == null)
            Log.e(TAG, "Holder is null");

        if (holder.txtName == null)
            Log.e(TAG, "textview name is null");

        holder.txtName.setText(thread.getName());
        holder.txtLastMsg.setText(listData.get(position).getName());

        messageLogic(holder, position);

        if (listData.get(position).getUsers().size() > 2)
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

    public void addRow(BThread thread){

        listData.add(thread);

        notifyDataSetChanged();
    }

    public void setListData(List<BThread> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    private void messageLogic(ViewHolder holder, int position){
        BMessage message;
        // If no message create dummy message.
        if (thread.getMessages().size() == 0)
        {
            message = new BMessage();
            message.setText("No Messages...");
            message.setType(bText.ordinal());
        }
        else message = thread.getMessages().get(0);


        if (message.getEntityID() == null)
        {
            Log.e(TAG, "Message has no entity");
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
}