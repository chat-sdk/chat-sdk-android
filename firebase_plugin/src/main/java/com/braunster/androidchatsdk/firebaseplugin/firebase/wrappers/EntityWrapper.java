/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors;
import com.braunster.chatsdk.object.ChatError;
import com.google.firebase.database.DatabaseError;

public class EntityWrapper<E>{
    
    protected E model;
    protected String entityId;


    public E getModel() {
        return model;
    }

    public String getEntityId() {
        return entityId;
    }
    
    protected ChatError getFirebaseError(DatabaseError firebaseError){
        return FirebaseErrors.getFirebaseError(firebaseError);
        
    }
}
