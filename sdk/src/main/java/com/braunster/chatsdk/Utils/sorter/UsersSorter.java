package com.braunster.chatsdk.Utils.sorter;

import com.braunster.chatsdk.dao.BUser;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * Created by braunster on 25.05.15.
 */
public class UsersSorter implements Comparator<BUser> {
    public static final int ORDER_TYPE_ASC = 0;
    public static final int ORDER_TYPE_DESC = 1;

    private int order = ORDER_TYPE_DESC;

    public UsersSorter(){}

    public UsersSorter(int order) {
        this.order = order;
    }

    @Override
    public int compare(BUser u1, BUser u2) {

        String x = u1.getName(), y = u2.getName();

        if (StringUtils.isEmpty(x))
        {
            x= "No name";
        }

        if (StringUtils.isEmpty(y))
        {
            y= "No name";
        }

        if (order == ORDER_TYPE_ASC)
            return x.compareTo(y);
        else return y.compareTo(x);
    }
}