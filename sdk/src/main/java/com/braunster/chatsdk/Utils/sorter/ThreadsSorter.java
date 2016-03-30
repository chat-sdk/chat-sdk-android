/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils.sorter;

import com.braunster.chatsdk.dao.BThread;

import java.util.Comparator;
import java.util.Date;

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
        Date x, y;
        if (t1.lastMessageAdded() == null)
        {
            x = new Date();
        }
        else x = t1.lastMessageAdded();

        if(t2.lastMessageAdded() == null)
        {
            y = new Date();
        } else y = t2.lastMessageAdded();

            if (order == ORDER_TYPE_ASC)
                return x.compareTo(y);
            else return y.compareTo(x);
    }
}
