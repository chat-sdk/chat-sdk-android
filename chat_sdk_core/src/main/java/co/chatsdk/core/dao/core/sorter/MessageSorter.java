/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.dao.core.sorter;


import java.util.Comparator;

import co.chatsdk.core.dao.core.BMessage;
import co.chatsdk.core.dao.core.DaoCore;

public class MessageSorter implements Comparator<BMessage> {
    public static final int ORDER_TYPE_ASC = DaoCore.ORDER_ASC;
    public static final int ORDER_TYPE_DESC = DaoCore.ORDER_DESC;

    private int order = ORDER_TYPE_DESC;

    public MessageSorter(){}

    public MessageSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(BMessage x, BMessage y) {
        if (order == ORDER_TYPE_ASC)
            return (x == null || y == null) ? -1 : (x.getDate().toDate() == null || y.getDate().toDate() == null ) ? -1 : x.getDate().toDate().compareTo(y.getDate().toDate());
        else return (x == null || y == null) ? 1 : (x.getDate().toDate() == null || y.getDate().toDate() == null ) ? -1 : y.getDate().toDate().compareTo(x.getDate().toDate());
    }
}
