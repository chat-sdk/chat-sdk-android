package com.braunster.chatsdk.Utils.sorter;

import com.braunster.chatsdk.dao.BMessage;

import java.util.Comparator;

/**
 * Created by braunster on 18/06/14.
 */
public class MsgSorter implements Comparator<BMessage> {
    public static final int ORDER_TYPE_ASC = 0;
    public static final int ORDER_TYPE_DESC = 1;

    @Override
    public int compare(BMessage x, BMessage y) {
        // TODO: Handle null x or y values
        int startComparison = compare(x.getDate().getTime(), y.getDate().getTime());
        return startComparison != 0 ? startComparison
                : compare(x.getDate().getTime(), y.getDate().getTime());
    }

    // I don't know why this isn't in Long...
    private  int compare(long a, long b) {
        return a > b ? -1
                : a > b ? 1
                : 0;
    }
}
