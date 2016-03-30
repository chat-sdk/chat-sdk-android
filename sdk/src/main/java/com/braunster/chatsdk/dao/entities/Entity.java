/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.dao.entities;


import com.braunster.chatsdk.network.BPath;

/**
 * Created by itzik on 6/16/2014.
 */
public class Entity implements com.braunster.chatsdk.dao.entity_interface.Entity{

    public Entity(){

    }

    /** It is important to notice that if your path depends on another object path <b>do not</b> call the Object.getBPath() directly.
     * The GreenDao load object lezley so you need to use the getter method of this object.*/
    @Override
    public BPath getBPath() {
        return null;
    }

    @Override
    public Type getEntityType() {
        return null;
    }

    @Override
    public void setEntityID(String entityID) {

    }

    @Override
    public String getEntityID() {
        return null;
    }

    @Override
    public Long getId() {
        return -1L;
    }
}
