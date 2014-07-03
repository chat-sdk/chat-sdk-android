package com.braunster.chatsdk.dao.entities;

import com.braunster.chatsdk.network.firebase.BPath;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by itzik on 6/16/2014.
 */
public class Entity<E> implements com.braunster.chatsdk.dao.entity_interface.Entity<E>{

    private Date lastUpdated;

    public Entity(){

    }

    @Override
    public void updateFrom(E e) {

    }

    /** It is important to notice that if your path depends on another object path <b>do not</b> call the Object.getPath() directly.
     * The GreenDao load object lezley so you need to use the getter method of this object.*/
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
    public void setEntityID(String entityID) {

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
    public <T extends Entity> List<T> getChildren() {
        return null;
    }

    @Override
    public String mapPath() {
        return "";
    }

    @Override
    public Long getId() {
        return -1L;
    }
}
