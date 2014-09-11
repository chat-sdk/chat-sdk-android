package com.braunster.chatsdk.Utils.sorter;

import com.braunster.chatsdk.adapter.AbstractThreadsListAdapter;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by braunster on 18/06/14.
 */
public class ThreadsItemSorter implements Comparator<AbstractThreadsListAdapter.ThreadListItem> {
    public static final int ORDER_TYPE_ASC = 0;
    public static final int ORDER_TYPE_DESC = 1;

    private int order = ORDER_TYPE_DESC;

    public ThreadsItemSorter(){}

    public ThreadsItemSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(AbstractThreadsListAdapter.ThreadListItem t1, AbstractThreadsListAdapter.ThreadListItem t2) {
        Date x, y;
        if (t1.getLastMessageDate() == null)
        {
            x = new Date();
        }
        else x = t1.getLastMessageDate();

        if(t2.getLastMessageDate() == null)
        {
            y = new Date();
        } else y = t2.getLastMessageDate();

        if (order == ORDER_TYPE_ASC)
            return x.compareTo(y);
        else return y.compareTo(x);
    }
}
