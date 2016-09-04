/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.FollowerLink;
import com.braunster.chatsdk.interfaces.AppEvents;

public class Event implements AppEvents{

    public enum Type{
        AppEvent, ThreadEvent, ThreadAddedEvent, MessageEvent, UserDetailsEvent, FollwerEvent;
    }

    protected String tag = "";
    protected String entityId = "";
    protected  Type type;

    public Event(String tag, String entityId) {
        this.tag = tag;
        this.entityId = entityId;
    }

    public Event(String tag, String entityId, Type type) {
        this.tag = tag;
        this.type = type;
        this.entityId = entityId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    /**
     * Kills the event.
     * Implemented in the {@link com.braunster.chatsdk.network.events.BatchedEvent BatchedEvent}
     **/
    public void kill(){
        
        
    }
    
    @Override
    public boolean onUserDetailsChange(BUser user) {
        return false;
    }

    @Override
    public boolean onMessageReceived(BMessage message) {
        return false;
    }

    @Override
    public boolean onThreadDetailsChanged(String threadId) {
        return false;
    }

    @Override
    public boolean onUserAddedToThread(String threadId, String userId) {
        return false;
    }

    @Override
    public boolean onFollowerAdded(FollowerLink follower) {

        return false;
    }

    @Override
    public boolean onFollowerRemoved() {
        return false;
    }

    @Override
    public boolean onUserToFollowAdded(FollowerLink follower) {
        return false;
    }

    @Override
    public boolean onUserToFollowRemoved() {
        return false;
    }

    @Override
    public boolean onThreadIsAdded(String threadId) {
        return false;
    }
}
