/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.dao.sorter;

import java.util.Comparator;
import java.util.Date;

import co.chatsdk.core.dao.Thread;

public class ThreadsSorter implements Comparator<Thread> {
    public static final int ORDER_TYPE_ASC = 0;
    public static final int ORDER_TYPE_DESC = 1;

    private int order = ORDER_TYPE_DESC;

    public ThreadsSorter(){}

    public ThreadsSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(Thread t1, Thread t2) {
        if (order == ORDER_TYPE_ASC) {
            return t1.lastMessageAddedDate().compareTo(t2.lastMessageAddedDate());
        }
        else {
            return t2.lastMessageAddedDate().compareTo(t1.lastMessageAddedDate());
        }
    }
}
