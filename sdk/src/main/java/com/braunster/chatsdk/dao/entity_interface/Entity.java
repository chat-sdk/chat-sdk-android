/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.dao.entity_interface;


import com.braunster.chatsdk.network.BPath;

/**
 * Created by itzik on 6/16/2014.
 */
public interface Entity{

    public static enum Type{
        bEntityTypeUser,
        bEntityTypeMessages,
        bEntityTypeGroup,
        bEntityTypeThread
    }


    public BPath getBPath();

    public Type getEntityType();

    void setEntityID(String entityID);

    public String getEntityID();

    public Long getId();
}
