package com.braunster.chatsdk.dao.core;

import com.braunster.chatsdk.network.firebase.BPath;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by itzik on 6/16/2014.
 */
public class Entity<T> implements com.braunster.chatsdk.dao.entity_interface.Entity<T>{

    private Date lastUpdated;

    public Entity(){

    }

    @Override
    public void updateFrom(T t) {

    }

    @Override
    public BPath getPath() {
        return null;
    }

    @Override
    public Type getEntityType() {
        return null;
    }

    @Override
    public void updateFromMap(Map<String, Object> map) {

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
        return lastUpdated;
    }


    @Override
    public void setEntityId(String entityID) {

    }

    @Override
    public String getEntityID() {
        return null;
    }

    @Override
    public void setLastUpdated(Date date) {
        this.lastUpdated = date;
    }

    @Override
    public <E extends Entity> List<E> getChildren() {
        return null;
    }

    @Override
    public String mapPath() {
        return "";
    }
}
