package co.chatsdk.ui.contacts;

import android.graphics.Bitmap;

import java.io.Serializable;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.utils.AvailabilityHelper;

/**
 * Created by benjaminsmiley-andrews on 14/07/2017.
 */

public class UserListItem implements Serializable {

    private User user;
    private int type;
    private int resourceID;

    public UserListItem(User user, int resourceID, int type) {
        this.resourceID  = resourceID;
        this.type = type;
        this.user = user;
    }

    public String getText() {
        return user.getName();
    }

    public User getUser () {
        return user;
    }

    public int getItemType() {
        return type;
    }

    public int getResourceID() {
        return resourceID;
    }

    public String getEntityID() {
        return user.getEntityID();
    }

    public String getStatus () {
        return user.getStatus();
    }

    public int getAvailability () {
        return AvailabilityHelper.imageResourceIdForAvailability(user.getAvailability());
    }

}