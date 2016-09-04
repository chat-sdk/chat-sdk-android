/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.dao.entities;

import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.FollowerLink;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BPath;

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
        return new BPath().addPathComponent(BFirebaseDefines.Path.BUsersPath, getEntityID());
    }

    public abstract String[] getCacheIDs();

    public abstract List<BThread> getThreads();

    public abstract List<BThread> getThreads(int type);

    public abstract List<BThread> getThreads(int type, boolean allowDeleted);

    public abstract List<BUser> getContacts();

    public abstract void addContact(BUser user);

    public abstract FollowerLink fetchOrCreateFollower(BUser follower, int type);
    
    public abstract void setMetaPictureUrl(String imageUrl);

    public abstract String getMetaPictureUrl();

    public abstract void setMetaPictureThumbnail(String thumbnailUrl);
    
    public abstract void setMetaName(String name);

    public abstract String getMetaName();

    public abstract void setMetaEmail(String email);

    public abstract String getMetaEmail();

    public abstract String getThumbnailPictureURL();

    public abstract List<BUser> getFollowers();

    public abstract List<BUser> getFollows();
/*    public abstract BMetadata fetchOrCreateMetadataForKey(String key, int type);*/





}
