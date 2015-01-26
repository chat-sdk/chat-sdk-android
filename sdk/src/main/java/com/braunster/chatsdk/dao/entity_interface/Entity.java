package com.braunster.chatsdk.dao.entity_interface;


import com.braunster.chatsdk.network.BPath;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    public void updateFromMap(Map<String, Object> map);

    public Map<String , Object> asMap();

    public Object getPriority();

    public Date lastUpdated();

    public void setLastUpdated(Date date);

    void setEntityID(String entityID);

    public String getEntityID();

    public <ChildEntity extends com.braunster.chatsdk.dao.entities.Entity> List<ChildEntity> getChildren();

    public Long getId();

    public boolean isDirty();

    public void setAsDirty(boolean dirty);
}
