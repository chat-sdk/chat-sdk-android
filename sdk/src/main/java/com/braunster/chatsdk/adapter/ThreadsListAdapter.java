package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BThread;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/16/2014.
 */
public class ThreadsListAdapter extends AbstractThreadsListAdapter {

    private static final String TAG = ThreadsListAdapter.class.getSimpleName();
    public static final boolean DEBUG = Debug.ThreadsListAdapter;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yy");

    //View
    private View row;

    private ThreadListItem thread;

    public ThreadsListAdapter(Activity activity) {
        super(activity);
    }

    public ThreadsListAdapter(Activity activity, List<ThreadListItem> listData) {
        super(activity, listData);
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

        row = view;

        final ViewHolder holder;

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

        holder.txtName.setText(thread.getName());
        holder.txtDate.setText(thread.getLastMessageDateAsString());
        holder.txtLastMsg.setText(thread.getLastMessageText());

//        messageLogic(holder, position);

        //If has image url saved load it.
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

        return row;
    }

    private class ViewHolder{
        TextView txtName, txtDate, txtLastMsg;
        CircleImageView imgIcon;

        private void setDefaultImg(ThreadListItem item){
            if (item.getUsersAmount() > 2)
                imgIcon.setImageResource(R.drawable.ic_profile);
            else
                imgIcon.setImageResource(R.drawable.ic_users);
        }
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

}