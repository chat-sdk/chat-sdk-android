/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import java.util.Comparator;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.ui.chat.MessageListItem;

public class MessageItemSorter implements Comparator<MessageListItem> {
    public static final int ORDER_TYPE_ASC = DaoCore.ORDER_ASC;
    public static final int ORDER_TYPE_DESC = DaoCore.ORDER_DESC;

    private int order = ORDER_TYPE_DESC;

    public MessageItemSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(MessageListItem m1, MessageListItem m2) {
        if(m1 == null || m2 == null) {
            return 0;
        }
        else {
            if (order == ORDER_TYPE_ASC) {
                return m1.getTimeInMillis() > m2.getTimeInMillis() ? -1 : 1;
            }
            else {
                return m1.getTimeInMillis() > m2.getTimeInMillis() ? 1 : -1;
            }
        }
    }
}
