package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.network.BNetworkManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.braunster.chatsdk.dao.BMessage.Type.bText;
import static com.braunster.chatsdk.dao.BMessage.Type.values;

/**
 * Created by itzik on 6/16/2014.
 */
public class ThreadsListAdapter extends BaseAdapter {

    private static final String TAG = ThreadsListAdapter.class.getSimpleName();

    private Activity mActivity;

    private List<BThread> listData = new ArrayList<BThread>();

    private int textColor = -1;

    private BMessage.Type[] types;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm dd/MM/yy");

    //View
    private View row;

    private TextView textView;
    private BThread thread;

    public ThreadsListAdapter(Activity activity){
        mActivity = activity;
        init();
    }

    public ThreadsListAdapter(Activity activity, List<BThread> listData){
        mActivity = activity;
        this.listData = listData;
        init();
    }

    private void init(){
        types = values();
    }
    @Override
    public int getCount() {
        return listData.size();
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
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.row_threads, null);
            holder.txtName = (TextView) row.findViewById(R.id.txt_name);
            holder.txtLastMsg = (TextView) row.findViewById(R.id.txt_last_message);
            holder.txtDate = (TextView) row.findViewById(R.id.txt_last_message_date);
            holder.imgIcon = (ImageView) row.findViewById(R.id.img_thread_image);
        }
        else
            holder = (ViewHolder) row.getTag();



        if (textColor != -1)
            textView.setTextColor(textColor);

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

        holder.txtName.setText(thread.getName());
        holder.txtLastMsg.setText(listData.get(position).getName());

        //TODO get the last message from the thread.
        BMessage message;
        // If no message create dummy message.
        if (listData.get(position).getMessages().size() == 0)
        {
            message = new BMessage();
            message.setText("No Messages...");
            message.setType(bText.ordinal());
        }

        else message = listData.get(position).getMessages().get(0);

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

        if (listData.get(position).getUsers().size() > 2)
            holder.imgIcon.setImageResource(R.drawable.icn_user_x_2);
        else
            holder.imgIcon.setImageResource(R.drawable.icn_group_x_2);
        // TODO set thread icon to custom one if supported.

        return row;
    }

    private class ViewHolder{
        TextView txtName, txtDate, txtLastMsg;
        ImageView imgIcon;
    }

    public void addRow(BThread thread){

        listData.add(thread);

        notifyDataSetChanged();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setListData(List<BThread> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }
}