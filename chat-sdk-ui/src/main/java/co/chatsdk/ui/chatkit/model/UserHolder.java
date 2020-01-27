package co.chatsdk.ui.chatkit.model;

import com.stfalcon.chatkit.commons.models.IUser;

import co.chatsdk.core.dao.User;

public class UserHolder implements IUser {

    protected User user;

    public UserHolder(User user) {
        this.user = user;
    }

    public UserHolder(User user, boolean hideName) {
        this.user = user;
    }

    @Override
    public String getId() {
        return user.getEntityID();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getAvatar() {
        return user.getAvatarURL();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof UserHolder && getId().equals(((UserHolder)object).getId());
    }

    public User getUser() {
        return user;
    }

    public boolean isOnline() {
        return user.getIsOnline();
    }
}
