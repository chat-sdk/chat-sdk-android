package com.braunster.chatsdk.Utils.sorter;

import android.util.Log;

import com.braunster.chatsdk.dao.BThread;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by braunster on 18/06/14.
 */
public class ThreadsSorter implements Comparator<BThread> {
    public static final int ORDER_TYPE_ASC = 0;
    public static final int ORDER_TYPE_DESC = 1;

    private int order = ORDER_TYPE_DESC;

    public ThreadsSorter(){}

    public ThreadsSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(BThread t1, BThread t2) {
        /*FIXME handle nulls*/
        Date x, y;
        if (t1.lastMessageAdded() == null)
        {
            x = new Date();
            Log.d("SSSS", "ThreadName: " + t1.displayName());
        }
        else x = t1.lastMessageAdded();

        if(t2.lastMessageAdded() == null)
        {
            y = new Date();
            Log.d("SSSS", "ThreadName: " + t2.displayName());
        } else y = t2.lastMessageAdded();

            if (order == ORDER_TYPE_ASC)
                return x.compareTo(y);
            else return y.compareTo(x);
    }
}
