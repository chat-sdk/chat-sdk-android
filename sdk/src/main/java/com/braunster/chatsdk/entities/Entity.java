package com.braunster.chatsdk.entities;

import com.braunster.chatsdk.firebase.BPath;
import com.firebase.client.Firebase;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Created by itzik on 6/9/2014.
 */
public abstract class Entity {

    public enum entityType{
        typeUser, typeMessage, typeGroup, typeThread
    }

    public String entityID;

    public Date lastUpdated;

    // ASK if i can assume priority is always string? in obective-c priority get id
    // (which is like Object if i understood right). and in java it have to be String,double, int ...
    public String priority;

    public abstract void updatedFrom(Object object);

    public abstract BPath getPath();

    public abstract Map<String, Object> asMap();
}
