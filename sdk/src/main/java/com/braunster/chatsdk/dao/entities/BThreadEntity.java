package com.braunster.chatsdk.dao.entities;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.Date;
import java.util.List;

/**
 * Created by braunster on 25/06/14.
 */
public abstract class BThreadEntity extends Entity<BThread>{

    public static class Type{
        public static final int Private = 0;
        public static final int Public = 1;
    }

    public abstract Date lastMessageAdded();

    /** Fetch messages list from the db for current thread, Messages will be order Desc/Asc on demand.*/
    public abstract List<BMessage> getMessagesWithOrder(int order);

    public abstract boolean hasUser(BUser user);


    public abstract List<BUser> getUsers();

    public abstract String displayName();

    public abstract Integer getType();

    public String threadImageUrl(){
        return threadImageUrl(getUsers());

    }

    public String threadImageUrl(List<BUser> users){
        String url = "";

        if (getType() == BThread.Type.Private) {
            if (users.size() == 2) {
                String curUserEntity = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID();
                if (!users.get(0).getEntityID().equals(curUserEntity))
                    url = users.get(0).getThumbnailPictureURL();
                else if (!users.get(1).getEntityID().equals(curUserEntity))
                    url = users.get(1).getThumbnailPictureURL();
            }
        }

        // If the thumbnail is null.
        if (url == null)
            url = "";

        return url;
    }
}
