package com.braunster.chatsdk.Utils;

import android.util.Log;

import com.braunster.chatsdk.adapter.ThreadsListAdapter;

import java.util.Comparator;

/**
 * Created by braunster on 18/06/14.
 */
public class ThreadsItemSorter implements Comparator<ThreadsListAdapter.ThreadListItem> {
    public static final int ORDER_TYPE_ASC = 0;
    public static final int ORDER_TYPE_DESC = 1;

    @Override
    public int compare(ThreadsListAdapter.ThreadListItem x, ThreadsListAdapter.ThreadListItem y) {
        // TODO: Handle null x or y values
        Log.e("asd", "COMPER");
        int startComparison = compare(x.getLastMessageDate().getTime(), y.getLastMessageDate().getTime());
        return startComparison != 0 ? startComparison
                : compare(x.getLastMessageDate().getTime(), y.getLastMessageDate().getTime());
    }

    private  int compare(long a, long b) {
        return a > b ? -1
                : a > b ? 1
                : 0;
    }
}
