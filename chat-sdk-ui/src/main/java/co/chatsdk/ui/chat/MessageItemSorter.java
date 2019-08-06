/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import java.util.Comparator;

import co.chatsdk.core.dao.DaoCore;

public class MessageItemSorter implements Comparator<MessageListItem> {

    private int order = DaoCore.ORDER_DESC;

    public MessageItemSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(MessageListItem m1, MessageListItem m2) {
        if(m1 == null || m2 == null) {
            return 0;
        }
        else {
            if (order == DaoCore.ORDER_ASC) {
                return m1.getTimeInMillis() > m2.getTimeInMillis() ? -1 : 1;
            }
            else {
                return m1.getTimeInMillis() > m2.getTimeInMillis() ? 1 : -1;
            }
        }
    }
}
