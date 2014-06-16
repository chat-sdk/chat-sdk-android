package com.braunster.chatsdk.dao;

import com.braunster.chatsdk.firebase.BPath;

import java.util.Date;
import java.util.Map;

/**
 * Created by itzik on 6/16/2014.
 */
public class Entity<T> implements com.braunster.chatsdk.dao.entity_interface.Entity<T>{

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
        return null;
    }

    @Override
    public void setEntityId(String entityID) {

    }

    @Override
    public String getEntityID() {
        return null;
    }
}
