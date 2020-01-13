package co.chatsdk.ui.threads.chatkit;

import com.stfalcon.chatkit.commons.models.IUser;

import co.chatsdk.core.dao.User;

public class UserView implements IUser {

    protected User user;

    public UserView(User user) {
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
}
