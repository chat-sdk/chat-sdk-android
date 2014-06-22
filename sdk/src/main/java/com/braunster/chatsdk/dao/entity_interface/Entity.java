package com.braunster.chatsdk.dao.entity_interface;

import com.braunster.chatsdk.network.firebase.BPath;

import java.util.Date;
import java.util.Map;

/**
 * Created by itzik on 6/16/2014.
 */
public interface Entity<T> {

    public static enum Type{
        bEntityTypeUser,
        bEntityTypeMessages,
        bEntityTypeGroup,
        bEntityTypeThread
    }

    public void updateFrom(T t);

    public BPath getPath();

    public Type getEntityType();

    public void updateFromMap(Map<String, Object> map);

    public Map<String , Object> asMap();

    public Object getPriority();

    public Date lastUpdated();

    public void setEntityId(String entityID);

    public String getEntityID();

}
