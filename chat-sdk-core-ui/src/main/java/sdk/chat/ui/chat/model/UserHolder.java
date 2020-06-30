package sdk.chat.ui.chat.model;

import com.stfalcon.chatkit.commons.models.IUser;

import sdk.chat.core.dao.User;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.module.UIModule;

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
        if (!UIModule.config().showAvatarInChatView) {
            return null;
        }
        String url = user.getAvatarURL();
        if (url == null) {
            url =  ImageUtils.uriForResourceId(ChatSDK.ctx(), UIModule.config().defaultProfilePlaceholder).toString();
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
