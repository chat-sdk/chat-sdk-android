package com.braunster.chatsdk.dao.entities;

import android.graphics.Bitmap;

import com.braunster.chatsdk.dao.BFollower;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.firebase.BFirebaseDefines;
import com.braunster.chatsdk.network.firebase.BPath;

import java.io.File;
import java.util.List;

/**
 * Created by braunster on 25/06/14.
 */
public abstract class BUserEntity extends Entity<BUser> {

    public boolean hasApp = false;
    public boolean pictureExist = false;
    public String email ="";
    public String pictureURL = "";

    public static String safeAuthenticationID(String aid, int type){

        // Numbers are like the Provider enum in simple login.
        String prefix = "";
        switch (type){
            case 1:
                prefix = "pw";
                break;
            case 2:
                prefix = "fb";
                break;
            case 4:
                prefix = "tw";
                break;
            case 5:
                prefix = "an";
                break;
            case 3:
                prefix = "go";
                break;
        }

        prefix += aid;

        return prefix;
    }

    @Override // Note Done!
    public BPath getPath() {
        return new BPath().addPathComponent(BFirebaseDefines.Path.BUsersPath, getEntityID());
    }

    public abstract String[] getCacheIDs();

    public abstract List<BThread> getThreads();

    public abstract List<BThread> getThreads(int type);

    public abstract List<BUser> getContacts();

    public abstract void addContact(BUser user);

    public abstract BFollower fetchOrCreateFollower(BUser follower, int type);

    public abstract void addMetaDataObject(BMetadata metadata);

    public abstract Bitmap getThumnail();

    public abstract void setMetaPicture(Bitmap bitmap);

    public abstract void setMetaPicture(String base64);

    public abstract void setMetaPicture(File image);

    public abstract void setMetaPictureUrl(String imageUrl);

    public abstract String getMetaPictureUrl();

    public abstract void setMetaName(String name);

    public abstract String getMetaName();

    public abstract void setMetaEmail(String email);

    public abstract String getMetaEmail();

    public abstract String getThumbnailPictureURL();

    public abstract List<BUser> getFollowers();

    public abstract List<BUser> getFollows();
/*    public abstract BMetadata fetchOrCreateMetadataForKey(String key, int type);*/





}
