package com.braunster.chatsdk.Utils;

import com.braunster.chatsdk.dao.BThread;

import java.util.Comparator;

/**
 * Created by braunster on 18/06/14.
 */
public class ThreadsSorter implements Comparator<BThread> {
    public static final int ORDER_TYPE_ASC = 0;
    public static final int ORDER_TYPE_DESC = 1;

    @Override
    public int compare(BThread x, BThread y) {
        // TODO: Handle null x or y values
        int startComparison = compare(x.lastMessageAdded().getTime(), y.lastMessageAdded().getTime());
        return startComparison != 0 ? startComparison
                : compare(x.lastMessageAdded().getTime(), y.lastMessageAdded().getTime());
    }

    // I don't know why this isn't in Long...
    private  int compare(long a, long b) {
        return a > b ? -1
                : a > b ? 1
                : 0;
    }
}
