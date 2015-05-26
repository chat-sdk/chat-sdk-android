/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.dao.entities;

import com.braunster.chatsdk.dao.BFollower;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.BUserConnection;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BPath;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by braunster on 25/06/14.
 */
public abstract class BUserEntity extends Entity {

    public String email ="";

    public static String safeAuthenticationID(String aid, int type){

        // Numbers are like the Provider enum in simple login.
        String prefix = "";
        switch (type){
            case BDefines.ProviderInt.Password:
                prefix = "simplelogin";
                break;
            case BDefines.ProviderInt.Facebook:
                prefix = "facebook";
                break;
            case BDefines.ProviderInt.Twitter:
                prefix = "twitter";
                break;
            case BDefines.ProviderInt.Anonymous:
                prefix = "anonymous";
                break;
            case BDefines.ProviderInt.Google:
                prefix = "google";
                break;
            case BDefines.ProviderInt.Custom:
                prefix = "custom";
                break;
        }

        prefix += aid;

        return prefix;
    }


    @Override
    public BPath getBPath() {
        return new BPath().addPathComponent(BFirebaseDefines.Path.BUsers, getEntityID());
    }

    public abstract List<BThread> getThreads();

    public abstract List<BThread> getThreads(@BThreadEntity.ThreadType String type);

    public abstract List<BThread> getThreads(@BThreadEntity.ThreadType String type, boolean allowDeleted);

    public int unreadMessageCount(){
        List<BThread> threads = getThreads();

        int count = 0;
        for (BThread t : threads)
        {
            count += t.getUnreadMessagesAmount();
        }

        return count;
    }

    public abstract Date dateOfBirth();

    // Age calculation taken from here: http://stackoverflow.com/a/1116138/2568492
    public int age(){
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth());
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;
        } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
            age--;
        }

        return age;
    }



    /* User connections */

    public abstract void disconnectUser(BUser user, @BUserConnection.ConnectionType int type);

    public abstract List<BUser> connectionsWithType(@BUserConnection.ConnectionType int type);

    public abstract void connectUser(BUser user, @BUserConnection.ConnectionType int type);


    public abstract BFollower fetchOrCreateFollower(BUser follower, int type);

    public abstract List<BUser> getFollowers();

    public abstract List<BUser> getFollows();






    /* User metadata*/

    public abstract void setPictureUrl(String imageUrl);

    public abstract String getPictureUrl();

    public abstract void setPictureThumbnail(String thumbnailUrl);
    
    public abstract void setName(String name);

    public abstract String getName();

    public abstract void setEmail(String email);

    public abstract String getEmail();

    public abstract String getPictureThumbnail();
}
