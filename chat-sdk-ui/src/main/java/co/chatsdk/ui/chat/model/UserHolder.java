package co.chatsdk.ui.chat.model;

import android.content.Context;

import com.stfalcon.chatkit.commons.models.IUser;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.image.ImageUtils;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.module.DefaultUIModule;

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
        String url = user.getAvatarURL();
        if (url == null) {
            url =  ImageUtils.uriForResourceId(ChatSDK.ctx(), DefaultUIModule.config().defaultProfileImage).toString();
        }
        return url;
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
