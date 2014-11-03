package com.braunster.chatsdk.Utils.sorter;

import com.braunster.chatsdk.adapter.ChatSDKMessagesListAdapter;
import com.braunster.chatsdk.dao.core.DaoCore;

import java.util.Comparator;

import static com.braunster.chatsdk.adapter.ChatSDKMessagesListAdapter.MessageListItem;

/**
 * Created by braunster on 18/06/14.
 */
public class MessageItemSorter implements Comparator<ChatSDKMessagesListAdapter.MessageListItem> {
    public static final int ORDER_TYPE_ASC = DaoCore.ORDER_ASC;
    public static final int ORDER_TYPE_DESC = DaoCore.ORDER_DESC;

    private int order = ORDER_TYPE_DESC;

    public MessageItemSorter(){}

    public MessageItemSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(MessageListItem x, MessageListItem y) {

        if (order == ORDER_TYPE_ASC)
            return (x == null || y == null) ? -1 : x.getTimeInMillis() > y.getTimeInMillis() ? -1 : 1;
        else return (x == null || y == null) ? 1 : x.getTimeInMillis() > y.getTimeInMillis()? 1 : -1 ;
    }
}
