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

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/16/2014.
 */
public class ThreadsListAdapter extends AbstractThreadsListAdapter {

    private static final String TAG = ThreadsListAdapter.class.getSimpleName();
    public static final boolean DEBUG = Debug.ThreadsListAdapter;

    public ThreadsListAdapter(Activity activity) {
        super(activity);
    }

    public ThreadsListAdapter(Activity activity, List<ThreadListItem> listData) {
        super(activity, listData);
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
            holder.txtUnreadMessagesAmount= (TextView) row.findViewById(R.id.txt_unread_messages);

            row.setTag(holder);
        }
        else
            holder = (ViewHolder) row.getTag();

        holder.txtName.setText(thread.getName());
        holder.txtDate.setText(thread.getLastMessageDateAsString());
        holder.txtLastMsg.setText(thread.getLastMessageText());

        int unreadMsg = thread.getUnreadMessagesAmount();
        if (DEBUG) Log.d(TAG, "Unread messages amount: " + unreadMsg);
        if (unreadMsg!=0)
        {
            holder.txtUnreadMessagesAmount.setText(String.valueOf(unreadMsg));
            holder.txtUnreadMessagesAmount.setVisibility(View.VISIBLE);
        }
        else holder.txtUnreadMessagesAmount.setVisibility(View.INVISIBLE);

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
        else holder.setDefaultImg(listData.get(position));

        return row;
    }
}