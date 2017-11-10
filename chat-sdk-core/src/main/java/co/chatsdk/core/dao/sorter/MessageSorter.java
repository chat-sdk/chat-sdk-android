/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.dao.sorter;


import java.util.Comparator;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.DaoCore;

public class MessageSorter implements Comparator<Message> {

    private int order = DaoCore.ORDER_DESC;

    public MessageSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(Message m1, Message m2) {
        if(m1 == null || m2 == null || m1.getDate() == null || m2.getDate() == null) {
            return 0;
        }
        else {
            return (order == DaoCore.ORDER_ASC ? 1 : -1 ) * m1.getDate().toDate().compareTo(m2.getDate().toDate());
        }
    }
}
