/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import wanderingdevelopment.tk.sdkbaseui.R;
import co.chatsdk.core.defines.Debug;

import java.util.List;

import wanderingdevelopment.tk.sdkbaseui.view.CircleImageView;
import timber.log.Timber;

/**
 * Created by itzik on 6/16/2014.
 */
public class ThreadsListAdapter extends AbstractThreadsListAdapter {

    private static final String TAG = ThreadsListAdapter.class.getSimpleName();
    public static final boolean DEBUG = Debug.ThreadsListAdapter;

    public ThreadsListAdapter(AppCompatActivity activity) {
        super(activity);
    }

    public ThreadsListAdapter(AppCompatActivity activity, List<ThreadListItem> listData) {
        super(activity, listData);
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        View row = view;

        final ThreadViewHolder holder;

        thread = threadItems.get(position);

        if ( row == null)
        {
            holder = new ThreadViewHolder();
            row =  ( (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.chat_sdk_row_threads, null);
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
        holder.txtDate.setText(thread.getLastMessageDateAsString());
        holder.txtLastMsg.setText(thread.getLastMessageText());

        int unreadMsg = thread.getUnreadMessagesAmount();
        if (DEBUG) Timber.d("Unread messages amount: %s", unreadMsg);
        if (unreadMsg!=0 &&  thread.getIsPrivate())
        {
            holder.txtUnreadMessagesAmount.setText(String.valueOf(unreadMsg));
            holder.txtUnreadMessagesAmount.setVisibility(View.VISIBLE);

            holder.showUnreadIndicator();
        }
        else {
            holder.hideUnreadIndicator();
            holder.txtUnreadMessagesAmount.setVisibility(View.INVISIBLE);
        }

        setPic(holder, position);

        return row;
    }
}