/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseNetworkAdapter;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
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
    
    protected AbstractNetworkAdapter getNetworkAdapter(){
        return BNetworkManager.sharedManager().getNetworkAdapter();
    }
    
    protected BError getFirebaseError(DatabaseError firebaseError){
        return BFirebaseNetworkAdapter.getFirebaseError(firebaseError);
        
    }
}
