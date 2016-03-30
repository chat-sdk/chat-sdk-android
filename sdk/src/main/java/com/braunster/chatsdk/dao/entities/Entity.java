package com.braunster.chatsdk.dao.entities;


import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BPath;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by itzik on 6/16/2014.
 */
public class Entity implements com.braunster.chatsdk.dao.entity_interface.Entity{

    private Date lastUpdated;

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
    public void updateFromMap(Map<String, Object> map) {

    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public Map<String, Object> asMap() {
        return null;
    }

    @Override
    public Object getPriority() {
        return null;
    }

    @Override
    public Date lastUpdated() {
        return null;
    }

    @Override
    public void setLastUpdated(Date date) {

    }

    @Override
    public void setEntityID(String entityID) {

    }

    @Override
    public String getEntityID() {
        return null;
    }

    @Override
    public <ChildEntity extends Entity> List<ChildEntity> getChildren() {
        return null;
    }

    @Override
    public Long getId() {
        return -1L;
    }

    @Override
    public boolean isDirty() {

        if (this instanceof BUser)
        {
            return ((BUser) this).getDirty() == null || ((BUser) this).getDirty();
        }
        else if (this instanceof BMessage)
        {
            return ((BMessage) this).getDirty() == null || ((BMessage) this).getDirty();
        }
        else if (this instanceof BMetadata)
        {
            return ((BMetadata) this).getDirty() == null || ((BMetadata) this).getDirty();
        }
        else if (this instanceof BThread)
        {
            return ((BThread) this).getDirty() == null || ((BThread) this).getDirty();
        }

        return true;
    }

    @Override
    public void setAsDirty(boolean dirty) {
        if (this instanceof BUser)
        {
            ((BUser) this).setDirty(dirty);
        }
        else if (this instanceof BMessage)
        {
            ((BMessage) this).setDirty(dirty);
        }
        else if (this instanceof BMetadata)
        {
            ((BMetadata) this).setDirty(dirty);
        }
        else if (this instanceof BThread)
        {
            ((BThread) this).setDirty(dirty);
        }
    }
}
